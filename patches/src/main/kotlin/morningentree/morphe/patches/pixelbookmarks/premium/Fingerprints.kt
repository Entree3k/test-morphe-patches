package morningentree.morphe.patches.pixelbookmarks.premium

import app.morphe.patcher.Fingerprint

// RevenueCat hybrid-common bridge. `com.psh.pixel_bookmarks` is a Flutter app: the premium
// decision (`isProFromRevenueCat`) lives in Dart (libapp.so) and reads
// `customerInfo.entitlements.active`. Every CustomerInfo that reaches Dart is serialized by
// CustomerInfoMapperKt -> EntitlementInfosMapperKt.map(EntitlementInfos), so that method is the
// only smali chokepoint that controls what Dart sees. Class/method names come from the
// RevenueCat library and are NOT obfuscated, so a class-type custom predicate is stable.
internal object EntitlementInfosMapperFingerprint : Fingerprint(
    returnType = "Ljava/util/Map;",
    parameters = listOf("Lcom/revenuecat/purchases/EntitlementInfos;"),
    custom = { _, classDef ->
        classDef.type == "Lcom/revenuecat/purchases/hybridcommon/mappers/EntitlementInfosMapperKt;"
    },
)
