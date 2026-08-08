package morningentree.morphe.patches.photon.premium

import app.morphe.patcher.Fingerprint

/**
 * The BillingManager's `queryPurchasesAsync` callback. On launch it walks the owned purchases,
 * decides whether premium is owned and writes that boolean into the manager's premium
 * `MutableStateFlow` — the single value every VIP/Pro gate in the app collects. Forcing it to
 * report owned makes the whole app read as premium.
 *
 * Everything about the class is obfuscated, but the log line it builds ("Purchases queried: ") is
 * unique to this method, so it anchors without a single obfuscated name.
 */
internal object QueryPurchasesResultFingerprint : Fingerprint(
    returnType = "V",
    strings = listOf("Purchases queried: ", "acknowledged"),
)
