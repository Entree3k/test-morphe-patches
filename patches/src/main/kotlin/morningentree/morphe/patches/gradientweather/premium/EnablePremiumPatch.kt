package morningentree.morphe.patches.gradientweather.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import morningentree.morphe.patches.gradientweather.shared.Constants
import morningentree.morphe.util.getReference

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Gradient Weather Premium (forces the paid Lifetime tier).",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Primary gate: the constructor seeds the tier StateFlow the UI reads from the
        // "is_lifetime"/"is_premium" prefs. Force the is_lifetime read to true so every launch
        // starts on LIFETIME regardless of what's stored.
        InitSubscriptionTierFingerprint.method.apply {
            val insns = instructions.toList()
            val stringIndex = insns.indexOfFirst {
                it.getReference<StringReference>()?.string == "is_lifetime"
            }
            if (stringIndex < 0) {
                throw PatchException("Could not find the is_lifetime pref read in the constructor.")
            }
            // The boolean result of getBoolean("is_lifetime", false) lands in the next move-result.
            val moveResultIndex = (stringIndex + 1 until insns.size).first {
                insns[it].opcode == Opcode.MOVE_RESULT
            }
            val register = (insns[moveResultIndex] as OneRegisterInstruction).registerA
            addInstruction(moveResultIndex + 1, "const/4 v$register, 0x1")
        }

        // Secondary: force the runtime tier setter to LIFETIME so a billing callback can't
        // downgrade the tier mid-session. Reuse the LIFETIME field reference the method already
        // loads, so no obfuscated enum/class name is hardcoded.
        SetSubscriptionTierFingerprint.method.apply {
            val lifetimeReference = instructions
                .firstNotNullOfOrNull {
                    it.getReference<FieldReference>()?.takeIf { ref -> ref.name == "LIFETIME" }
                }
                ?: throw PatchException("Could not find the LIFETIME tier constant in the setter.")

            val smaliReference =
                "${lifetimeReference.definingClass}->${lifetimeReference.name}:${lifetimeReference.type}"

            addInstructions(0, "sget-object p1, $smaliReference")
        }
    }
}
