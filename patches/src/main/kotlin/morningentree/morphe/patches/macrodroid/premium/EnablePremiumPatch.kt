package morningentree.morphe.patches.macrodroid.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.macrodroid.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks MacroDroid Pro, removing the macro limit and other paywalled features.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // The single source of truth for Pro. Forcing it true makes the view model report Pro and
        // renders the server device-check teardown (which only rewrites the backing preference)
        // harmless, so no separate purchase-validation neutering is needed.
        ProStatusFingerprint.method.returnEarly(true)

        // Anti-tamper check: false == "original signature", so the re-signed APK is not treated as
        // modified.
        SignatureCheckFingerprint.method.returnEarly(false)

        // Same check behind the template-store API auth hash; false keeps the hash matching the
        // original signed build so community templates keep loading.
        TemplateStoreSignatureCheckFingerprint.method.returnEarly(false)
    }
}
