package morningentree.morphe.patches.bend.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Bend",
        packageName = "com.bowerydigital.bend",
        appIconColor = 0x00BFA5,
        targets = listOf(
            AppTarget("7.6.4"),
        ),
    )
}
