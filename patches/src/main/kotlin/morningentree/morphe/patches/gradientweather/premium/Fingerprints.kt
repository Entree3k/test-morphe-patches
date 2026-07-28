package morningentree.morphe.patches.gradientweather.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * The billing repository's constructor (verified in 1.1.1 as `ot5.<init>(Context)V`). It seeds the
 * tier StateFlow the UI actually observes (`ot5.f`) from local SharedPreferences:
 *   is_lifetime -> LIFETIME, else is_premium -> PREMIUM, else FREE.
 * This initial value is the effective gate on the normal screen. Forcing the `is_lifetime` read to
 * true makes every launch start on the paid Lifetime tier.
 *
 * Uniquely identified by containing BOTH pref-key strings; no other method has both. We deliberately
 * do not constrain on the CONSTRUCTOR access flag (kept the match tolerant across builds).
 */
internal object InitSubscriptionTierFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("is_lifetime", "is_premium"),
)

/**
 * The subscription-tier setter (verified in 1.1.1 as `ot5.e(bo1)V`, `bo1` = { FREE, PREMIUM,
 * LIFETIME }). Every runtime tier update flows through it; forcing its argument to LIFETIME prevents
 * the startup billing callback from downgrading the constructor's initial Lifetime value.
 *
 * Anchored on: void return, the "override_state" string, and exactly one parameter (the tier enum).
 * The only other method with that string is `WeatherApp.onCreate()V`, which has zero parameters, so
 * the param-count check disambiguates without relying on the obfuscated enum type descriptor.
 */
internal object SetSubscriptionTierFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    strings = listOf("override_state"),
    custom = { method, _ -> method.parameterTypes.size == 1 },
)
