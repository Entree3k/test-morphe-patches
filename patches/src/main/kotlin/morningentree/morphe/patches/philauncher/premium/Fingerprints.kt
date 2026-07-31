package morningentree.morphe.patches.philauncher.premium

import app.morphe.patcher.Fingerprint

/**
 * The app-wide Pro gate: `Prefs.getProUser()Z`.
 *
 * It is a plain getter over the private static field `_proUser` (backed by the SharedPreferences
 * key "proUser"), so it carries no string to anchor on — hence the class+name predicate. The
 * `com.launcher.hype` package is not obfuscated in this build, so both names are stable for 3.5.1.
 *
 * Every premium feature reads this getter directly (36 call sites), and the reactive
 * `BillingViewModel._proUser` flow is (re)initialised from it, so forcing it true unlocks both the
 * synchronous checks and the observed Compose screens. See phi-launcher-premium-findings.md.
 */
internal object IsProUserFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lcom/launcher/hype/data/local/prefs/Prefs;" &&
            method.name == "getProUser"
    },
)
