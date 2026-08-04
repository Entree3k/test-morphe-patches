package morningentree.morphe.patches.legsworkout.ads

import app.morphe.patcher.Fingerprint

internal object AdClickCapFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("last_start_click_ad_time", "have_click_ad_times"),
)
