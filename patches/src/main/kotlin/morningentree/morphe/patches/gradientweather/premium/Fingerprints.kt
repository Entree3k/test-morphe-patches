package morningentree.morphe.patches.gradientweather.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * The subscription-tier setter in the (obfuscated) billing repository — verified in 1.1.1 as
 * `ot5.e(bo1)V`, where `bo1` is the user-tier enum { FREE, PREMIUM, LIFETIME }. Every tier update
 * (including the startup Play-Billing query result, dispatched via the purchase callback) flows
 * through this single method: it stores the tier into a field and publishes it to the StateFlows
 * the UI observes, persisting it under the "override_state" preference.
 *
 * Anchored on stable characteristics, not obfuscated names:
 *  - returns void, takes exactly one object parameter (the tier enum),
 *  - contains the literal "override_state".
 * The only other method containing that string is `WeatherApp.onCreate()V`, which takes no
 * parameters, so this fingerprint resolves uniquely.
 */
internal object SetSubscriptionTierFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("L"),
    strings = listOf("override_state"),
)
