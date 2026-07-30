package morningentree.morphe.patches.autoappshub.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "AutoAppsHub",
        packageName = "com.joaomgcd.autoappshub",
        appIconColor = 0x1BA8C4,
        targets = listOf(
            AppTarget("1.8.13"),
        ),
    )
}
