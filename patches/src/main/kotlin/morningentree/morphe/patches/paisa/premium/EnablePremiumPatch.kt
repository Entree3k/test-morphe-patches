package morningentree.morphe.patches.paisa.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.paisa.shared.Constants
import morningentree.morphe.util.injectActiveRevenueCatEntitlements
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Paisa Premium. Use With Spoof Install Source.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    // Note: Paisa's Pairip is only LicenseClient.checkLicense (non-fatal — the app
    // still launches) and premium is decided by RevenueCat, so no Pairip patch is
    // needed here.

    execute {
        // Paisa is Flutter: the premium decision lives in AOT Dart, but billing runs
        // through the native RevenueCat SDK. Inject a synthetic active entitlement at
        // the DEX serialization boundary (EntitlementInfosMapperKt.map) under every
        // common id (Paisa's real id is "premium", confirmed in libapp.so), in both
        // the "all" and "active" sub-maps.
        EntitlementInfosMapperFingerprint.method.injectActiveRevenueCatEntitlements()

        // Any pre-existing entitlement object also reports active.
        EntitlementInfoIsActiveFingerprint.method.returnEarly(true)
    }
}
