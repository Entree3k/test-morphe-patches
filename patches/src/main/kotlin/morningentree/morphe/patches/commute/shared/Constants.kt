package morningentree.morphe.patches.commute.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    // Does Not Commute is a Mediocre native game: the engine and game logic live in libcommute.so
    // plus Lua/XML assets, so most of the app is not in the DEX. The DEX is only the Android shell.
    // Premium is a single Play IAP (com.mediocre.commute.premium) brokered by the unobfuscated
    // class com.mediocre.commute.AndroidStore, which the native side queries over the
    // MainActivity.command JNI bridge, so ownership is decided from Java getters and is spoofable.
    val COMPATIBILITY = Compatibility(
        name = "Does Not Commute",
        packageName = "com.mediocre.commute",
        // Shipped as a single universal APK, not a split bundle.
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2E7D6F,
        targets = listOf(AppTarget("1.5.5")),
    )
}
