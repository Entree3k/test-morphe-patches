package morningentree.morphe.patches.all.detection.pairip

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import java.util.logging.Logger

private const val SIGNATURE_CHECK = "Lcom/pairip/SignatureCheck;"
private const val LICENSE_CLIENT = "Lcom/pairip/licensecheck/LicenseClient;"
private const val VM_RUNNER = "Lcom/pairip/VMRunner;"

/**
 * Universal Pairip neutralizer.
 *
 * Pairip (Google's Play-side app protection) wires three client-side checks into an app's
 * `Application.attachBaseContext`, in order:
 *
 *     Lcom/pairip/VMRunner;->setContext(Landroid/content/Context;)V
 *     Lcom/pairip/SignatureCheck;->verifyIntegrity(Landroid/content/Context;)V   // crashes on re-sign
 *     Lcom/pairip/licensecheck/LicenseClient;->checkLicense(Landroid/content/Context;)V
 *
 * `verifyIntegrity` SHA-256s the APK's signing certificate and throws once Morphe re-signs the
 * APK — the classic "won't open, just crashes on launch" symptom. `checkLicense` contacts the Play
 * licensing service. Both live on stable, never-obfuscated SDK classes, so we short-circuit them by
 * class type (no fingerprints needed). When a target ships neither class this patch simply no-ops.
 *
 * The optional VM gutting is a separate, more aggressive step — see the option below.
 */
@Suppress("unused")
val disablePairipPatch = bytecodePatch(
    name = "Disable Pairip protection",
    description = "Neutralizes Pairip's client-side signature and license checks so the re-signed " +
        "APK launches instead of crashing. Optionally guts the Pairip VM as well. Does not bypass " +
        "server-side Play Integrity attestation.",
    default = false,
) {
    val gutVmRunner by booleanOption(
        key = "gutVmRunner",
        default = false,
        title = "Gut Pairip VM (advanced)",
        description = "Also neutralizes the Pairip VM: makes VMRunner.invoke() return null and " +
            "drops the pairipcore native library load. Only enable this for apps whose VM merely " +
            "runs the startup integrity/license program (its result is discarded). If the app " +
            "routes real functionality through the VM — e.g. broadcast receivers — enabling this " +
            "breaks those features instead of unlocking anything. Leave off unless you know the " +
            "app needs it.",
    )

    execute {
        val logger = Logger.getLogger(this::class.java.name)
        var signaturePatched = false
        var licensePatched = false
        var vmInvokePatched = false
        var vmClinitPatched = false

        classDefForEach { classDef ->
            when (classDef.type) {
                SIGNATURE_CHECK -> mutableClassDefBy(classDef).methods
                    .filter { it.name == "verifyIntegrity" && it.returnType == "V" }
                    .forEach {
                        // Short-circuit before any signature is read or compared.
                        it.addInstruction(0, "return-void")
                        signaturePatched = true
                    }

                LICENSE_CLIENT -> mutableClassDefBy(classDef).methods
                    .filter { it.name == "checkLicense" && it.returnType == "V" }
                    .forEach {
                        // Short-circuit before the license service is ever contacted.
                        it.addInstruction(0, "return-void")
                        licensePatched = true
                    }

                VM_RUNNER -> {
                    if (gutVmRunner != true) return@classDefForEach

                    mutableClassDefBy(classDef).methods.forEach { method ->
                        // VMRunner.invoke(String, Object[]) -> Object : return null so the entire
                        // encrypted-bytecode chain becomes inert.
                        if (
                            method.name == "invoke" &&
                            method.returnType == "Ljava/lang/Object;"
                        ) {
                            method.addInstructions(
                                0,
                                """
                                    const/4 v0, 0x0
                                    return-object v0
                                """,
                            )
                            vmInvokePatched = true
                        }

                        // <clinit> only loads the "pairipcore" native lib; drop it so the missing
                        // (now unused) library can never crash startup.
                        if (method.name == "<clinit>" && method.returnType == "V") {
                            method.addInstruction(0, "return-void")
                            vmClinitPatched = true
                        }
                    }
                }
            }
        }

        logger.info(
            "Pairip: verifyIntegrity=$signaturePatched, checkLicense=$licensePatched, " +
                "VMRunner.invoke=$vmInvokePatched, VMRunner.<clinit>=$vmClinitPatched.",
        )

        if (!signaturePatched && !licensePatched && !vmInvokePatched && !vmClinitPatched) {
            logger.warning("Pairip: no Pairip classes found; no changes applied.")
        }
    }
}
