package morningentree.morphe.patches.notiguy.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "NotiGuy",
        packageName = "com.dynamic.notifications",
        appIconColor = 0xFF6D00,
        targets = listOf(
            AppTarget("2.7.6"),
        ),
    )
}
