package morningentree.morphe.patches.dhgate.updates

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.dhgate.shared.Constants.COMPATIBILITY_DHGATE

internal object IsForceUpdateFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lcom/dhgate/buyermob/data/model/VersionInfoDto;" &&
                method.name == "isForceUpdate"
    },
)

val disableForcedUpdatesPatch = bytecodePatch(
    name = "Disable forced updates",
    description = "Makes app updates optional by disabling the forced-update flag.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_DHGATE)

    execute {
        IsForceUpdateFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
        )
    }
}
