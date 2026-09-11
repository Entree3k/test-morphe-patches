package morningentree.morphe.patches.bottled.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Bottled",
        packageName = "com.bottledapp.bottled",
        appIconColor = 0x00BCD4,
        targets = listOf(
            AppTarget("2.15.6"),
        ),
    )
}
