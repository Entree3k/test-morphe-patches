package morningentree.morphe.patches.textra.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.textra.shared.Constants

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Textra Pro, removing the ads.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Textra is Pairip protected. Only the signature check has to go: the VM itself runs real
        // app code (the startup program and two broadcast receivers), so gutting VMRunner would
        // break the app rather than unlock it.
        classDefForEach { classDef ->
            if (classDef.type != "Lcom/pairip/SignatureCheck;") return@classDefForEach

            mutableClassDefBy(classDef).methods
                .filter { it.name == "verifyIntegrity" && it.returnType == "V" }
                .forEach { it.addInstruction(0, "return-void") }
        }

        // 1 is Pro. The method is declared synchronized, so inject after the leading monitor-enter
        // and release the monitor before returning.
        LicensePreferenceGetterFingerprint.method.addInstructions(
            1,
            """
                const/4 v0, 0x1
                invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object v0
                monitor-exit p0
                return-object v0
            """,
        )
    }
}
