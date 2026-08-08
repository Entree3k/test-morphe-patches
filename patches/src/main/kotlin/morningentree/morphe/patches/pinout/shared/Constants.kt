package morningentree.morphe.patches.pinout.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    // PinOut uses the same Mediocre Android shell as Does Not Commute: the game itself is native,
    // and premium is a single Play IAP (com.mediocre.pinout.premium) brokered by the unobfuscated
    // com.mediocre.pinout.AndroidStore, which the native side queries over the
    // MainActivity.command JNI bridge.
    val COMPATIBILITY = Compatibility(
        name = "PinOut",
        packageName = "com.mediocre.pinout",
        // Shipped as a single universal APK, not a split bundle.
        apkFileType = ApkFileType.APK,
        appIconColor = 0xE91E63,
        targets = listOf(AppTarget("1.0.7")),
    )
}
