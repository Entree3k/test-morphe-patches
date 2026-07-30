package morningentree.morphe.patches.pixelbookmarks.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.pixelbookmarks.shared.Constants

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Pixel Bookmarks premium by forcing an active RevenueCat entitlement " +
        "into every CustomerInfo the Flutter/Dart layer reads.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Rewrite EntitlementInfosMapperKt.map(EntitlementInfos) to ignore the real (empty for a
        // free user) entitlements and return a fabricated map with an always-active "pro"
        // entitlement. The mapper serializes enums/dates to plain strings, so the fabricated
        // entitlement is just a HashMap of String/Boolean constants — no Date/enum objects.
        // Shape mirrors the real EntitlementInfoMapperKt output for the fields Dart's
        // EntitlementInfo.fromJson requires. Keys "pro"/"premium"/"plus" cover both a keyed
        // lookup and an `active.isNotEmpty` check. Method is `.locals 6`, 1 param (p0) — we
        // return before the original body, so v0-v4 are free to clobber.
        EntitlementInfosMapperFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Ljava/util/HashMap;
                invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

                const-string v1, "identifier"
                const-string v2, "pro"
                invoke-virtual {v0, v1, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v1, "isActive"
                sget-object v2, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                invoke-virtual {v0, v1, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v1, "willRenew"
                sget-object v2, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                invoke-virtual {v0, v1, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v1, "periodType"
                const-string v2, "NORMAL"
                invoke-virtual {v0, v1, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v1, "latestPurchaseDate"
                const-string v2, "2024-01-01T00:00:00Z"
                invoke-virtual {v0, v1, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v1, "originalPurchaseDate"
                const-string v2, "2024-01-01T00:00:00Z"
                invoke-virtual {v0, v1, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v1, "expirationDate"
                const-string v2, "2099-01-01T00:00:00Z"
                invoke-virtual {v0, v1, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v1, "productIdentifier"
                const-string v2, "yearly_subscription"
                invoke-virtual {v0, v1, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v1, "isSandbox"
                sget-object v2, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                invoke-virtual {v0, v1, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v1, "store"
                const-string v2, "PLAY_STORE"
                invoke-virtual {v0, v1, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v1, "ownershipType"
                const-string v2, "PURCHASED"
                invoke-virtual {v0, v1, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v1, "verification"
                const-string v2, "NOT_REQUESTED"
                invoke-virtual {v0, v1, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                new-instance v3, Ljava/util/HashMap;
                invoke-direct {v3}, Ljava/util/HashMap;-><init>()V
                const-string v1, "pro"
                invoke-virtual {v3, v1, v0}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
                const-string v1, "premium"
                invoke-virtual {v3, v1, v0}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
                const-string v1, "plus"
                invoke-virtual {v3, v1, v0}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                new-instance v4, Ljava/util/HashMap;
                invoke-direct {v4}, Ljava/util/HashMap;-><init>()V
                const-string v1, "all"
                invoke-virtual {v4, v1, v3}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
                const-string v1, "active"
                invoke-virtual {v4, v1, v3}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
                const-string v1, "verification"
                const-string v2, "NOT_REQUESTED"
                invoke-virtual {v4, v1, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                return-object v4
            """,
        )
    }
}
