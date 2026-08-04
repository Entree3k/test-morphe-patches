package morningentree.morphe.patches.sixpack.premium

import app.morphe.patcher.Fingerprint

/**
 * Anchors the obfuscated billing helper (v1.4.8: `Lgq/g0;`), the class that decides
 * remove-ads / premium state.
 *
 * It exposes three premium gates that feature and ad-gate sites call directly:
 *   - `g(Landroid/content/Context;)Z` — the master aggregate (matched here),
 *   - `k(Landroid/content/Context;)Z` — the cached "remove_ads" preference,
 *   - `h()Z` — whether any premium subscription SKU is owned.
 * (`f(Ljava/lang/String;)Z` in the same class is a free-trial-offer pricing check, not a gate,
 * and is intentionally left alone.)
 *
 * [enablePremiumPatch] reaches all three by enumerating the class, so we only need to land on
 * one member to recover the (obfuscated) class type. We match the master gate by its stable,
 * unencrypted remove-ads SKU string plus its exact signature — never by the obfuscated
 * class/method names, which drift between releases.
 */
internal object RemoveAdsGateFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("sixpack.sixpackabs.absworkout.removeads"),
)
