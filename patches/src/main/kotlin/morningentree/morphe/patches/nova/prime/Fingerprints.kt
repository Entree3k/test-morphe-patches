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
