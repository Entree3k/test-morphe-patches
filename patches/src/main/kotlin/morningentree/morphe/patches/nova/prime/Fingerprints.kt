package morningentree.morphe.patches.nova.prime

import app.morphe.patcher.Fingerprint

/**
 * Nova's license-state initializer (verified in 8.8.6 as `Lny/h2;->a(SharedPreferences)V`, a
 * `SharedPreferences$OnSharedPreferenceChangeListener`). It reads the license level from the pref key
 * "1" via `getInt("1", 0)`, then — in its own code — sets `field c` = "isPrime" via `level == 0x200`
 * and `field h` = "unlocked" via `level != 0`. So `0x200` is Nova's Prime value (the app itself
 * compares against it), and both flags are computed **only** here and read from other classes.
 *
 * Anchored purely on two strings that are unique to this method: the reflection literal
 * "android.os.SystemProperties" and the Nova pref key "widget_reset_ids". A string-only fingerprint is
 * used (rather than the ordered `filters`/`instructionMatches` DSL the 8.5.1 patch used) because Nova
 * now bundles many SDKs that also reference "android.os.SystemProperties"; the pair pins this to Nova's
 * own license method, and the patch then locates the `getInt` result by scanning instructions.
 */
object SetPrimeFromPreferencesFingerprint : Fingerprint(
    strings = listOf("android.os.SystemProperties", "widget_reset_ids"),
)

/**
 * Nova's license-check entry point (verified in 8.8.6 as `Lvu/y0;->c(Landroid/content/Context;)V`).
 * This is the ONLY place the async licensing subsystem is created: it builds the LVL
 * `LicenseChecker`s (`Lv00/f;` / `Lv00/i;`) and the key-app signature-verifier runnable (`Lvu/x0;`,
 * which compares the installed `com.teslacoilsw.launcher.prime` signing-cert hashCode against Tesla's
 * hardcoded values). On a failed check the callbacks flip `Lny/h2;->h` (the Prime gate, read in 50+
 * places) back to false and schedule the `aa/p` watchdog that revokes Prime — which is why forcing the
 * startup state in `a()` alone did NOT stick.
 *
 * Neutering this method (grant the in-memory flags, then return before any checker/watchdog spawns)
 * removes every async revocation path. Names are obfuscated and drift per release, so this is anchored
 * with a class+name custom predicate and is therefore **version-specific to 8.8.6 (88600)** — the only
 * target declared in Constants. Re-verify against `Lvu/y0;` for any new version before adding targets.
 */
object LicenseCheckEntryFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    custom = { method, classDef ->
        classDef.type == "Lvu/y0;" && method.name == "c"
    },
)
