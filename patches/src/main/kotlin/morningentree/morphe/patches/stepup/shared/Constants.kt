package morningentree.morphe.patches.stepup.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Step Up",
        packageName = "com.thestepupapp.stepup",
        appIconColor = 0x4CAF50,
        targets = listOf(
            AppTarget("5.0.71"),
        ),
    )
}
