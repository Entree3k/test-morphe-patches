package morningentree.morphe.patches.legsworkout.ads

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.legsworkout.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Removes All ADs",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        AdClickCapFingerprint.method.returnEarly(true)
    }
}
