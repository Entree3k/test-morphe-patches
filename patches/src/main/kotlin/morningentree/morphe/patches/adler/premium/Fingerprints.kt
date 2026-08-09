package morningentree.morphe.patches.adler.premium

import app.morphe.patcher.Fingerprint

/**
 * `com.splendapps.adler.AdlerApp.d()` — the app-wide "should show ads" gate.
 *
 * It reads the Monetizer state (an int field on the settings object, persisted
 * under the key "MonetizerAdsMode"):
 *   value == 1  -> ads removed (premium)  -> d() returns false
 *   otherwise   -> free user              -> d() returns true (show ads)
 *
 * Every ad path routes through this method — the interstitial gate, banner /
 * native ad loading, and MainActivity — so forcing it to return false disables
 * all ads, which is the app's only paid feature (IAP `iap_adler_remove_ads`).
 *
 * Pinned by the stable application class type plus the concrete boolean
 * override; the obfuscated settings class is intentionally not referenced.
 */
internal object ShouldShowAdsFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lcom/splendapps/adler/AdlerApp;" && method.name == "d"
    },
)
