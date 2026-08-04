package morningentree.morphe.patches.prompterpal.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Prompter Pal",
        packageName = "com.solid.teleprompter",
        appIconColor = 0x1C1C1E,
        targets = listOf(
            AppTarget("7.0.1"),
        ),
    )
}
