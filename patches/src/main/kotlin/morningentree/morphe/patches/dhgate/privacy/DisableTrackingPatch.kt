package morningentree.morphe.patches.dhgate.privacy

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.dhgate.shared.Constants.COMPATIBILITY_DHGATE

internal object TrackSendBatchedFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Lcom/dhgate/buyermob/data/model/track/TrackEvent;", "Z"),
    custom = { _, classDef -> classDef.type == "Lcom/dhgate/buyermob/utils/TrackingUtil;" },
)

internal object TrackSendImmediateFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Lcom/dhgate/buyermob/data/model/track/TrackEvent;"),
    custom = { _, classDef -> classDef.type == "Lcom/dhgate/buyermob/utils/TrackingUtil;" },
)

val disableTrackingPatch = bytecodePatch(
    name = "Disable tracking",
    description = "Blocks DHgate's native analytics/tracking beacons (tracklog.jsp).",
    default = true,
) {
    compatibleWith(COMPATIBILITY_DHGATE)

    execute {
        TrackSendBatchedFingerprint.method.addInstructions(0, "return-void")
        TrackSendImmediateFingerprint.method.addInstructions(0, "return-void")
    }
}
