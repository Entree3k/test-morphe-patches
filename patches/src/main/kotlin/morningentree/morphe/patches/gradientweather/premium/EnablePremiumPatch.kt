package morningentree.morphe.patches.gradientweather.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import morningentree.morphe.patches.all.detection.pairip.disablePairipPatch
import morningentree.morphe.patches.gradientweather.shared.Constants
import morningentree.morphe.util.getReference
import java.util.logging.Logger

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium (dev)",
    description = "Unlocks Gradient Weather Premium. Use With Spoof Install Source",
) {
    compatibleWith(Constants.COMPATIBILITY)

    dependsOn(disablePairipPatch)

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        val setter = SetSubscriptionTierFingerprint.method
        val managerType = setter.definingClass
        val tierEnumType = setter.parameterTypes.first()

        var lifetimeField: FieldReference? = null
        classDefForEach { classDef ->
            if (classDef.type != tierEnumType) return@classDefForEach
            for (method in classDef.methods) {
                if (method.name != "<clinit>") continue
                val insns = method.instructionsOrNull?.toList() ?: continue
                val nameIndex = insns.indexOfFirst {
                    it.getReference<StringReference>()?.string == "LIFETIME"
                }
                if (nameIndex < 0) continue
                lifetimeField = ((nameIndex + 1) until insns.size).firstNotNullOfOrNull { i ->
                    val insn = insns[i]
                    if (insn.opcode == Opcode.SPUT_OBJECT &&
                        insn.getReference<FieldReference>()?.type == tierEnumType
                    ) {
                        insn.getReference<FieldReference>()
                    } else {
                        null
                    }
                }
                break
            }
        }
        val lifetime = lifetimeField
            ?: throw PatchException("Gradient Weather: could not resolve the LIFETIME tier constant.")
        val lifetimeRef = "${lifetime.definingClass}->${lifetime.name}:${lifetime.type}"

        setter.addInstructions(0, "sget-object p1, $lifetimeRef")
        logger.info("Gradient Weather: tier setter pinned to LIFETIME.")

        var seeded = false
        classDefForEach { classDef ->
            if (seeded || classDef.type != managerType) return@classDefForEach
            for (method in mutableClassDefBy(classDef).methods) {
                val insns = method.instructionsOrNull?.toList() ?: continue
                val stringIndex = insns.indexOfFirst {
                    it.getReference<StringReference>()?.string == "is_lifetime"
                }
                if (stringIndex < 0) continue
                val moveResultIndex = ((stringIndex + 1) until insns.size).firstOrNull {
                    insns[it].opcode == Opcode.MOVE_RESULT
                } ?: continue
                val register = (insns[moveResultIndex] as OneRegisterInstruction).registerA
                method.addInstruction(moveResultIndex + 1, "const/4 v$register, 0x1")
                seeded = true
                break
            }
        }

        if (seeded) {
            logger.info("Gradient Weather: initial tier seeded to LIFETIME.")
        } else {
            logger.warning("Gradient Weather: is_lifetime seed not found; relying on setter pin only.")
        }
    }
}
