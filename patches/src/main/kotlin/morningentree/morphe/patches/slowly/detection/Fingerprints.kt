package morningentree.morphe.patches.slowly.detection

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * react-native-device-info's native emulator probe. Both the JS `isEmulator()`
 * Promise and the synchronous `isEmulatorSync()` constant funnel through here,
 * so forcing this to return false hides the emulator from every RN caller.
 */
internal object IsEmulatorSyncFingerprint : Fingerprint(
    definingClass = "Lcom/learnium/RNDeviceInfo/RNDeviceModule;",
    name = "isEmulatorSync",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * Slowly's own attestation root probe (su binaries + `test-keys` build tags).
 * Its result is bit 0 of the fraud flags baked into the HMAC-signed attestation
 * token in [Lcom/slowlyapp/AttestationModule;->generateToken].
 */
internal object AttestationLooksRootedFingerprint : Fingerprint(
    definingClass = "Lcom/slowlyapp/AttestationModule;",
    name = "looksRooted",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * Slowly's attestation hook probe (scans /proc/self/maps for `frida`/`xposed`).
 * Its result is bit 2 of the fraud flags in the attestation token.
 */
internal object AttestationLooksHookedFingerprint : Fingerprint(
    definingClass = "Lcom/slowlyapp/AttestationModule;",
    name = "looksHooked",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * Static initializer of @react-native-community/netinfo's connection-type enum.
 * Each enum constant carries a lowercase `label` that NetInfo hands to JS as
 * `state.type` ("wifi", "cellular", "vpn", ...). Relabeling the VPN constant here
 * is a single chokepoint that both the NetworkCallback and Broadcast receivers
 * funnel through.
 */
internal object ConnectionTypeClinitFingerprint : Fingerprint(
    definingClass = "Lcom/reactnativecommunity/netinfo/types/ConnectionType;",
    name = "<clinit>",
    returnType = "V",
    parameters = emptyList(),
    strings = listOf("vpn", "wifi", "cellular"),
)
