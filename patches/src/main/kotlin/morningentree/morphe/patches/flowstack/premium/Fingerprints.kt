package morningentree.morphe.patches.flowstack.premium

import app.morphe.patcher.Fingerprint

/**
 * The RevenueCat hybrid-common serializer that converts a native `EntitlementInfos` into the
 * `Map` (`{ all, active, verification }`) sent over the Flutter method channel to Dart.
 *
 * FlowStack is a Flutter (Dart AOT) app; its premium decision lives in `libapp.so` and reads
 * `customerInfo.entitlements`. Every `getCustomerInfo` / purchase / restore / listener update the
 * `purchases_flutter` plugin returns is serialized through this one function, so it is the single
 * native seam that feeds the Dart premium check. SDK class/method names are not obfuscated.
 *
 * See flowstack-premium-findings.md.
 */
internal object EntitlementInfosMapperFingerprint : Fingerprint(
    returnType = "Ljava/util/Map;",
    custom = { method, classDef ->
        classDef.type == "Lcom/revenuecat/purchases/hybridcommon/mappers/EntitlementInfosMapperKt;" &&
            method.name == "map"
    },
)
