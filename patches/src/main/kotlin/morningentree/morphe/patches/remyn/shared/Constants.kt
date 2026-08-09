package morningentree.morphe.patches.remyn.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Remyn",
        packageName = "com.mahersafadi.remyn",
        appIconColor = 0x7C4DFF,
        targets = listOf(
            AppTarget("1.7.3"),
        ),
    )
}
