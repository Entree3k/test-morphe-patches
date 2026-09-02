package morningentree.morphe.patches.droplert.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

/**
 * The app's per-`CustomerInfo` premium check, `ai3.d(CustomerInfo)` (obfuscated
 * class/method names):
 * ```smali
 * .method public final d(Lcom/revenuecat/purchases/CustomerInfo;)Z
 *     invoke-virtual {p1}, Lcom/revenuecat/purchases/CustomerInfo;->getEntitlements()...
 *     const-string v0, "premium"
 *     invoke-virtual {p1, v0}, Lcom/revenuecat/purchases/EntitlementInfos;->get(String)...
 *     if-eqz p1, :cond_0           # entitlement "premium" not present -> free
 *     invoke-virtual {p1}, Lcom/revenuecat/purchases/EntitlementInfo;->isActive()Z
 *     if-ne p1, v0, :cond_0
 *     goto :goto_0                 # -> return true
 *     :cond_0
 *     iget-boolean p0, p0, Lai3;->n:Z   # premium-override fallback field
 *     if-eqz p0, :cond_1
 *     :goto_0
 *     return v0                     # true
 *     :cond_1
 *     const/4 p0, 0x0
 *     return p0                     # false
 * .end method
 * ```
 *
 * This is the only method app-wide with signature `(CustomerInfo)Z`. It is
 * fingerprinted purely to locate the obfuscated host class `ai3` and its
 * volatile boolean "premium override" field (`n`), which the patch then forces
 * true everywhere it is read.
 *
 * The same override is checked first by the no-arg aggregate getter `ai3.e()`
 * (8 call sites across the app's view models) and by the inlined paywall/state
 * checks, so flipping its reads unlocks premium app-wide. The override is never
 * written in a stock build (defaults false), so this is a pure enable.
 *
 * Pinned entirely by RevenueCat SDK names (never obfuscated) plus the boolean
 * return and single `CustomerInfo` parameter.
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
