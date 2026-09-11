package morningentree.morphe.patches.bottled.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.bottled.shared.Constants
import morningentree.morphe.util.injectActiveRevenueCatEntitlements
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Bottled Premium. Use With Spoof Install Source.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Bottled is React Native (Hermes): the premium decision lives in the JS
        // bundle, but billing runs through the native RevenueCat SDK. Inject a
        // synthetic active entitlement at the DEX serialization boundary
        // (EntitlementInfosMapperKt.map) under every common id ("Bottled Premium"
        // reads as "premium"), in both the "all" and "active" sub-maps.
        EntitlementInfosMapperFingerprint.method.injectActiveRevenueCatEntitlements()

        // Any pre-existing entitlement object also reports active.
        EntitlementInfoIsActiveFingerprint.method.returnEarly(true)
    }
}
