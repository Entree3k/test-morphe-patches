package morningentree.morphe.patches.oldroll.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.oldroll.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val unlockAllCamerasPatch = bytecodePatch(
    name = "Unlock All Cameras",
    description = "Unlocks OldRoll VIP",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Force the global "is VIP / owns Pro" flag (`manager.j.r0()Z`) true. This is the SOURCE of the
        // premium state, not the per-camera leaf gate: it puts the app in the exact same state as a real
        // paying VIP (every Pro camera unlocks via `isPRO() && r0()`, and the app's own
        // resource-provisioning still runs), which avoids the launch crash caused by forcing the leaf
        // `AnalogCamera.isUnlockedCommon()` true (that reported cameras as ready before their
        // downloadable assets existed).
        VipStatusFingerprint.method.returnEarly(true)
    }
}
