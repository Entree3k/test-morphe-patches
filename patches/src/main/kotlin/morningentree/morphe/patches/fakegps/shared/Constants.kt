package morningentree.morphe.patches.fakegps.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Fake GPS",
        packageName = "com.blogspot.newapphorizons.fakegps",
        appIconColor = 0x2196F3,
        targets = listOf(
            AppTarget(null),
            AppTarget("5.8.2"),
        ),
    )
}
