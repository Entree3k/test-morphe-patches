package morningentree.morphe.patches.magiceraser.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.magiceraser.shared.Constants
import morningentree.morphe.patches.magiceraser.shared.disablePairipSignatureCheckPatch
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Magic Eraser premium (removes ads/watermark and unlocks pro tools) " +
        "by forcing the app-wide subscription gate. Use with Spoof Install Source.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    // The app is wrapped by Pairip; its SignatureCheck.verifyIntegrity throws on
    // a re-signed APK (instant launch crash), so neutralize it.
    dependsOn(disablePairipSignatureCheckPatch)

    execute {
        // utils.F0.a() is the single "is subscribed" gate read by all 13 gated
        // call sites. Forcing it true marks the user premium app-wide regardless
        // of the underlying subscription_status LiveData value.
        IsSubscribedFingerprint.method.returnEarly(true)
    }
}
