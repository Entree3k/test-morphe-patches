package morningentree.morphe.patches.legsworkout.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Legs Workout - Women Legs Training",
        packageName = "legsworkout.slimlegs.fatburning.stronglegs",
        appIconColor = 0xFD6376,
        targets = listOf(
            AppTarget("1.2.2"),
        ),
    )
}
