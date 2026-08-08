package morningentree.morphe.patches.automate.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Automate",
        packageName = "com.llamalab.automate",
        // Shipped as a single universal APK, not a split bundle.
        apkFileType = ApkFileType.APK,
        appIconColor = 0x7CB342,
        targets = listOf(AppTarget("1.51.1")),
    )
}
