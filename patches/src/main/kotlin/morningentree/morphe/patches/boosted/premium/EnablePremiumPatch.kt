package morningentree.morphe.patches.boosted.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import morningentree.morphe.patches.boosted.shared.Constants
import morningentree.morphe.util.getReference

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Boosted Productivity Premium (forces the app-wide premium flag on).",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // The whole app reads one relay (`w3.d.c`) for "is premium active". Force it true at both
        // ends: the initial seed value (before any billing emission) and the value the writer pushes
        // on every emission. Either alone leaves a gap; together the relay is true from launch and
        // can never be downgraded.

        // End 1 — the constructor seeds the relay with Boolean.FALSE. Flip that seed to TRUE so the
        // very first read (before the purchase observable emits) already reports premium.
        PremiumStateInitFingerprint.method.apply {
            val insns = instructions.toList()
            val falseIndex = insns.indexOfFirst {
                val ref = it.getReference<FieldReference>()
                ref?.definingClass == "Ljava/lang/Boolean;" && ref.name == "FALSE"
            }
            if (falseIndex < 0) {
                throw PatchException("Could not find the Boolean.FALSE relay seed in the constructor.")
            }
            val register = (insns[falseIndex] as OneRegisterInstruction).registerA
            replaceInstruction(
                falseIndex,
                "sget-object v$register, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
            )
        }

        // End 2 — the writer computes a boolean (does any purchase match a premium SKU?), boxes it
        // via Boolean.valueOf(Z) and pushes it into the relay. Force valueOf's input register to 1
        // so every emission pushes TRUE, regardless of what's actually owned.
        PremiumStateWriteFingerprint.method.apply {
            val insns = instructions.toList()
            val valueOfIndex = insns.indexOfFirst {
                val ref = it.getReference<MethodReference>()
                ref?.definingClass == "Ljava/lang/Boolean;" &&
                    ref.name == "valueOf" &&
                    ref.returnType == "Ljava/lang/Boolean;"
            }
            if (valueOfIndex < 0) {
                throw PatchException("Could not find the Boolean.valueOf boxing before the relay push.")
            }
            // invoke-static {vX}, Boolean->valueOf(Z)Ljava/lang/Boolean; — the single arg is registerC.
            val argRegister = (insns[valueOfIndex] as FiveRegisterInstruction).registerC
            addInstruction(valueOfIndex, "const/4 v$argRegister, 0x1")
        }
    }
}
