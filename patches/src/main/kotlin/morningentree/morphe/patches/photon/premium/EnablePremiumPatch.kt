package morningentree.morphe.patches.photon.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.photon.shared.Constants

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Photon Camera Pro — the VIP LUTs, AI photo analysis and LUT Creator — " +
        "without a purchase.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Photon is Pairip protected (license check only, no VMRunner). Short-circuit the check
        // wired into Application.attachBaseContext so the re-signed APK is not killed at launch.
        classDefForEach { classDef ->
            if (classDef.type != "Lcom/pairip/licensecheck/LicenseClient;") return@classDefForEach

            mutableClassDefBy(classDef).methods
                .filter { it.name == "checkLicense" && it.returnType == "V" }
                .forEach { it.addInstruction(0, "return-void") }
        }

        // The BillingManager's queryPurchasesAsync callback runs at launch and writes "is premium
        // owned" into the manager's premium MutableStateFlow, which every VIP/Pro gate collects.
        // Overwrite it to always report owned. The descriptors below are the manager's own field
        // (`a`), its flow field (`l`) and the flow's setter (`j`), taken verbatim from this method
        // — obfuscated names pinned to 1.25.1.2, so re-derive them if the fingerprint moves.
        QueryPurchasesResultFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lrd;->a:Lj8;
                iget-object v0, v0, Lj8;->l:Ljava/lang/Object;
                check-cast v0, LUO0;
                const/4 v1, 0x0
                sget-object v2, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                invoke-virtual {v0, v1, v2}, LUO0;->j(Ljava/lang/Object;Ljava/lang/Object;)Z
                return-void
            """,
        )
    }
}
