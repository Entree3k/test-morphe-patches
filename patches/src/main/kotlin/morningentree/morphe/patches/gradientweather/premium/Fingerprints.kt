package morningentree.morphe.patches.gradientweather.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * The billing repository's constructor (verified in 1.1.1 as `ot5.<init>(Context)`). It seeds the
 * tier StateFlow the UI actually observes (`ot5.f`) from local SharedPreferences:
 *   is_lifetime -> LIFETIME, else is_premium -> PREMIUM, else FREE.
 * This initial value is the effective gate on the normal screen (the `e()` setter only fires when
 * billing reports a change). Forcing the `is_lifetime` read to true makes every launch start on the
 * paid Lifetime tier.
 *
 * Anchored on the stable pref-key strings, not the obfuscated class name.
 */
internal object InitSubscriptionTierFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.CONSTRUCTOR),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("is_lifetime", "is_premium"),
)

/**
 * The subscription-tier setter (verified in 1.1.1 as `ot5.e(bo1)V`, `bo1` = { FREE, PREMIUM,
 * LIFETIME }). Every runtime tier update flows through it; forcing its argument to LIFETIME prevents
 * a billing callback from ever downgrading the tier mid-session.
 *
 * Anchored on: void return, one object param (the tier enum), and the "override_state" string. The
 * only other method with that string is `WeatherApp.onCreate()V` (no params), so this is unique.
 */
internal object SetSubscriptionTierFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("L"),
    strings = listOf("override_state"),
)
