package morningentree.morphe.patches.flowstack.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks FlowStack premium by forcing RevenueCat to report an active entitlement.",
) {
    compatibleWith(
        Compatibility(
            name = "FlowStack",
            packageName = "com.flowstack",
            appIconColor = 0x4F46E5,
            targets = listOf(AppTarget("2.0.5")),
        ),
    )

    execute {
        // FlowStack is Flutter (Dart AOT): the premium decision is in libapp.so and reads
        // customerInfo.entitlements, which the purchases_flutter plugin serializes to Dart via
        // EntitlementInfosMapperKt.map(). For a non-subscriber the native active/all maps are empty
        // (server-populated), so flipping EntitlementInfo.isActive() alone can't manufacture premium.
        //
        // Instead we ignore the real (empty) EntitlementInfos and build a fully-valid one that
        // contains active entitlements, using the SDK's own factory (buildEntitlementInfos). The
        // original mapper body then serializes it, guaranteeing the exact field/enum shapes Dart
        // expects. Several common entitlement identifiers are injected (all pointing at one product)
        // so a by-id Dart lookup — or an "active.isNotEmpty" check — sees premium.
        //
        // Registers: map() is `static ... (EntitlementInfos)` with `.locals 6` (v0-v5 free; p0 is the
        // param). We build the fabricated EntitlementInfos in v0-v5 and overwrite p0, then fall
        // through to the untouched original body.
        val entitlementsJson = "{" +
            "\\\"premium\\\":{\\\"expires_date\\\":\\\"2099-12-31T23:59:59Z\\\",\\\"purchase_date\\\":\\\"2024-01-01T00:00:00Z\\\",\\\"product_identifier\\\":\\\"flowstack.pro\\\"}," +
            "\\\"pro\\\":{\\\"expires_date\\\":\\\"2099-12-31T23:59:59Z\\\",\\\"purchase_date\\\":\\\"2024-01-01T00:00:00Z\\\",\\\"product_identifier\\\":\\\"flowstack.pro\\\"}," +
            "\\\"plus\\\":{\\\"expires_date\\\":\\\"2099-12-31T23:59:59Z\\\",\\\"purchase_date\\\":\\\"2024-01-01T00:00:00Z\\\",\\\"product_identifier\\\":\\\"flowstack.pro\\\"}," +
            "\\\"premium_access\\\":{\\\"expires_date\\\":\\\"2099-12-31T23:59:59Z\\\",\\\"purchase_date\\\":\\\"2024-01-01T00:00:00Z\\\",\\\"product_identifier\\\":\\\"flowstack.pro\\\"}," +
            "\\\"pro_access\\\":{\\\"expires_date\\\":\\\"2099-12-31T23:59:59Z\\\",\\\"purchase_date\\\":\\\"2024-01-01T00:00:00Z\\\",\\\"product_identifier\\\":\\\"flowstack.pro\\\"}" +
            "}"

        val subscriptionsJson = "{" +
            "\\\"flowstack.pro\\\":{" +
            "\\\"expires_date\\\":\\\"2099-12-31T23:59:59Z\\\"," +
            "\\\"purchase_date\\\":\\\"2024-01-01T00:00:00Z\\\"," +
            "\\\"original_purchase_date\\\":\\\"2024-01-01T00:00:00Z\\\"," +
            "\\\"period_type\\\":\\\"normal\\\"," +
            "\\\"store\\\":\\\"play_store\\\"," +
            "\\\"is_sandbox\\\":false," +
            "\\\"ownership_type\\\":\\\"PURCHASED\\\"" +
            "}}"

        EntitlementInfosMapperFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Lorg/json/JSONObject;
                const-string v5, "$entitlementsJson"
                invoke-direct {v0, v5}, Lorg/json/JSONObject;-><init>(Ljava/lang/String;)V

                new-instance v1, Lorg/json/JSONObject;
                const-string v5, "$subscriptionsJson"
                invoke-direct {v1, v5}, Lorg/json/JSONObject;-><init>(Ljava/lang/String;)V

                new-instance v2, Lorg/json/JSONObject;
                const-string v5, "{}"
                invoke-direct {v2, v5}, Lorg/json/JSONObject;-><init>(Ljava/lang/String;)V

                new-instance v3, Ljava/util/Date;
                invoke-direct {v3}, Ljava/util/Date;-><init>()V

                sget-object v4, Lcom/revenuecat/purchases/VerificationResult;->NOT_REQUESTED:Lcom/revenuecat/purchases/VerificationResult;

                invoke-static {v0, v1, v2, v3, v4}, Lcom/revenuecat/purchases/common/EntitlementInfoFactoriesKt;->buildEntitlementInfos(Lorg/json/JSONObject;Lorg/json/JSONObject;Lorg/json/JSONObject;Ljava/util/Date;Lcom/revenuecat/purchases/VerificationResult;)Lcom/revenuecat/purchases/EntitlementInfos;
                move-result-object p0
            """,
        )
    }
}
