package morningentree.morphe.patches.pinnit.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import morningentree.morphe.patches.pinnit.shared.Constants
import morningentree.morphe.util.getReference

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Pinnit Pro (all features).",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        PurchaseStatusFingerprint.method.apply {
            val purchasedStatus = instructions
                .firstNotNullOfOrNull { insn ->
                    if (insn.opcode == Opcode.SGET_OBJECT) insn.getReference<FieldReference>() else null
                }
                ?: throw PatchException("Could not find the purchased-status field in the mapper.")

            val smaliReference =
                "${purchasedStatus.definingClass}->${purchasedStatus.name}:${purchasedStatus.type}"

            addInstructions(0, "sget-object v0, $smaliReference\nreturn-object v0")
        }
    }
}
