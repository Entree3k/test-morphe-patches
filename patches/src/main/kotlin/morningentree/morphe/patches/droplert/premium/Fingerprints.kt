package morningentree.morphe.patches.droplert.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

/**
 * The app's per-`CustomerInfo` premium boolean, `ai3.d(CustomerInfo)` (obfuscated
 * class/method names). Used by the view-model producer that drives the
 * `isPremium` StateFlows:
 * ```smali
 * .method public final d(Lcom/revenuecat/purchases/CustomerInfo;)Z
 *     invoke-virtual {p1}, ...CustomerInfo;->getEntitlements()...
 *     const-string v0, "premium"
 *     invoke-virtual {p1, v0}, ...EntitlementInfos;->get(String)...
 *     if-eqz p1, :cond_0
 *     invoke-virtual {p1}, ...EntitlementInfo;->isActive()Z
 *     ...
 * ```
 *
 * Only method app-wide with signature `(CustomerInfo)Z`. Forced to return true.
 * Pinned entirely by RevenueCat SDK names (never obfuscated).
 */
internal object IsPremiumForCustomerInfoFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;"),
    strings = listOf("premium"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
            name = "getEntitlements",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;",
            name = "get",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
            name = "isActive",
        ),
    ),
)

/**
 * `ai3.k(CustomerInfo, Continuation)` — the suspend RevenueCat state refresh that
 * is the *sole* producer of the app-wide premium state (`s33` carrying a `w33`
 * tier, published on the `ai3.d` StateFlow that the Compose UI collects).
 *
 * For the current customer it derives an "entitlement active" boolean and:
 * ```smali
 * invoke-virtual {v1}, ...EntitlementInfo;->isActive()Z
 * move-result v9                       # active?
 * ...
 * :goto_1
 * if-nez v9, :cond_5                   # <-- guard: only build state when active
 *     iget-boolean n ... return        # not active -> keep / defer to Play verify
 *     iget-boolean o ... return
 * :cond_5
 * if-eqz v9, :cond_6
 *     sget-object v10, Lw33;->PREMIUM  # active   -> PREMIUM
 * :cond_6
 *     sget-object v10, Lw33;->FREE     # inactive -> FREE
 * ... new Ls33(tier, expiration, willRenew, ...) ; publish to ai3.d
 * ```
 *
 * The patch forces the `active` register to 1 immediately before that guard, so
 * the refresh always publishes `s33(PREMIUM, expiration = null, ...)` — i.e.
 * lifetime premium — regardless of the real RevenueCat/Play entitlement.
 *
 * Pinned by RevenueCat SDK names, the boolean-return signature and the "premium"
 * / "TRIAL" literals; the obfuscated class/method names are never referenced.
 */
internal object RevenueCatStateRefreshFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;", "L"),
    strings = listOf("premium", "TRIAL"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;",
            name = "get",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
            name = "isActive",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
            name = "getExpirationDate",
        ),
    ),
)
