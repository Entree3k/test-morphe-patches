package morningentree.morphe.patches.nova.prime

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import morningentree.morphe.patches.nova.shared.Constants
import morningentree.morphe.util.getReference

@Suppress("unused")
val enablePrimePatch = bytecodePatch(
    name = "Enable Prime",
    description = "Unlocks Nova Launcher Prime and everything behind the Prime paywall, locally " +
        "(no Google Play licensing / network check needed).",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        SetPrimeFromPreferencesFingerprint.method.apply {
            val insns = instructions.toList()

            // The license level is read as `getInt("1", 0)` (the first getInt in this method). Nova's
            // own code then sets isPrime = (level == 0x200), so overwriting that read's result with
            // 0x200 flips both the "unlocked" and "isPrime" flags on every launch.
            val getIntIndex = insns.indexOfFirst {
                it.opcode == Opcode.INVOKE_INTERFACE &&
                    it.getReference<MethodReference>()?.name == "getInt"
            }
            if (getIntIndex < 0) {
                throw PatchException("Could not find the license-level getInt read in Nova's license method.")
            }

            val moveResultIndex = (getIntIndex + 1 until insns.size).first {
                insns[it].opcode == Opcode.MOVE_RESULT
            }
            val register = (insns[moveResultIndex] as OneRegisterInstruction).registerA

            addInstruction(moveResultIndex + 1, "const/16 v$register, 0x200")
        }
    }
}
