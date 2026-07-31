package morningentree.morphe.patches.bend.shared

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import java.util.logging.Logger

/**
 * Dependency patch (no [name] → always runs when a dependent patch is selected).
 *
 * Bend (`com.bowerydigital.bend`) is wrapped by Pairip. Its Application
 * ([com.pairip.application.Application]) runs, from `attachBaseContext`, before any app code:
 *
 *     invoke-static {p1}, Lcom/pairip/VMRunner;->setContext(Landroid/content/Context;)V
 *     invoke-static {p1}, Lcom/pairip/SignatureCheck;->verifyIntegrity(Landroid/content/Context;)V
 *     invoke-static {p1}, Lcom/pairip/licensecheck/LicenseClient;->checkLicense(Landroid/content/Context;)V
 *
 * On a re-signed (Morphe-patched) APK this chain blocks/crashes the app at launch, so premium would
 * never be reachable. We neutralize every client-side gate:
 *
 *  1. [com.pairip.SignatureCheck].verifyIntegrity — SHA-256s the APK signing cert and throws
 *     `SignatureTamperedException` once the signature changes → instant launch crash.
 *  2. [com.pairip.licensecheck.LicenseClient].checkLicense — binds to Play licensing and launches
 *     the blocking `LicenseActivity` when the install can't be verified.
 *  3. [com.pairip.VMRunner].invoke — the pairipcore VM dispatch. Every wrapped `onReceive` and the
 *     `StartupLauncher` startup program route through it (results are discarded), so returning null
 *     makes the whole VM chain inert. Verified against the community-modded APK, whose only pairip
 *     edit is exactly this (invoke → null).
 *  4. [com.pairip.VMRunner].`<clinit>` — drops `System.loadLibrary("pairipcore")`. With `invoke`
 *     gutted the native VM is never used, and the library may live in a split not shipped with the
 *     patched base (loading it would `UnsatisfiedLinkError`-crash at launch). The class's static
 *     field values are dex-encoded, not set here, so clearing the initializer is safe.
 *
 * This removes only the client-side checks; it does not bypass Play Integrity attestation.
 */
@Suppress("unused")
val disablePairipPatch = bytecodePatch(
    description = "Neutralizes Bend's Pairip signature/license checks and the pairipcore VM so the re-signed APK launches.",
) {
    execute {
        val logger = Logger.getLogger(this::class.java.name)
        val done = mutableSetOf<String>()

        classDefForEach { classDef ->
            when (classDef.type) {
                "Lcom/pairip/SignatureCheck;" ->
                    mutableClassDefBy(classDef).methods
                        .filter { it.name == "verifyIntegrity" && it.returnType == "V" }
                        .forEach {
                            it.addInstruction(0, "return-void")
                            done += "verifyIntegrity"
                        }

                "Lcom/pairip/licensecheck/LicenseClient;" ->
                    mutableClassDefBy(classDef).methods
                        .filter { it.name == "checkLicense" && it.returnType == "V" }
                        .forEach {
                            it.addInstruction(0, "return-void")
                            done += "checkLicense"
                        }

                "Lcom/pairip/VMRunner;" ->
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        when {
                            method.name == "invoke" && method.returnType == "Ljava/lang/Object;" -> {
                                method.addInstructions(
                                    0,
                                    """
                                        const/4 v0, 0x0
                                        return-object v0
                                    """,
                                )
                                done += "VMRunner.invoke"
                            }

                            method.name == "<clinit>" -> {
                                method.addInstruction(0, "return-void")
                                done += "VMRunner.<clinit>"
                            }
                        }
                    }
            }
        }

        val expected = setOf("verifyIntegrity", "checkLicense", "VMRunner.invoke", "VMRunner.<clinit>")
        if (done == expected) {
            logger.info("Bend Pairip: neutralized ${done.joinToString()}.")
        } else {
            logger.warning("Bend Pairip: some targets not found (applied: ${done.joinToString().ifEmpty { "none" }}).")
        }
    }
}
