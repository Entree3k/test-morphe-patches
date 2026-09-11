package morningentree.morphe.patches.bottled.premium

import app.morphe.patcher.Fingerprint

/**
 * Bottled is a React Native app (Hermes bytecode bundle in
 * `assets/index.android.bundle`). The premium decision lives in JS and is not
 * directly smali-patchable, but billing runs through the native RevenueCat SDK
 * (`react-native-purchases`). Every `CustomerInfo` handed back to JS crosses the
 * hybrid-common serialization boundary below — which IS plain DEX.
 *
 * Confirmed chain: `RNPurchasesModule` → `CustomerInfoMapperKt.map` →
 * `EntitlementInfosMapperKt.map`, so injecting an active entitlement into the
 * latter reaches the JS layer's `customerInfo.entitlements.active`.
 *
 * Both fingerprints anchor on the stable RevenueCat SDK class + method names
 * (never obfuscated). Same architecture as Remyn / Paisa.
 */

/**
 * `EntitlementInfosMapperKt.map(EntitlementInfos)` — the single chokepoint that
 * serializes every CustomerInfo (network AND disk-cache) into the Map the JS
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
