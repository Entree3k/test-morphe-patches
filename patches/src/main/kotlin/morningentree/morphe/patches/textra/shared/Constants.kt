package morningentree.morphe.patches.textra.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Textra",
        packageName = "com.textra",
        appIconColor = 0x00A0E9,
        targets = listOf(AppTarget("4.85")),
    )
}
