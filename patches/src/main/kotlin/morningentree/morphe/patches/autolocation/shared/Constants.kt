package morningentree.morphe.patches.autolocation.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "AutoLocation",
        packageName = "com.joaomgcd.autolocation",
        // Cosmetic only (patcher UI accent).
        appIconColor = 0x4CAF50,
        targets = listOf(
            AppTarget("1.2.6"),
        ),
    )
}
