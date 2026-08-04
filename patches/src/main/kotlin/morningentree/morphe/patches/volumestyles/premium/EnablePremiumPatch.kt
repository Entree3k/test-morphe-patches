package morningentree.morphe.patches.volumestyles.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import morningentree.morphe.patches.volumestyles.shared.Constants
import morningentree.morphe.util.getReference
import java.util.logging.Logger

private const val OWNED_STATE_NAME = "PURCHASED_AND_ACKNOWLEDGED"
private const val SEED_STATE_NAME = "UNKNOWN"
private const val PREMIUM_PREF_KEY = "is_premium_cached"

private fun FieldReference.descriptor() = "$definingClass->$name:$type"

/**
 * Volume Styles (com.tombayley.volumepanel) premium unlock.
 *
 * Same tombayley Play-Billing lib as Super Status Bar: premium is a per-SKU
 * `MutableStateFlow<qs>` (`qs` = obfuscated entitlement enum, owned == `PURCHASED_AND_ACKNOWLEDGED`)
 * held in the billing repository `ft`. The UI observes that flow **directly** (obfuscated mappers
 * compare `state == qs.e` inline), and separately the value is cached to the pref `is_premium_cached`
 * that the ad/premium manager and a few feature classes read.
 *
 * Forcing only the cached pref did NOT unlock, because the live flow governs the UI (and the manager
 * can re-seed from billing). So this forces the **flow itself** to owned at both write points, plus the
 * cached pref for good measure:
 *
 *  1. Seed — `ft.<init>` seeds each SKU's `StateFlow` with `UNKNOWN`; rewrite that to the owned constant
 *     so every launch starts premium (the gradientweather lesson: the initial value governs the normal
 *     screen; a setter-only patch isn't enough).
 *  2. Setter — `ft.k(String, qs)` is the single write path (the reconciler calls it with `UNPURCHASED`
 *     for un-owned SKUs); force its state argument to the owned constant so billing can never downgrade.
 *  3. Cache — force every `getBoolean("is_premium_cached", …)` read true (ad/premium-manager path).
 *
 * Everything is anchored on R8-stable signals — the enum's constant-name strings, `ft.k`'s
 * `"Unknown SKU "` log literal, and the pref key — never on the obfuscated `qs`/`ft`/`k` names.
 */
@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Volume Styles Premium. Use with Spoof Install Source",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        // --- Resolve the entitlement enum + its owned / seed constants. ---
        val enumInit = EntitlementStateEnumFingerprint.method
        val enumType = enumInit.definingClass
        val enumInsns = enumInit.instructions.toList()

        fun fieldForConstant(constantName: String): FieldReference {
            val nameIndex = enumInsns.indexOfFirst {
                it.opcode == Opcode.CONST_STRING &&
                    it.getReference<StringReference>()?.string == constantName
            }
            if (nameIndex < 0) {
                throw PatchException("Could not find entitlement constant '$constantName' in $enumType.")
            }
            return enumInsns.drop(nameIndex).firstNotNullOfOrNull { insn ->
                if (insn.opcode != Opcode.SPUT_OBJECT) return@firstNotNullOfOrNull null
                insn.getReference<FieldReference>()?.takeIf { it.type == enumType }
            } ?: throw PatchException("Could not resolve the '$constantName' field on $enumType.")
        }

        val ownedField = fieldForConstant(OWNED_STATE_NAME)
        val seedField = fieldForConstant(SEED_STATE_NAME)

        // --- 2. Force the state setter ft.k(String, qs) to always store the owned state. ---
        val setter = SetEntitlementStateFingerprint.method
        setter.addInstructions(0, "sget-object p2, ${ownedField.descriptor()}")
        val repositoryType = setter.definingClass

        // --- 1. Re-seed: in the billing repository, replace the StateFlow's UNKNOWN seed with owned. ---
        var reseeded = 0
        classDefForEach { classDef ->
            if (classDef.type != repositoryType) return@classDefForEach
            mutableClassDefBy(classDef).methods.forEach { method ->
                val insns = method.instructionsOrNull?.toList() ?: return@forEach
                insns.forEachIndexed { index, insn ->
                    if (insn.opcode != Opcode.SGET_OBJECT) return@forEachIndexed
                    val ref = insn.getReference<FieldReference>() ?: return@forEachIndexed
                    if (ref.definingClass != seedField.definingClass ||
                        ref.name != seedField.name ||
                        ref.type != seedField.type
                    ) {
                        return@forEachIndexed
                    }
                    val register = (insn as OneRegisterInstruction).registerA
                    method.replaceInstruction(index, "sget-object v$register, ${ownedField.descriptor()}")
                    reseeded++
                }
            }
        }

        // --- 3. Cached fast-path: force every getBoolean("is_premium_cached", …) read true. ---
        fun Instruction.isGetBoolean(): Boolean {
            if (opcode != Opcode.INVOKE_INTERFACE && opcode != Opcode.INVOKE_VIRTUAL &&
                opcode != Opcode.INVOKE_INTERFACE_RANGE && opcode != Opcode.INVOKE_VIRTUAL_RANGE
            ) {
                return false
            }
            val ref = getReference<MethodReference>() ?: return false
            return ref.definingClass == "Landroid/content/SharedPreferences;" &&
                ref.name == "getBoolean" &&
                ref.parameterTypes.size == 2 &&
                ref.returnType == "Z"
        }

        fun Instruction.keyRegisterOrNull(): Int? = when (this) {
            is FiveRegisterInstruction -> registerD
            is RegisterRangeInstruction -> startRegister + 1
            else -> null
        }

        var cacheReads = 0
        classDefForEach { classDef ->
            if (classDef.methods.none { m -> m.instructionsOrNull?.any { it.isGetBoolean() } == true }) {
                return@classDefForEach
            }
            mutableClassDefBy(classDef).methods.forEach { method ->
                val insns = method.instructionsOrNull?.toList() ?: return@forEach
                insns.forEachIndexed { index, insn ->
                    if (!insn.isGetBoolean()) return@forEachIndexed
                    val keyRegister = insn.keyRegisterOrNull() ?: return@forEachIndexed

                    val keyMatches = (index - 1 downTo maxOf(0, index - 10)).any { i ->
                        val prev = insns[i]
                        prev.opcode == Opcode.CONST_STRING &&
                            (prev as OneRegisterInstruction).registerA == keyRegister &&
                            prev.getReference<StringReference>()?.string == PREMIUM_PREF_KEY
                    }
                    if (!keyMatches) return@forEachIndexed

                    val moveResult = insns.getOrNull(index + 1) as? OneRegisterInstruction
                        ?: return@forEachIndexed
                    if (moveResult.opcode != Opcode.MOVE_RESULT) return@forEachIndexed

                    val reg = moveResult.registerA
                    val literal = if (reg <= 15) "const/4 v$reg, 0x1" else "const/16 v$reg, 0x1"
                    method.replaceInstruction(index + 1, literal)
                    cacheReads++
                }
            }
        }

        if (reseeded == 0) {
            throw PatchException("Could not re-seed the entitlement StateFlow in $repositoryType.")
        }

        logger.info(
            "Volume Styles Enable Premium: setter forced, re-seeded $reseeded StateFlow(s), " +
                "$cacheReads '$PREMIUM_PREF_KEY' read(s) forced true.",
        )
    }
}
