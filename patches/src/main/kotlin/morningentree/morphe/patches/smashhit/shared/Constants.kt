package morningentree.morphe.patches.smashhit.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    // Smash Hit is a Mediocre native game. The DEX is only the Android shell; the C++ engine talks
    // to it through the unobfuscated com.mediocre.smashhit.CommandHandler JNI command table, which
    // is where premium ownership is answered.
    val COMPATIBILITY = Compatibility(
        name = "Smash Hit",
        packageName = "com.mediocre.smashhit",
        // Shipped as a single universal APK, not a split bundle.
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1E88E5,
        targets = listOf(AppTarget("1.5.14")),
    )
}
