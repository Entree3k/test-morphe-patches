package morningentree.morphe.patches.remyn.premium

import app.morphe.patcher.Fingerprint

/**
 * Remyn is a Flutter app; its premium decision lives in AOT-compiled Dart
 * (`libapp.so`) and is not directly smali-patchable. Billing, however, goes
 * through the native RevenueCat SDK (`purchases_flutter`), and every
 * `CustomerInfo` handed back to Dart crosses the hybrid-common serialization
 * boundary below — which IS plain DEX. Same architecture as Todo Mate.
 *
 * Both fingerprints anchor on the stable RevenueCat SDK class + method names
 * (never obfuscated).
 */

/**
 * `EntitlementInfosMapperKt.map(EntitlementInfos)` — the single chokepoint that
 * serializes every CustomerInfo (network AND disk-cache) into the Map the Dart
 * layer reads. We inject a synthetic active entitlement into its result.
 */
internal object EntitlementInfosMapperFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lcom/revenuecat/purchases/hybridcommon/mappers/EntitlementInfosMapperKt;" &&
            method.name == "map"
    },
)

/**
 * `EntitlementInfo.isActive()` — forced true so any account that already carries
 * an entitlement object (e.g. a lapsed subscriber) is treated as active without
 * relying on the injection above.
 */
internal object EntitlementInfoIsActiveFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lcom/revenuecat/purchases/EntitlementInfo;" &&
            method.name == "isActive"
    },
)
