package morningentree.morphe.patches.ling.premium

import app.morphe.patcher.patch.rawResourcePatch
import morningentree.morphe.patches.all.detection.pairip.disablePairipPatch
import morningentree.morphe.patches.ling.shared.Constants
import morningentree.morphe.patches.shared.misc.hermes.hermesPatch

const val RETURN_TRUE = "78 00 5C 00"

@Suppress("unused")
val enablePremiumPatch = rawResourcePatch(
    name = "Enable Premium",
    description = "Unlocks Ling Pro"
) {
    compatibleWith(Constants.COMPATIBILITY)

    // Ling 8.9.0 newly ships Pairip. SignatureCheck.verifyIntegrity() throws
    // SignatureTamperedException in Application.attachBaseContext() on a re-signed APK —
    // crashing the app on launch before the Hermes bundle (and this patch) ever loads.
    // disablePairipPatch no-ops verifyIntegrity + the licensecheck chain so it launches.
    dependsOn(
        hermesPatch {
            val selectIsProUser =
                "6C 00 01 37 00 00 01 A0 9D 37 00 00 02 81 E6 5C 00" to RETURN_TRUE

            setOf(selectIsProUser)
        },
        disablePairipPatch,
    )
}
