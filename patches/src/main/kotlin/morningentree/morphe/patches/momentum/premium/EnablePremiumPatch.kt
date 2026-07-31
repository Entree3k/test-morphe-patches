package morningentree.morphe.patches.momentum.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import morningentree.morphe.patches.momentum.shared.disablePairipLicenseCheckPatch

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Momentum Plus by forcing the RevenueCat \"plus\" entitlement check to true.",
) {
    compatibleWith(
        Compatibility(
            name = "Momentum",
            packageName = "shub39.momentum.play",
            appIconColor = 0xE0553F,
            targets = listOf(AppTarget("1.8.3-play")),
        ),
    )

    // Pairip's license check runs in attachBaseContext and blocks the re-signed APK at launch.
    dependsOn(disablePairipLicenseCheckPatch)

    execute {
        // IsPlusEntitlementFingerprint matches `cy.b(Continuation)` — the suspend method that reads
        // customerInfo.entitlements.getAll().get("plus").isActive() and returns a sealed result.
        // The boolean gate the app actually consumes is the sibling `cy.a(Continuation)`, which calls
        // `b` and returns `Boolean.valueOf(result instanceof <active>)`. `b` is only ever called by
        // `a`, so forcing `a` to return Boolean.TRUE unlocks every premium consumer.
        val entitlementMethod = IsPlusEntitlementFingerprint.method
        val classType = entitlementMethod.definingClass
        val entitlementMethodName = entitlementMethod.name

        var patched = false
        classDefForEach { classDef ->
            if (classDef.type != classType) return@classDefForEach

            mutableClassDefBy(classDef).methods
                .filter { method ->
                    method.name != entitlementMethodName &&
                        method.returnType == "Ljava/lang/Object;" &&
                        method.parameterTypes.size == 1 &&
                        // the sibling boolean gate `a` is the one that invokes `b`
                        method.instructionsOrNull?.any { instruction ->
                            val reference = (instruction as? ReferenceInstruction)?.reference
                            reference is MethodReference &&
                                reference.definingClass == classType &&
                                reference.name == entitlementMethodName
                        } == true
                }
                .forEach { gateMethod ->
                    gateMethod.addInstructions(
                        0,
                        """
                            sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                            return-object v0
                        """,
                    )
                    patched = true
                }
        }

        if (!patched) {
            throw PatchException("Momentum premium gate (sibling of ${classType}->$entitlementMethodName) not found.")
        }
    }
}
