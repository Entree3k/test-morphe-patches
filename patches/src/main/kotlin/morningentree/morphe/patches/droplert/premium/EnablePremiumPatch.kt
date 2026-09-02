package morningentree.morphe.patches.droplert.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import morningentree.morphe.patches.all.detection.pairip.disablePairipPatch
import morningentree.morphe.patches.droplert.shared.Constants
import morningentree.morphe.util.getReference
import morningentree.morphe.util.returnEarly
import java.util.logging.Logger

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Droplert premium (lifetime) by forcing the RevenueCat state refresh to " +
        "publish the PREMIUM tier regardless of the real \"premium\" entitlement. " +
        "Use with Spoof Install Source.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    // The app is wrapped by Pairip's modern licensecheck (no SignatureCheck): a
    // re-signed APK trips LicenseActivity.onStart -> showPaywallAndCloseApp and is
    // killed at launch. Neutralize the whole license chain so it launches.
    dependsOn(disablePairipPatch)

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        // Primary lever: the state refresh only publishes the PREMIUM tier when
        // its "entitlement active" register is non-zero. Force that register to 1
        // right before the guard so the refresh always publishes PREMIUM (with a
        // null expiration => lifetime). This is the state the Compose UI collects.
        val refresh = RevenueCatStateRefreshFingerprint.method
        val insns = refresh.instructionsOrNull?.toList()
            ?: throw PatchException("Droplert: state-refresh method has no instructions.")

        val isActiveIndex = insns.indexOfFirst { insn ->
            val ref = insn.getReference<MethodReference>()
            ref?.definingClass == "Lcom/revenuecat/purchases/EntitlementInfo;" &&
                ref.name == "isActive"
        }
        if (isActiveIndex < 0) {
            throw PatchException("Droplert: EntitlementInfo.isActive call not found in state refresh.")
        }

        val guardIndex = (isActiveIndex + 1 until insns.size).firstOrNull {
            insns[it].opcode == Opcode.IF_NEZ
        } ?: throw PatchException("Droplert: premium guard branch not found in state refresh.")

        val guardRegister = (insns[guardIndex] as OneRegisterInstruction).registerA
        refresh.addInstruction(guardIndex, "const/16 v$guardRegister, 0x1")
        logger.info("Droplert: forced RevenueCat state refresh to publish PREMIUM (lifetime).")

        // Secondary: the per-CustomerInfo boolean drives the view-model isPremium
        // StateFlows (paywall/upgrade UI). Keep it consistent with the state.
        IsPremiumForCustomerInfoFingerprint.method.returnEarly(true)
    }
}
