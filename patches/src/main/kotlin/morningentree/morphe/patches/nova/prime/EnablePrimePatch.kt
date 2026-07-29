package morningentree.morphe.patches.nova.prime

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import morningentree.morphe.patches.nova.shared.Constants

@Suppress("unused")
val enablePrimePatch = bytecodePatch(
    name = "Enable Prime",
    description = "Unlocks Nova Launcher Prime and everything behind the Prime paywall, locally " +
        "(no Google Play licensing / network check needed).",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        SetPrimeFromPreferencesFingerprint.apply {
            // instructionMatches.last() is the `move-result` that receives getInt("1", 0); its
            // register holds the license level. Overwrite it with 0x200 (Nova's Prime value) so the
            // derived "unlocked" (level != 0) and "isPrime" (level == 0x200) flags are both true.
            val primeReg = instructionMatches.last()
                .getInstruction<OneRegisterInstruction>().registerA
            method.addInstructions(
                instructionMatches.last().index + 1,
                "const/16 v$primeReg, 0x200",
            )
        }
    }
}
