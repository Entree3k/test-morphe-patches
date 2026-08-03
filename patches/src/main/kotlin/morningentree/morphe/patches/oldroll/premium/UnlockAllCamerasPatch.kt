package morningentree.morphe.patches.oldroll.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.oldroll.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val unlockAllCamerasPatch = bytecodePatch(
    name = "Unlock All Cameras",
    description = "Unlocks every OldRoll camera and disables the modified-app (anti-piracy) check " +
        "that otherwise closes the re-signed build on launch.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // 1) Defuse the anti-piracy check FIRST — it trips on any re-signed APK and closes the app on
        //    launch (the "your version has been cracked" popup), before any camera UI is reached.
        //    Force the verdict to "genuine" and no-op the popup scheduler as a safety net.
        PiracyVerdictFingerprint.method.returnEarly(false)
        PiracyPopupSchedulerFingerprint.method.addInstructions(0, "return-void")

        // 2) Unlock every camera by forcing the base per-camera gate true. This is the confirmed,
        //    readable funnel for the whole isUnlocked* family.
        IsCameraUnlockedFingerprint.method.returnEarly(true)
    }
}
