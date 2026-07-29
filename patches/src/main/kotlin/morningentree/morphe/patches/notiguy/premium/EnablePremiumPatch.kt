package morningentree.morphe.patches.notiguy.premium

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
import morningentree.morphe.patches.notiguy.shared.Constants
import java.util.logging.Logger

private const val PREMIUM_KEY = "premium"
private const val SHARED_PREFERENCES = "Landroid/content/SharedPreferences;"

/**
 * Matches a boolean preference read whose result we can force to `true`.
 *
 * NotiGuy reads the "premium" flag two ways, and in both the key is the **second** argument
 * and the default the third — so the register/`move-result` handling below is identical:
 *
 *  A. Direct: `SharedPreferences.getBoolean(String, Z)Z`  (invoke-interface {prefs, key, def}).
 *  B. Obfuscated wrapper: a static `(L…, String, Z)Z` helper that forwards to `getBoolean`
 *     (e.g. `androidx/lifecycle/z->h(SaStyle, "premium", def)`). The class/method names are
 *     obfuscated and change per release, so we match it by shape — a static boolean method
 *     taking (object, String, boolean) — rather than by name. The "premium" key guard below
 *     keeps this from touching any unrelated helper.
 */
private fun Instruction.isBooleanPrefRead(): Boolean {
    val ref = (this as? ReferenceInstruction)?.reference as? MethodReference ?: return false

    // A. Direct SharedPreferences.getBoolean(String, Z)Z
    if ((opcode == Opcode.INVOKE_INTERFACE || opcode == Opcode.INVOKE_INTERFACE_RANGE) &&
        ref.definingClass == SHARED_PREFERENCES &&
        ref.name == "getBoolean" &&
        ref.parameterTypes.size == 2 &&
        ref.returnType == "Z"
    ) {
        return true
    }

    // B. Obfuscated static wrapper: (object, String, boolean) -> boolean
    if ((opcode == Opcode.INVOKE_STATIC || opcode == Opcode.INVOKE_STATIC_RANGE) &&
        ref.returnType == "Z" &&
        ref.parameterTypes.size == 3 &&
        ref.parameterTypes[0].startsWith("L") &&
        ref.parameterTypes[1] == "Ljava/lang/String;" &&
        ref.parameterTypes[2] == "Z"
    ) {
        return true
    }

    return false
}

private fun Method.readsAnyBooleanPref(): Boolean =
    instructionsOrNull?.any { it.isBooleanPrefRead() } == true

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks NotiGuy premium. Use with Spoof Install Source",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val logger = Logger.getLogger(this::class.java.name)
        var patchedCount = 0

        classDefForEach { classDef ->
            if (classDef.methods.none { it.readsAnyBooleanPref() }) return@classDefForEach

            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructionList = method.instructionsOrNull?.toList() ?: return@forEach

                instructionList.forEachIndexed { index, instruction ->
                    if (!instruction.isBooleanPrefRead()) return@forEachIndexed

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

        logger.info("NotiGuy premium: forced $patchedCount \"$PREMIUM_KEY\" read(s) to true.")
    }
}
