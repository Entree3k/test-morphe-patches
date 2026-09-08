package morningentree.morphe.patches.gradientweather.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
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
    name = "Enable Premium",
    description = "Unlocks Gradient Weather Premium. Use With Spoof Install Source",
) {
    compatibleWith(Constants.COMPATIBILITY)

    dependsOn(disablePairipPatch)

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        InitSubscriptionTierFingerprint.method.apply {
            val insns = instructions.toList()
            val stringIndex = insns.indexOfFirst {
                it.getReference<StringReference>()?.string == "is_lifetime"
            }
            if (stringIndex < 0) {
                throw PatchException("Could not find the is_lifetime pref read in the constructor.")
            }
            val moveResultIndex = ((stringIndex + 1) until insns.size).firstOrNull {
                insns[it].opcode == Opcode.MOVE_RESULT
            } ?: throw PatchException("Could not find the is_lifetime getBoolean result.")
            val register = (insns[moveResultIndex] as OneRegisterInstruction).registerA
            addInstruction(moveResultIndex + 1, "const/4 v$register, 0x1")
        }

        val setter = SetSubscriptionTierFingerprint.method
        val tierEnumType = setter.parameterTypes.first()

        var lifetimeField: FieldReference? = null
        classDefForEach { classDef ->
            if (classDef.type != tierEnumType) return@classDefForEach
            for (method in mutableClassDefBy(classDef).methods) {
                if (method.name != "<clinit>") continue
                val insns = method.instructionsOrNull?.toList() ?: continue
                val nameIndex = insns.indexOfFirst {
                    it.getReference<StringReference>()?.string == "LIFETIME"
                }
                if (nameIndex < 0) continue
                val sputIndex = ((nameIndex + 1) until insns.size).firstOrNull {
                    insns[it].opcode == Opcode.SPUT_OBJECT &&
                        insns[it].getReference<FieldReference>()?.type == tierEnumType
                } ?: continue
                lifetimeField = insns[sputIndex].getReference<FieldReference>()
                break
            }
        }

        val ref = lifetimeField
            ?: throw PatchException("Could not resolve the LIFETIME tier constant.")
        val smaliReference = "${ref.definingClass}->${ref.name}:${ref.type}"
        setter.addInstructions(0, "sget-object p1, $smaliReference")
        logger.info("Gradient Weather: tier seeded and setter pinned to LIFETIME.")
    }
}
