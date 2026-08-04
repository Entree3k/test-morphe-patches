package morningentree.morphe.patches.dumbbellworkout.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Dumbbell Workout at Home",
        packageName = "dumbbellworkout.dumbbellapp.homeworkout",
        appIconColor = 0x2DA9B3,
        targets = listOf(
            AppTarget("1.3.5"),
        ),
    )
}
