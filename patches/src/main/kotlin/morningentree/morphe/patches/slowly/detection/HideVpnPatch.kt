package morningentree.morphe.patches.slowly.detection

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import morningentree.morphe.patches.slowly.shared.Constants
import morningentree.morphe.util.getReference

@Suppress("unused")
val slowlyHideVpnPatch = bytecodePatch(
    name = "Slowly hide VPN",
    description = "Reports an active VPN as an ordinary Wi-Fi connection by relabeling the " +
        "@react-native-community/netinfo VPN connection type, so Slowly's client-side VPN warning " +
        "(\"Your connection appears to be using a VPN\") never triggers. Server-side IP geolocation " +
        "checks (REG_GEO_USING_VPN) are enforced on the server and are not affected.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val clinit = ConnectionTypeClinitFingerprint.method
        val clinitInstructions = clinit.instructions.toList()

        // The lowercase enum label "vpn" is the string NetInfo surfaces as state.type.
        // (The uppercase "VPN" enum name is left intact so valueOf() still resolves.)
        val vpnLabelIndex = clinitInstructions.indexOfFirst {
            it.opcode == Opcode.CONST_STRING &&
                it.getReference<StringReference>()?.string == "vpn"
        }
        if (vpnLabelIndex < 0) throw PatchException("Could not locate the NetInfo \"vpn\" connection-type label")

        val register = (clinitInstructions[vpnLabelIndex] as OneRegisterInstruction).registerA
        clinit.replaceInstruction(vpnLabelIndex, "const-string v$register, \"wifi\"")
    }
}
