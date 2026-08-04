package morningentree.morphe.patches.sixpack.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Six Pack in 30 Days",
        packageName = "sixpack.sixpackabs.absworkout",
        // Cosmetic only (patcher UI accent).
        appIconColor = 0xFF6D00,
        targets = listOf(
            AppTarget("1.4.8"),
        ),
    )
}
