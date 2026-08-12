package morningentree.morphe.patches.thefor.premium

import app.morphe.patcher.Fingerprint

/**
 * TheFor (xyz.thefor.habits.habits) is a Flutter app; its Pro decision lives in
 * AOT-compiled Dart (`libapp.so`) and is not directly smali-patchable. Billing,
 * however, goes through the native RevenueCat SDK (`purchases_flutter`), and every
 * `CustomerInfo` handed back to Dart crosses the hybrid-common serialization
 * boundary below — which IS plain DEX.
 *
 * Both fingerprints anchor on stable RevenueCat SDK class + method names.
 */

internal object EntitlementInfosMapperFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lcom/revenuecat/purchases/hybridcommon/mappers/EntitlementInfosMapperKt;" &&
            method.name == "map"
    },
)

internal object EntitlementInfoIsActiveFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lcom/revenuecat/purchases/EntitlementInfo;" &&
            method.name == "isActive"
    },
)
