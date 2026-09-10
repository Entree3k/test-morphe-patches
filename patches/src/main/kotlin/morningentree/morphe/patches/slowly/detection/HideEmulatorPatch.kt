package morningentree.morphe.patches.slowly.detection

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.slowly.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val slowlyHideEmulatorPatch = bytecodePatch(
    name = "Slowly hide emulator",
    description = "Reports the device as a physical device by forcing react-native-device-info's " +
        "isEmulator/isEmulatorSync to return false, so Slowly no longer detects that it is running " +
        "in an emulator or simulated device.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        IsEmulatorSyncFingerprint.method.returnEarly(false)
    }
}
