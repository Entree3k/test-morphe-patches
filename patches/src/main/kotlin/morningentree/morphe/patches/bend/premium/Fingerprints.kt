package morningentree.morphe.patches.bend.premium

import app.morphe.patcher.Fingerprint

// Bend (com.bowerydigital.bend) gates every premium feature through RevenueCat.
// This single method is the app-wide premium decision: it maps a RevenueCat
// CustomerInfo to the "is premium active" boolean by reading
// getEntitlements().get("premium").isActive(). Every premium consumer (the main
// orchestrators Lc82; and Lyc0;) routes through it, so overriding it unlocks
// everything regardless of the real entitlement state.
//
// It is the ONLY method in the whole app whose signature is
// (CustomerInfo, Continuation) -> Object (verified: exactly one match), which
// makes that signature a stable, R8-proof anchor. The obfuscated class/method
// names (Lbd;->e) drift every release, so they are deliberately NOT used. The
// RevenueCat type and the "premium" entitlement id are SDK/config values that
// are never obfuscated.
internal object IsPremiumEntitlementActiveFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    // Second parameter is the obfuscated Kotlin Continuation (Len1;); "L" matches
    // any object type. Arity (2) plus the CustomerInfo type is what pins it.
    parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;", "L"),
    strings = listOf("premium"),
)
