package morningentree.morphe.patches.todomate.premium

import app.morphe.patcher.Fingerprint

/**
 * Todo Mate (com.undefined.mate) is a Flutter app: its premium decision lives in Dart
 * (PurchaseManager.dart, compiled into libapp.so) and reads RevenueCat entitlements — the app is
 * gated on a "premium" entitlement (SKUs `todo_mate_premium_monthly_plan` / `_yearly_plan`).
 *
 * Dart cannot be patched from smali, but the RevenueCat SDK it bridges to is plain DEX, and every
 * `CustomerInfo` handed to Dart is serialized through the hybrid-common mappers. `EntitlementInfos`
 * -> Dart goes through exactly one method:
 *   `EntitlementInfosMapperKt.map(EntitlementInfos)` -> Map{ "all": {...}, "active": {...}, ... }
 *
 * This is the single chokepoint for both the network path and the disk-cache path (RevenueCat caches
 * the response JSON and rebuilds CustomerInfo on load, then re-serializes here). Injecting a synthetic
 * active "premium" entitlement into the `all` + `active` sub-maps makes Dart see the account as
 * premium regardless of what the server actually returned. Anchored on the stable RevenueCat class +
 * method names (SDK names are never obfuscated).
 */
internal object EntitlementInfosMapperFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lcom/revenuecat/purchases/hybridcommon/mappers/EntitlementInfosMapperKt;" &&
            method.name == "map"
    },
)

/**
 * `EntitlementInfo.isActive()Z` — the per-entitlement active flag the mapper reads. Forcing it true is
 * a cheap belt-and-suspenders that also unlocks accounts which already have an entitlement in the map
 * (e.g. a lapsed/expired subscriber) without relying on the injection above.
 */
internal object EntitlementInfoIsActiveFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lcom/revenuecat/purchases/EntitlementInfo;" &&
            method.name == "isActive"
    },
)
