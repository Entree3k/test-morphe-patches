package morningentree.morphe.patches.slowly.pairip

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.slowly.shared.Constants

@Suppress("unused")
val slowlyPairipLicenseCheckBypassPatch = bytecodePatch(
    name = "Bypass Pairip License Check",
    description = "Disables client-side Pairip installer and enforcement",
) {
    compatibleWith(Constants.COMPATIBILITY_PAIRIP)

    execute {
        PairipLicenseCheckFingerprint.method.addInstruction(0, "return-void")
    }
}
