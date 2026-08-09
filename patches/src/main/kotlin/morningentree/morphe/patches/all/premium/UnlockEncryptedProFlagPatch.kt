package morningentree.morphe.patches.all.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import morningentree.morphe.util.getReference
import java.util.logging.Logger

private fun MethodReference.isProFlagDecrypt() =
    returnType == "Ljava/lang/String;" &&
        parameterTypes.map { it.toString() } ==
        listOf("Ljava/lang/String;", "Ljavax/crypto/SecretKey;")

private fun Method.readsProFlag(): Boolean {
    val instructions = instructionsOrNull ?: return false

    return instructions.any { it.getReference<StringReference>()?.string == "yes" } &&
        instructions.any { it.getReference<MethodReference>()?.isProFlagDecrypt() == true }
}

private fun MethodReference.isFlowEmit() =
    returnType == "Ljava/lang/Object;" &&
        parameterTypes.size == 2 &&
        parameterTypes[0].toString() == "Ljava/lang/Object;"

@Suppress("unused")
val unlockEncryptedProFlagPatch = bytecodePatch(
    name = "Unlock encrypted Pro flag",
    description = "(TESTING) Unlocks apps that gate Pro behind an AES-decrypted \"yes\" flag emitted " +
        "through a Kotlin Flow. No-ops on apps that don't use " +
        "this scheme. Probably Won't Work Most Apps",
) {
    execute {
        val logger = Logger.getLogger(this::class.java.name)

        classDefForEach { classDef ->
            if (classDef.type != "Lcom/pairip/licensecheck/LicenseClient;") return@classDefForEach

            mutableClassDefBy(classDef).methods
                .filter { it.name == "checkLicense" && it.returnType == "V" }
                .forEach { it.addInstruction(0, "return-void") }
        }

        var patchedCount = 0

        classDefForEach { classDef ->
            if (classDef.methods.none { it.readsProFlag() }) return@classDefForEach

            mutableClassDefBy(classDef).methods.forEach { method ->
                if (!method.readsProFlag()) return@forEach
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach

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

        if (patchedCount == 0) {
            logger.info("Encrypted Pro-flag pattern not found; app left unchanged.")
        } else {
            logger.info("Forced $patchedCount encrypted Pro-flag reader(s) to owned.")
        }
    }
}
