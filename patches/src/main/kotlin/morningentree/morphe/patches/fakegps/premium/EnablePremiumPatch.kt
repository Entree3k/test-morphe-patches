package morningentree.morphe.patches.fakegps.premium

import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import morningentree.morphe.patches.fakegps.shared.Constants
import morningentree.morphe.util.getReference
import morningentree.morphe.util.returnEarly
import java.util.logging.Logger

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Fake GPS Pro",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val logger = Logger.getLogger(this::class.java.name)
        val proMarkers = setOf("fake_gps_pro", "pref_key_latitude_2")
        val mainActivityType = "Lcom/blogspot/newapphorizons/fakegps/MainActivity;"

        var gettersForced = 0
        classDefForEach { classDef ->
            val hasProGetter = classDef.methods.any { method ->
                method.returnType == "Z" &&
                    method.parameterTypes.isEmpty() &&
                    method.instructionsOrNull?.any {
                        it.getReference<StringReference>()?.string?.let { s -> s in proMarkers } == true
                    } == true
            }
            if (!hasProGetter) return@classDefForEach

            for (method in mutableClassDefBy(classDef).methods) {
                if (method.returnType != "Z" || method.parameterTypes.isNotEmpty()) continue
                val marks = method.instructionsOrNull?.any {
                    it.getReference<StringReference>()?.string?.let { s -> s in proMarkers } == true
                } == true
                if (!marks) continue
                method.returnEarly(true)
                gettersForced++
            }
        }

        var proField: FieldReference? = null
        classDefForEach { classDef ->
            if (classDef.type != mainActivityType) return@classDefForEach
            for (method in classDef.methods) {
                val insns = method.instructionsOrNull?.toList() ?: continue
                val hasKey = insns.any {
                    it.getReference<StringReference>()?.string == "pref_key_latitude_2"
                }
                if (!hasKey) continue
                proField = insns.firstNotNullOfOrNull {
                    if (it.opcode == Opcode.SPUT_BOOLEAN) it.getReference<FieldReference>() else null
                }
                if (proField != null) break
            }
        }

        var fieldReadsForced = 0
        proField?.let { field ->
            classDefForEach { classDef ->
                val readsField = classDef.methods.any { method ->
                    method.instructionsOrNull?.any {
                        it.opcode == Opcode.SGET_BOOLEAN &&
                            it.getReference<FieldReference>()?.let { ref ->
                                ref.name == field.name && ref.definingClass == field.definingClass
                            } == true
                    } == true
                }
                if (!readsField) return@classDefForEach

                for (method in mutableClassDefBy(classDef).methods) {
                    val insns = method.instructionsOrNull?.toList() ?: continue
                    insns.withIndex().reversed().forEach { (index, insn) ->
                        if (insn.opcode != Opcode.SGET_BOOLEAN) return@forEach
                        val ref = insn.getReference<FieldReference>() ?: return@forEach
                        if (ref.name != field.name || ref.definingClass != field.definingClass) {
                            return@forEach
                        }
                        val register = (insn as OneRegisterInstruction).registerA
                        method.replaceInstruction(index, "const/16 v$register, 0x1")
                        fieldReadsForced++
                    }
                }
            }
        }

        if (gettersForced == 0) {
            throw PatchException("Fake GPS: no Pro-state getters were found to patch.")
        }
        logger.info(
            "Fake GPS: forced $gettersForced Pro getter(s) and $fieldReadsForced in-memory flag read(s) to true.",
        )
    }
}
