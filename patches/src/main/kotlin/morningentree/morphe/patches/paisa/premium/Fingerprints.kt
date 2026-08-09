package morningentree.morphe.patches.paisa.premium

import app.morphe.patcher.Fingerprint

/**
 * Paisa is a Flutter app; its premium decision is in AOT Dart (`libapp.so`) and
 * not directly smali-patchable. Billing runs through the native RevenueCat SDK
 * (`purchases_flutter`), and every `CustomerInfo` handed back to Dart crosses
 * the hybrid-common serialization boundary below — plain DEX. Same architecture
 * as Todo Mate / Remyn.
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
