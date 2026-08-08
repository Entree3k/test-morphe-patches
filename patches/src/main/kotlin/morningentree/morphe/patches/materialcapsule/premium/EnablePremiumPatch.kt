package morningentree.morphe.patches.materialcapsule.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import morningentree.morphe.patches.materialcapsule.shared.Constants
import morningentree.morphe.patches.philauncher.shared.disablePairipLicenseCheckPatch
import morningentree.morphe.util.getReference

/**
 * The Pro flag is stored as the AndroidKeyStore-AES encrypted text "yes" in a DataStore entry, so
 * every read of it decrypts the ciphertext and compares the plaintext against that literal. Class
 * and method names are fully obfuscated, but the decrypt helper's signature and the literal are
 * not, so identify the reads by that pair.
 */
private fun MethodReference.isProFlagDecrypt() =
    returnType == "Ljava/lang/String;" &&
        parameterTypes.map { it.toString() } ==
        listOf("Ljava/lang/String;", "Ljavax/crypto/SecretKey;")

private fun Method.readsProFlag(): Boolean {
    val instructions = instructionsOrNull ?: return false

    return instructions.any { it.getReference<StringReference>()?.string == "yes" } &&
        instructions.any { it.getReference<MethodReference>()?.isProFlagDecrypt() == true }
}

/** Matches `FlowCollector.emit(value, continuation)`, whose interface name is obfuscated. */
private fun MethodReference.isFlowEmit() =
    returnType == "Ljava/lang/Object;" &&
        parameterTypes.size == 2 &&
        parameterTypes[0].toString() == "Ljava/lang/Object;"

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Material Capsule Pro.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    // The app ships with the Pairip license check, which kills the re-signed APK on launch.
    dependsOn(disablePairipLicenseCheckPatch)

    execute {
        var patchedCount = 0

        classDefForEach { classDef ->
            if (classDef.methods.none { it.readsProFlag() }) return@classDefForEach

            mutableClassDefBy(classDef).methods.forEach { method ->
                if (!method.readsProFlag()) return@forEach
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach

                // The flag is read two ways: a suspending one-shot getter that returns the Boolean
                // itself, and a flow whose collector emits it. Both must report Pro.
                val emits = instructions.withIndex().mapNotNull { (index, instruction) ->
                    if (instruction.opcode != Opcode.INVOKE_INTERFACE) return@mapNotNull null
                    if (instruction.getReference<MethodReference>()?.isFlowEmit() != true) {
                        return@mapNotNull null
                    }

                    (instruction as? FiveRegisterInstruction)?.let { index to it.registerD }
                }

                if (emits.isEmpty()) {
                    method.addInstructions(
                        0,
                        """
                            sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                            return-object v0
                        """,
                    )
                } else {
                    // Overwrite the value about to be emitted. Walk backwards so the remaining
                    // indices stay valid as instructions are inserted.
                    emits.reversed().forEach { (index, register) ->
                        method.addInstruction(
                            index,
                            "sget-object v$register, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
                        )
                    }
                }

                patchedCount++
            }
        }

        if (patchedCount == 0) throw PatchException(
            "No Pro flag read was found. Re-derive.",
        )
    }
}
