package morningentree.morphe.patches.volumestyles.premium

import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import morningentree.morphe.patches.volumestyles.shared.Constants
import morningentree.morphe.util.getReference
import java.util.logging.Logger

private const val PREMIUM_PREF_KEY = "is_premium_cached"

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Volume Styles Premium. Use with Spoof Install Source",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        fun Instruction.isGetBoolean(): Boolean {
            if (opcode != Opcode.INVOKE_INTERFACE && opcode != Opcode.INVOKE_VIRTUAL &&
                opcode != Opcode.INVOKE_INTERFACE_RANGE && opcode != Opcode.INVOKE_VIRTUAL_RANGE
            ) {
                return false
            }
            val ref = getReference<MethodReference>() ?: return false
            return ref.definingClass == "Landroid/content/SharedPreferences;" &&
                ref.name == "getBoolean" &&
                ref.parameterTypes.size == 2 &&
                ref.returnType == "Z"
        }

        fun Instruction.keyRegisterOrNull(): Int? = when (this) {
            is FiveRegisterInstruction -> registerD // {obj, key, def}
            is RegisterRangeInstruction -> startRegister + 1
            else -> null
        }

        fun Method.hasPremiumPrefRead() =
            instructionsOrNull?.any { it.isGetBoolean() } == true

        var patched = 0
        classDefForEach { classDef ->
            if (classDef.methods.none { it.hasPremiumPrefRead() }) return@classDefForEach

            mutableClassDefBy(classDef).methods.forEach { method ->
                val insns = method.instructionsOrNull?.toList() ?: return@forEach

                insns.forEachIndexed { index, insn ->
                    if (!insn.isGetBoolean()) return@forEachIndexed
                    val keyRegister = insn.keyRegisterOrNull() ?: return@forEachIndexed

                    val keyMatches = (index - 1 downTo maxOf(0, index - 10)).any { i ->
                        val prev = insns[i]
                        prev.opcode == Opcode.CONST_STRING &&
                            (prev as OneRegisterInstruction).registerA == keyRegister &&
                            prev.getReference<StringReference>()?.string == PREMIUM_PREF_KEY
                    }
                    if (!keyMatches) return@forEachIndexed

                    val moveResult = insns.getOrNull(index + 1) as? OneRegisterInstruction
                        ?: return@forEachIndexed
                    if (moveResult.opcode != Opcode.MOVE_RESULT) return@forEachIndexed

                    val reg = moveResult.registerA
                    val literal = if (reg <= 15) "const/4 v$reg, 0x1" else "const/16 v$reg, 0x1"
                    method.replaceInstruction(index + 1, literal)
                    patched++
                }
            }
        }

        if (patched == 0) {
            throw PatchException("Could not find any '$PREMIUM_PREF_KEY' read to force premium.")
        }

        logger.info("Volume Styles Enable Premium: forced $patched '$PREMIUM_PREF_KEY' read(s) to true.")
    }
}
