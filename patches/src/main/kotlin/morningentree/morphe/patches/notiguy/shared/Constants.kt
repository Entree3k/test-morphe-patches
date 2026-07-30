package morningentree.morphe.patches.notiguy.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "NotiGuy",
        packageName = "com.dynamic.notifications",
        appIconColor = 0xFF6D00,
        // Detection is shape-based (no hardcoded obfuscated names), so the same patch applies
        // across releases. 2.7.8 (versionCode 278) verified: premium flag `SaStyle.D2` is read via
        // the obfuscated `(object, "premium", Z)Z` wrapper the patch's case B matches.
        targets = listOf(
            AppTarget("2.7.6"),
            AppTarget("2.7.8"),
        ),
    )
}
