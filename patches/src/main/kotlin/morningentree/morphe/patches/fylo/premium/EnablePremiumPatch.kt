package morningentree.morphe.patches.fylo.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.all.detection.pairip.disablePairipPatch
import morningentree.morphe.patches.fylo.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Fylo — File Manager Pro (all Pro and AI features) by forcing the app's own " +
        "DebugFeatureUnlock switch, which drives the app-wide isPro/entitlement state. " +
        "Use with Spoof Install Source.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    // The app is wrapped by Pairip's modern licensecheck (no SignatureCheck): a
    // re-signed APK trips LicenseActivity.onStart -> showPaywallAndCloseApp and is
    // killed at launch. Neutralize the whole license chain so it launches.
    dependsOn(disablePairipPatch)

    execute {
        // DebugFeatureUnlock.isActive() -> true makes BillingManager treat the
        // build as fully unlocked: _isPro = true and _entitlement = LIFETIME,
        // pinned across every billing refresh.
        DebugFeatureUnlockIsActiveFingerprint.method.returnEarly(true)
    }
}
