package morningentree.morphe.patches.dynamicspot.premium

import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import morningentree.morphe.patches.dynamicspot.shared.Constants
import morningentree.morphe.patches.dynamicspot.shared.disablePairipSignatureCheckPatch
import java.util.logging.Logger

private const val PREMIUM_KEY = "100"
private const val SHARED_PREFERENCES = "Landroid/content/SharedPreferences;"

private fun Instruction.isGetBooleanCall(): Boolean {
    if (opcode != Opcode.INVOKE_INTERFACE && opcode != Opcode.INVOKE_INTERFACE_RANGE) return false
    val ref = (this as? ReferenceInstruction)?.reference as? MethodReference ?: return false
    return ref.definingClass == SHARED_PREFERENCES &&
        ref.name == "getBoolean" &&
        ref.parameterTypes.size == 2 &&
        ref.returnType == "Z"
}

private fun Method.readsAnyBooleanPref(): Boolean =
    instructionsOrNull?.any { it.isGetBooleanCall() } == true

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks DynamicSpot premium",
) {
    compatibleWith(Constants.COMPATIBILITY)

    // Without this the re-signed APK crashes on launch via Pairip's SignatureCheck,
    // so premium would never even be reachable.
    dependsOn(disablePairipSignatureCheckPatch)

    execute {
        val logger = Logger.getLogger(this::class.java.name)
        var patchedCount = 0

        classDefForEach { classDef ->
            if (classDef.methods.none { it.readsAnyBooleanPref() }) return@classDefForEach

            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructionList = method.instructionsOrNull?.toList() ?: return@forEach

                instructionList.forEachIndexed { index, instruction ->
                    if (!instruction.isGetBooleanCall()) return@forEachIndexed

                    val keyRegister = when (instruction) {
                        is FiveRegisterInstruction -> instruction.registerD
                        is RegisterRangeInstruction -> instruction.startRegister + 1
                        else -> return@forEachIndexed
                    }

                    var keyIsPremiumFlag = false
                    for (back in index - 1 downTo maxOf(0, index - 6)) {
                        val prev = instructionList[back]
                        if (prev.opcode == Opcode.CONST_STRING &&
                            (prev as OneRegisterInstruction).registerA == keyRegister
                        ) {
                            keyIsPremiumFlag =
                                ((prev as ReferenceInstruction).reference as StringReference)
                                    .string == PREMIUM_KEY
                            break
                        }
                    }
                    if (!keyIsPremiumFlag) return@forEachIndexed

                    val moveResult = instructionList.getOrNull(index + 1) as? OneRegisterInstruction
                        ?: return@forEachIndexed
                    if (moveResult.opcode != Opcode.MOVE_RESULT) return@forEachIndexed

                    val register = moveResult.registerA
                    val setTrue =
                        if (register < 16) "const/4 v$register, 0x1" else "const/16 v$register, 0x1"
                    method.replaceInstruction(index + 1, setTrue)
                    patchedCount++
                }
            }
        }

        logger.info("DynamicSpot premium: forced $patchedCount \"$PREMIUM_KEY\" read(s) to true.")
    }
}
