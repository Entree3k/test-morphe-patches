package morningentree.morphe.patches.dynamicspot.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "DynamicSpot",
        packageName = "com.jamworks.dynamicspot",
        appIconColor = 0x00BCD4,
        targets = listOf(
            AppTarget("2.01"),
        ),
    )
}
