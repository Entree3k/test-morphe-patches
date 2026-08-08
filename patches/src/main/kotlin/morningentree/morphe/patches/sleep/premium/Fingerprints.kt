package morningentree.morphe.patches.sleep.premium

import app.morphe.patcher.Fingerprint

private const val TRIAL_FILTER = "Lcom/urbandroid/sleep/TrialFilter;"
private const val PREFERENCES_UTILS = "Lcom/urbandroid/common/util/PreferencesUtils;"

/** `TrialFilter.hasUnlock()` — returns the unlocked-purchase flag every paid feature gate reads. */
internal object HasUnlockFingerprint : Fingerprint(
    name = "hasUnlock",
    returnType = "Z",
    parameters = emptyList(),
    custom = { _, classDef -> classDef.type == TRIAL_FILTER },
)

/** `PreferencesUtils.isUnlockAcked(Context)` — the stored acknowledgement of that purchase. */
internal object IsUnlockAckedFingerprint : Fingerprint(
    name = "isUnlockAcked",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    custom = { _, classDef -> classDef.type == PREFERENCES_UTILS },
)

/** `TrialFilter.isTrial()` — must be false so the app stops treating the install as a trial. */
internal object IsTrialFingerprint : Fingerprint(
    name = "isTrial",
    returnType = "Z",
    parameters = emptyList(),
    custom = { _, classDef -> classDef.type == TRIAL_FILTER },
)
