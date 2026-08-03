package morningentree.morphe.patches.superstatusbar.premium

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import morningentree.morphe.patches.superstatusbar.shared.Constants
import morningentree.morphe.util.getReference
import morningentree.morphe.util.returnEarly
import java.util.logging.Logger

/**
 * Highest paywall enum constant. `ro.h(eo)Z` (the UI's entitlement -> Boolean converter,
 * called from the obfuscated flow mappers `io`/`o3`) returns `true` only when the state
 * equals this constant, so it is the definitive "premium owned" gate.
 */
private const val OWNED_STATE_NAME = "PURCHASED_AND_ACKNOWLEDGED"

/**
 * Super Status Bar premium unlock.
 *
 * Premium status is a per-SKU `MutableStateFlow<eo>` in the billing repository. Every
 * feature-gating screen (settings, status-bar styles, colouring, extras, gestures, the
 * premium overlays, …) observes that flow after it is folded to a `Boolean` by exactly
 * one converter, `ro.h(eo)Z` == `state == PURCHASED_AND_ACKNOWLEDGED`. Forcing that
 * converter to always report owned makes the whole app treat every product as fully
 * purchased, with no Google Play purchase or network check.
 *
 * The converter itself has no strings, so we anchor on the entitlement enum's stable
 * constant names ([EntitlementStateEnumFingerprint]), read its class type and "owned"
 * field off the matched `<clinit>`, then locate the converter structurally: a `static`
 * method returning `Z`, taking that enum as its only parameter, that reads the "owned"
 * field. No obfuscated class/field letters are hard-coded.
 */
@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Super Status Bar Premium locally by forcing the entitlement \"owned\" " +
        "check to always report a purchased subscription. Use with Spoof Install Source.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        // 1. Resolve the entitlement-state enum from its constant-name strings.
        val enumInit = EntitlementStateEnumFingerprint.method
        val enumType = enumInit.definingClass

        // 2. Find the enum's "owned" constant (PURCHASED_AND_ACKNOWLEDGED). In the enum's
        //    <clinit> the constant is created right after its name literal and stored into
        //    its backing field via the first sput-object of the enum's own type.
        val enumInsns = enumInit.instructions.toList()
        val ownedNameIndex = enumInsns.indexOfFirst {
            it.opcode == Opcode.CONST_STRING &&
                it.getReference<StringReference>()?.string == OWNED_STATE_NAME
        }
        if (ownedNameIndex < 0) {
            throw PatchException("Could not find the '$OWNED_STATE_NAME' entitlement constant in $enumType.")
        }
        val ownedField = enumInsns.drop(ownedNameIndex).firstNotNullOfOrNull { insn ->
            if (insn.opcode != Opcode.SPUT_OBJECT) return@firstNotNullOfOrNull null
            insn.getReference<FieldReference>()?.takeIf { it.type == enumType }
        } ?: throw PatchException("Could not resolve the owned entitlement field on $enumType.")

        // 3. A converter is: static, returns Z, single parameter = the entitlement enum.
        fun Method.isEntitlementConverter() =
            AccessFlags.STATIC.isSet(accessFlags) &&
                returnType == "Z" &&
                parameterTypes.size == 1 &&
                parameterTypes.first().toString() == enumType

        // 4. Force every such converter that reads the "owned" field to report owned.
        var patched = 0
        classDefForEach { classDef ->
            if (classDef.methods.none { it.isEntitlementConverter() }) return@classDefForEach

            mutableClassDefBy(classDef).methods.forEach { method ->
                if (!method.isEntitlementConverter()) return@forEach

                val insns = method.instructionsOrNull ?: return@forEach
                val readsOwnedField = insns.any {
                    val ref = it.getReference<FieldReference>() ?: return@any false
                    ref.name == ownedField.name &&
                        ref.definingClass == ownedField.definingClass &&
                        ref.type == ownedField.type
                }
                if (!readsOwnedField) return@forEach

                method.returnEarly(true)
                patched++
            }
        }

        if (patched == 0) {
            throw PatchException("Could not find the entitlement '$OWNED_STATE_NAME' converter to patch.")
        }

        logger.info("Super Status Bar Enable Premium: forced $patched entitlement converter(s) to owned.")
    }
}
