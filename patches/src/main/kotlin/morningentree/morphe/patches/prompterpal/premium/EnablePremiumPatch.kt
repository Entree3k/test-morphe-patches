package morningentree.morphe.patches.prompterpal.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import morningentree.morphe.patches.prompterpal.shared.Constants
import morningentree.morphe.util.getReference

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Prompter Pal Premium. Use With Spoof Install Source",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        SubscriptionStateFingerprint.method.apply {
            val insns = instructions.toList()

            // Each premium StateFlow<Boolean> is pushed through the app's setter helper
            // `static (Z, StateFlow, Object)V`. Within this method the only static calls that
            // take a boolean first argument and return void are those three pushes
            // (hasLifetime, isTrial, hasSubscription). Force every one of them to true.
            val pushIndices = insns.withIndex().filter { (_, insn) ->
                insn.opcode == Opcode.INVOKE_STATIC &&
                    insn.getReference<MethodReference>()?.let { ref ->
                        ref.returnType == "V" &&
                            ref.parameterTypes.firstOrNull()?.toString() == "Z"
                    } == true
            }.map { it.index }

            if (pushIndices.size < 3) {
                throw PatchException(
                    "Expected 3 premium StateFlow pushes in SubscriptionManager, found " +
                        "${pushIndices.size}.",
                )
            }

            // Insert high-to-low so earlier indices remain valid after each insertion.
            pushIndices.sortedDescending().forEach { index ->
                val register = (insns[index] as FiveRegisterInstruction).registerC
                val setTrue =
                    if (register <= 15) "const/4 v$register, 0x1"
                    else "const/16 v$register, 0x1"
                addInstruction(index, setTrue)
            }
        }
    }
}
