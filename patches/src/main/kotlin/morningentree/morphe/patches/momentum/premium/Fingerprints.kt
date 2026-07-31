package morningentree.morphe.patches.momentum.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

/**
 * Momentum's RevenueCat premium repository method (obfuscated `cy.b`). It fetches the
 * `CustomerInfo`, reads `entitlements.getAll().get("plus").isActive()` and returns a sealed result
 * (active / not / error). The sibling boolean gate (`cy.a`) calls this and returns
 * `result instanceof <active>` — that is what the app's premium checks consume.
 *
 * Anchored on the never-obfuscated RevenueCat SDK calls + the stable entitlement id "plus" (which
 * appears in exactly one method in the whole app). Filters are in instruction order.
 *
 * See phi-launcher-premium-findings.md / momentum notes.
 */
internal object IsPlusEntitlementFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("L"),
    filters = listOf(
        methodCall(definingClass = "Lcom/revenuecat/purchases/CustomerInfo;", name = "getEntitlements"),
        methodCall(definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;", name = "getAll"),
        string("plus"),
        methodCall(definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;", name = "isActive"),
    ),
)
