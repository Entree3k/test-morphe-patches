package morningentree.morphe.patches.adler.shared

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import java.util.logging.Logger

/**
 * Adler's `com.pairip.application.Application.attachBaseContext` calls
 * `SignatureCheck.verifyIntegrity`, which SHA-256s the APK signing certificate
 * and throws `SignatureTamperedException` when it no longer matches — an instant
 * crash on launch for any re-signed (patched) build. No-op it.
 *
 * Nameless dependency patch (kept in Adler's own package so it never depends on
 * another app's folder being present in the build).
 */
@Suppress("unused")
val disablePairipSignatureCheckPatch = bytecodePatch(
    description = "Neutralizes the Pairip SignatureCheck.verifyIntegrity that crashes the re-signed APK at launch.",
) {
    execute {
        val logger = Logger.getLogger(this::class.java.name)
        var patched = false

        classDefForEach { classDef ->
            if (classDef.type != "Lcom/pairip/SignatureCheck;") return@classDefForEach

            mutableClassDefBy(classDef).methods
                .filter { it.name == "verifyIntegrity" && it.returnType == "V" }
                .forEach { method ->
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
