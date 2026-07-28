package morningentree.morphe.patches.dynamicspot.shared

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import java.util.logging.Logger

/**
 * Dependency patch (no [name] → always runs when a dependent patch is selected).
 *
 * DynamicSpot ships with Pairip protection. Its Application
 * ([com.pairip.application.Application]) runs, from `attachBaseContext`:
 *
 *     invoke-static {p1}, Lcom/pairip/SignatureCheck;->verifyIntegrity(Landroid/content/Context;)V
 *
 * `verifyIntegrity` reads the APK's signing certificate via
 * `PackageManager.getPackageInfo(pkg, GET_SIGNATURES)`, SHA-256s `signatures[0]`, and compares
 * it against a hardcoded value. Once Morphe re-signs the patched APK the hash no longer matches
 * and the method throws `SignatureCheck$SignatureTamperedException` — crashing the app the
 * instant it launches (which is exactly the "won't open, just crashes" symptom).
 *
 * We neutralize the check by making `verifyIntegrity` return immediately, before it can read or
 * compare the signature. This does NOT bypass Play Integrity attestation or pairipcore
 * virtualization — it only removes the client-side self-signature check that breaks on re-sign.
 */
@Suppress("unused")
val disablePairipSignatureCheckPatch = bytecodePatch(
    description = "Neutralizes the Pairip client-side signature check that crashes the re-signed APK.",
) {
    execute {
        val logger = Logger.getLogger(this::class.java.name)
        var patched = false

        classDefForEach { classDef ->
            if (classDef.type != "Lcom/pairip/SignatureCheck;") return@classDefForEach

            mutableClassDefBy(classDef).methods
                .filter { it.name == "verifyIntegrity" && it.returnType == "V" }
                .forEach { method ->
                    // Short-circuit before any signature is read or compared.
                    method.addInstruction(0, "return-void")
                    patched = true
                }
        }

        if (patched) {
            logger.info("Pairip: disabled SignatureCheck.verifyIntegrity (no-op).")
        } else {
            logger.warning("Pairip: SignatureCheck.verifyIntegrity not found; no changes applied.")
        }
    }
}
