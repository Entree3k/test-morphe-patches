package morningentree.morphe.patches.philauncher.shared

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import java.util.logging.Logger

/**
 * Dependency patch (no [name] → always runs when a dependent patch is selected).
 *
 * Phi Launcher is wrapped by Pairip. Its Application ([com.pairip.application.Application]) runs,
 * from `attachBaseContext`, before any app code:
 *
 *     invoke-static {p1}, Lcom/pairip/licensecheck/LicenseClient;->checkLicense(Landroid/content/Context;)V
 *
 * `checkLicense` binds to the Play Store licensing service and verifies the install. On a
 * sideloaded / re-signed APK that verification fails and Pairip launches `LicenseActivity` with an
 * error, blocking the app at launch — so premium would never be reachable.
 *
 * We neutralise it by making the static `checkLicense` return immediately, before it can start the
 * license verification. This only removes the client-side license gate; it does NOT bypass Play
 * Integrity attestation or pairipcore virtualization (this build ships neither — licensecheck only,
 * no `com.pairip.SignatureCheck`).
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
                    // Short-circuit before the license service is ever contacted.
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
