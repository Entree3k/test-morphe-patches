package morningentree.morphe.patches.lightmeter.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.lightmeter.shared.Constants
import morningentree.morphe.util.COMMON_REVENUECAT_ENTITLEMENT_IDS
import morningentree.morphe.util.injectActiveRevenueCatEntitlements
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Lightmeter Pro. Use With Spoof Install Source.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Flutter + native RevenueCat: inject a synthetic active entitlement at the DEX
        // serialization boundary (EntitlementInfosMapperKt.map). Lightmeter's Dart checks
        // the custom entitlement id "m3_lightmeter_pro_entitlement" (from libapp.so), which
        // is NOT in the common set, so add it alongside the defaults.
        EntitlementInfosMapperFingerprint.method.injectActiveRevenueCatEntitlements(
            COMMON_REVENUECAT_ENTITLEMENT_IDS + "m3_lightmeter_pro_entitlement",
        )

        // Any pre-existing entitlement object also reports active.
        EntitlementInfoIsActiveFingerprint.method.returnEarly(true)
    }
}
