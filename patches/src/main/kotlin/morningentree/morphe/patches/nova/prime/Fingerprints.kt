package morningentree.morphe.patches.nova.prime

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode

/**
 * Nova's license-state initializer (verified in 8.8.6 as `Lny/h2;->a(SharedPreferences)V`, a
 * `SharedPreferences$OnSharedPreferenceChangeListener`). It reads the license level from the pref key
 * "1" via `getInt("1", 0)`, then derives the app-wide flags: `field h` = "unlocked" (`level != 0`) and
 * `field c` = "isPrime" (`level == 0x200`). So `0x200` is Nova's Prime license value.
 *
 * We force the `getInt("1", 0)` result to `0x200`, exactly as the original hoodles patch did for 8.5.1 —
 * the method shape is unchanged in 8.8.6.
 *
 * Anchored on:
 *  - the ordered instruction sequence `getInt("1", 0); move-result` (the filters), and
 *  - two strings unique to this method: the reflection literal "android.os.SystemProperties" and the
 *    Nova pref key "widget_reset_ids". Nova is heavily R8-obfuscated and now bundles many ad/analytics
 *    SDKs that also reference "android.os.SystemProperties", so the "widget_reset_ids" string is what
 *    disambiguates this to Nova's own license method.
 */
object SetPrimeFromPreferencesFingerprint : Fingerprint(
    strings = listOf("android.os.SystemProperties", "widget_reset_ids"),
    filters = listOf(
        string("1"),
        literal(0),
        methodCall(name = "getInt"),
        opcode(Opcode.MOVE_RESULT),
    ),
)
