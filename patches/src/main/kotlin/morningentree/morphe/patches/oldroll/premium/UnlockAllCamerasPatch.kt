package morningentree.morphe.patches.oldroll.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.oldroll.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val unlockAllCamerasPatch = bytecodePatch(
    name = "Unlock All Cameras",
    description = "Unlocks every OldRoll camera by forcing the AnalogCamera unlock gate on.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // `AnalogCamera.isUnlockedCommon()Z` is the base of the whole unlock family
        // (isUnlockedWithoutFreeUse -> isUnlocked -> isUnlockedAndCanUse / isUnlockedWithBFreeUse,
        // plus isUnlockedWithoutCaptureDcrUnlock all call into it). Returning true makes every
        // camera report unlocked and usable, regardless of VIP/purchase state.
        IsCameraUnlockedFingerprint.method.returnEarly(true)
    }
}
