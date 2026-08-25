package morningentree.morphe.patches.slowly.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Slowly",
        packageName = "com.slowlyapp",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF5A5F,
        targets = listOf(
            AppTarget("9.5.6"),
            AppTarget("9.5.8"),
        ),
    )

    val COMPATIBILITY_AVATAR = Compatibility(
        name = "Slowly",
        packageName = "com.slowlyapp",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF5A5F,
        targets = listOf(
            AppTarget("9.5.6"),
            AppTarget("9.5.8"),
        ),
    )

    val COMPATIBILITY_PAIRIP = Compatibility(
        name = "Slowly",
        packageName = "com.slowlyapp",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF5A5F,
        targets = listOf(
            AppTarget("9.5.6"),
            AppTarget("9.5.8"),
        ),
    )
}
