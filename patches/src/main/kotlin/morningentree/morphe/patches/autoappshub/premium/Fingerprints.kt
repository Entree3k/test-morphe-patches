package morningentree.morphe.patches.autoappshub.premium

import app.morphe.patcher.Fingerprint

internal object SubscriptionUnlockFingerprint : Fingerprint(
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf("Ljava/lang/String;"),
    strings = listOf("fullsub", "fullsubextra", "fullsubyearly"),
)

internal object AutoAppsLicensedFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("com.joaomgcd.autoapps.EXTRA_IS_LICENSED"),
)
