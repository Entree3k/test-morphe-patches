package morningentree.morphe.patches.hobi.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Hobi",
        packageName = "com.hobi.android",
        appIconColor = 0x023C69,
        targets = listOf(
            AppTarget("3.4.0"),
        ),
    )
}
