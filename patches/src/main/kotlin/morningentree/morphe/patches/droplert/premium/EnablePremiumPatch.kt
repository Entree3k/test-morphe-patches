package morningentree.morphe.patches.droplert.premium

import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import morningentree.morphe.patches.all.detection.pairip.disablePairipPatch
import morningentree.morphe.patches.droplert.shared.Constants
import morningentree.morphe.util.getReference
import java.util.logging.Logger

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Droplert premium (RevenueCat \"premium\" entitlement) by forcing the app's " +
        "own premium-override flag, which every premium check falls back to. " +
        "Use with Spoof Install Source.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    // The app is wrapped by Pairip's modern licensecheck (no SignatureCheck): a
    // re-signed APK trips LicenseActivity.onStart -> showPaywallAndCloseApp and is
    // killed at launch. Neutralize the whole license chain so it launches.
    dependsOn(disablePairipPatch)

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        val gate = IsPremiumForCustomerInfoFingerprint.method

        // Every premium check is `entitlement("premium").isActive() || <override>`.
        // The override is a volatile boolean field read (the only iget-boolean in
        // the gate method). It is never written in a stock build, so forcing all
        // of its reads to true flips the aggregate getter e(), the per-CustomerInfo
        // check d() and the inlined paywall/state checks — premium app-wide.
        val overrideField = gate.instructionsOrNull
            ?.firstOrNull { it.opcode == Opcode.IGET_BOOLEAN }
            ?.getReference<FieldReference>()
            ?: throw PatchException(
                "Droplert: could not find the premium-override boolean field in the RevenueCat gate.",
            )

        val gateClassType = gate.definingClass
        var patchedReads = 0

        classDefForEach { classDef ->
            if (classDef.type != gateClassType) return@classDefForEach

            for (method in mutableClassDefBy(classDef).methods) {
                val insns = method.instructionsOrNull?.toList() ?: continue
                insns.withIndex().reversed().forEach { (index, insn) ->
                    if (insn.opcode != Opcode.IGET_BOOLEAN) return@forEach
                    val ref = insn.getReference<FieldReference>() ?: return@forEach
                    if (ref.name != overrideField.name ||
                        ref.definingClass != overrideField.definingClass
                    ) {
                        return@forEach
                    }
                    val register = (insn as OneRegisterInstruction).registerA
                    method.replaceInstruction(index, "const/16 v$register, 0x1")
                    patchedReads++
                }
            }
        }

        if (patchedReads == 0) {
            throw PatchException("Droplert: no premium-override reads were found to patch.")
        }
        logger.info("Droplert: forced $patchedReads premium-override read(s) to true.")
    }
}
