package morningentree.morphe.patches.magiceraser.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Magic Eraser",
        packageName = "com.duygiangdg.magiceraser",
        appIconColor = 0x7C4DFF,
        targets = listOf(
            AppTarget("3.3.9"),
        ),
    )
}
