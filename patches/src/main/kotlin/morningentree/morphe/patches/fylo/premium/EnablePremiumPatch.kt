package morningentree.morphe.patches.fylo.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import morningentree.morphe.patches.all.detection.pairip.disablePairipPatch
import morningentree.morphe.patches.fylo.shared.Constants
import morningentree.morphe.util.getReference
import java.util.logging.Logger

/**
 * Fylo 2.1 rewrote its billing layer. The old `DebugFeatureUnlock.isActive` switch is gone; the
 * BillingManager (obfuscated, `Lk41;` at time of writing) now tracks a subscription-plan enum
 * (NONE/MONTHLY/YEARLY/LIFETIME) and derives `_isPro = (plan != NONE)`.
 *
 * Two coupled edits, resolved by stable anchors rather than obfuscated names:
 *  - Seed: the active-plan resolver (0-param method returning the plan enum, containing the
 *    "migrated" grace-period string) is forced to return LIFETIME. The constructor reads it to
 *    seed `_entitlement` + `_isPro` on first launch.
 *  - Pin: the state publisher (`(plan, String)V`, sets both `_entitlement` and `_isPro`) has its
 *    plan argument overwritten with LIFETIME, so a purchase refresh for a non-purchaser can never
 *    downgrade `_isPro` back to false.
 */
@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Pro",
    description = "Unlocks Fylo File Manager Pro",
) {
    compatibleWith(Constants.COMPATIBILITY)

    dependsOn(disablePairipPatch)

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        // 1. Resolve the subscription-plan enum and its LIFETIME constant by <clinit> string names
        //    (obfuscation survives only in the enum-value const-strings).
        val planNames = setOf("NONE", "MONTHLY", "YEARLY", "LIFETIME")
        var planEnumType: String? = null
        var lifetimeField: FieldReference? = null

        classDefForEach { classDef ->
            if (planEnumType != null) return@classDefForEach
            val clinit = classDef.methods.firstOrNull { it.name == "<clinit>" } ?: return@classDefForEach
            val insns = clinit.instructionsOrNull?.toList() ?: return@classDefForEach
            val strings = insns.mapNotNull { it.getReference<StringReference>()?.string }.toSet()
            if (!strings.containsAll(planNames)) return@classDefForEach

            val nameIndex = insns.indexOfFirst {
                it.getReference<StringReference>()?.string == "LIFETIME"
            }
            val field = ((nameIndex + 1) until insns.size).firstNotNullOfOrNull { i ->
                val insn = insns[i]
                if (insn.opcode == Opcode.SPUT_OBJECT &&
                    insn.getReference<FieldReference>()?.type == classDef.type
                ) {
                    insn.getReference<FieldReference>()
                } else {
                    null
                }
            } ?: return@classDefForEach

            planEnumType = classDef.type
            lifetimeField = field
        }

        val planEnum = planEnumType
            ?: throw PatchException("Fylo: could not resolve the subscription-plan enum.")
        val lifetime = lifetimeField
            ?: throw PatchException("Fylo: could not resolve the LIFETIME plan constant.")
        val lifetimeRef = "${lifetime.definingClass}->${lifetime.name}:${lifetime.type}"

        // 2. Seed the active-plan resolver + 3. pin the state publisher (both live on the
        //    BillingManager, so a single class pass handles them).
        var seeded = false
        var pinned = false

        classDefForEach { classDef ->
            if (seeded && pinned) return@classDefForEach
            val mutableClass = mutableClassDefBy(classDef)
            for (method in mutableClass.methods) {
                if (!seeded &&
                    method.returnType == planEnum &&
                    method.parameterTypes.isEmpty()
                ) {
                    val hasMigrated = method.instructionsOrNull.orEmpty().any {
                        it.getReference<StringReference>()?.string == "migrated"
                    }
                    if (hasMigrated) {
                        method.addInstructions(
                            0,
                            """
                                sget-object v0, $lifetimeRef
                                return-object v0
                            """.trimIndent(),
                        )
                        seeded = true
                        continue
                    }
                }

                if (!pinned &&
                    method.returnType == "V" &&
                    method.parameterTypes.map { it.toString() } == listOf(planEnum, "Ljava/lang/String;")
                ) {
                    method.addInstructions(0, "sget-object p1, $lifetimeRef")
                    pinned = true
                }
            }
        }

        if (seeded) {
            logger.info("Fylo: active-plan resolver pinned to LIFETIME.")
        } else {
            logger.warning("Fylo: active-plan resolver not found; relying on publisher pin only.")
        }

        if (pinned) {
            logger.info("Fylo: state publisher pinned to LIFETIME.")
        } else {
            logger.warning("Fylo: state publisher not found; relying on resolver seed only.")
        }

        if (!seeded && !pinned) {
            throw PatchException("Fylo: neither the plan resolver nor the publisher could be patched.")
        }
    }
}
