package morningentree.morphe.patches.habitica.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Habitica",
        packageName = "com.habitrpg.android.habitica",
        appIconColor = 0x5A2CC8,
        targets = listOf(
            AppTarget(null),
            AppTarget("4.10.3"),
        ),
    )
}
