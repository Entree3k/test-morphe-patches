package morningentree.morphe.patches.thefor.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.thefor.shared.Constants
import morningentree.morphe.util.injectActiveRevenueCatEntitlements
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks TheFor Pro. Use With Spoof Install Source.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Flutter + native RevenueCat: inject a synthetic active entitlement at the DEX
        // serialization boundary (EntitlementInfosMapperKt.map). TheFor's Dart checks the
        // "pro" entitlement (is_pro in libapp.so), which is already in the common id set.
        EntitlementInfosMapperFingerprint.method.injectActiveRevenueCatEntitlements()

        // Any pre-existing entitlement object also reports active.
        EntitlementInfoIsActiveFingerprint.method.returnEarly(true)
    }
}
