package morningentree.morphe.patches.slowly.detection

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.slowly.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val slowlyBypassSelfieVerificationPatch = bytecodePatch(
    name = "Slowly bypass selfie verification",
    description = "Neutralizes the root and hook probes that feed Slowly's attestation token " +
        "(com.slowlyapp.AttestationModule) so a modified/emulated device is no longer flagged as " +
        "suspicious. This removes the device-fraud trigger that forces the selfie / ID (IDV) " +
        "verification flow. It does not affect verification that the server mandates by country or " +
        "for an already-flagged account, which is enforced server-side.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Bit 0 of attestation flags: reported clean so the server does not deny the device.
        AttestationLooksRootedFingerprint.method.returnEarly(false)
        // Bit 2 of the attestation flags: reported clean (no frida/xposed detected).
        AttestationLooksHookedFingerprint.method.returnEarly(false)
    }
}
