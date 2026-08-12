package morningentree.morphe.patches.tide.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Tide",
        packageName = "io.moreless.tide",
        appIconColor = 0x159E9C,
        targets = listOf(
            AppTarget(null),
            AppTarget("5.6.2"),
        ),
    )
}
