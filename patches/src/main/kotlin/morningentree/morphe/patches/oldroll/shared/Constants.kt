package morningentree.morphe.patches.oldroll.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "OldRoll",
        packageName = "com.accordion.analogcam",
        // Cosmetic only (patcher UI accent).
        appIconColor = 0xF5A623,
        targets = listOf(
            AppTarget("6.5.2"),
        ),
    )
}
