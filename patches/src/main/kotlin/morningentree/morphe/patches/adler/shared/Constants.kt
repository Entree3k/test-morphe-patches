package morningentree.morphe.patches.adler.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Notepad",
        packageName = "com.splendapps.adler",
        appIconColor = 0x1E88E5,
        targets = listOf(
            AppTarget("3.0.8"),
        ),
    )
}
