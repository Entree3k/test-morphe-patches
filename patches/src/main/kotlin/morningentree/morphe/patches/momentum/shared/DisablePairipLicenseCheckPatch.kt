package morningentree.morphe.patches.momentum.shared

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import java.util.logging.Logger

/**
 * Dependency patch (no [name] → always runs when a dependent patch is selected).
 *
 * Momentum is wrapped by Pairip. Its Application ([com.pairip.application.Application]) runs, from
 * `attachBaseContext`, before any app code:
 *
 *     invoke-static {p1}, Lcom/pairip/licensecheck/LicenseClient;->checkLicense(Landroid/content/Context;)V
 *
 * On a sideloaded / re-signed APK the Play license verification fails and Pairip launches
 * `LicenseActivity` with an error, blocking the app at launch. We neutralise it by making the static
 * `checkLicense` return immediately. This is the licensecheck variant only (no `com.pairip.SignatureCheck`
 * in this build); it does NOT bypass Play Integrity attestation or pairipcore virtualization.
 */
@Suppress("unused")
val disablePairipLicenseCheckPatch = bytecodePatch(
    description = "Neutralizes the Pairip client-side license check that blocks the re-signed APK at launch.",
) {
    execute {
        val logger = Logger.getLogger(this::class.java.name)
        var patched = false

        classDefForEach { classDef ->
            if (classDef.type != "Lcom/pairip/licensecheck/LicenseClient;") return@classDefForEach

            mutableClassDefBy(classDef).methods
                .filter { it.name == "checkLicense" && it.returnType == "V" }
                .forEach { method ->
                    method.addInstruction(0, "return-void")
                    patched = true
                }
        }

        if (patched) {
            logger.info("Pairip: disabled LicenseClient.checkLicense (no-op).")
        } else {
            logger.warning("Pairip: LicenseClient.checkLicense not found; no changes applied.")
        }
    }
}
