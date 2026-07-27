package morningentree.morphe.patches.gradientweather.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import morningentree.morphe.patches.gradientweather.shared.Constants
import morningentree.morphe.util.getReference

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Gradient Weather Premium (forces the paid Lifetime tier).",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        SetSubscriptionTierFingerprint.method.apply {
            // Reuse the LIFETIME enum-constant reference the method already loads, so we never
            // hardcode the obfuscated enum/class names (which change every release). Enum constant
            // names are preserved by R8, so "LIFETIME" is a stable anchor.
            val lifetimeReference = instructions
                .firstNotNullOfOrNull { it.getReference<FieldReference>()?.takeIf { ref -> ref.name == "LIFETIME" } }
                ?: throw PatchException("Could not find the LIFETIME tier constant in the setter.")

            val smaliReference =
                "${lifetimeReference.definingClass}->${lifetimeReference.name}:${lifetimeReference.type}"

            // Force the incoming tier argument (p1) to LIFETIME before it is stored and published,
            // so every tier update — including the startup billing result — reports the paid tier.
            addInstructions(0, "sget-object p1, $smaliReference")
        }
    }
}
