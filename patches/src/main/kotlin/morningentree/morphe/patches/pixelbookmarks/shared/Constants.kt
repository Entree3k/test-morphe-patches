package morningentree.morphe.patches.pixelbookmarks.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Pixel Bookmarks",
        packageName = "com.psh.pixel_bookmarks",
        appIconColor = 0xEC3D42,
        targets = listOf(
            AppTarget("2.3.7"),
        ),
    )
}
