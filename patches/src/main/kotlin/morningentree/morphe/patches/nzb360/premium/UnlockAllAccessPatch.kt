package morningentree.morphe.patches.nzb360.premium

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import morningentree.morphe.patches.nzb360.shared.Constants
import morningentree.morphe.util.getReference
import morningentree.morphe.util.returnEarly

/**
 * Unlock nzb360 All Access.
 *
 * Forces `isAASubscriptionActive`, `isUnlocked` and `Contract$UIState.isSubscribed`
 * to return true, and both `isLocked` overloads to return false, bypassing all
 * per-service module paywalls (SABnzbd, Torrents, Radarr, Sonarr, etc.).
 *
 * Also pre-selects the Yearly plan in Settings → Upgrade Center by rewriting the
 * `mutableStateOf("Monthly")` seed to "Yearly".
 *
 * nzb360's classes are not obfuscated, so each gate is pinned by its real
 * class + method name; `methodOrNull` keeps optional gates non-fatal if a build
 * drops one.
 */
@Suppress("unused")
val unlockAllAccessPatch = bytecodePatch(
    name = "Unlock All Access",
    description = "Unlocks All Access in nzb360.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        listOf(
            IsAASubscriptionActiveFingerprint,
            IsUnlockedFingerprint,
            IsSubscribedFingerprint,
        ).forEach { it.methodOrNull?.returnEarly(true) }

        listOf(
            IsLockedTwoArgFingerprint,
            IsLockedOneArgFingerprint,
        ).forEach { it.methodOrNull?.returnEarly(false) }

        // Default the Upgrade Center toggle to the Yearly plan.
        SubscriptionSectionDefaultPlanFingerprint.methodOrNull?.apply {
            val insns = instructions.toList()
            val idx = insns.indexOfFirst {
                it.opcode == Opcode.CONST_STRING &&
                    it.getReference<StringReference>()?.string == "Monthly"
            }
            if (idx >= 0) {
                val reg = (insns[idx] as OneRegisterInstruction).registerA
                replaceInstruction(idx, "const-string v$reg, \"Yearly\"")
            }
        }
    }
}
