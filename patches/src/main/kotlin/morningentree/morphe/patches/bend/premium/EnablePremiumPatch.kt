package morningentree.morphe.patches.bend.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import java.util.logging.Logger

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlock Bend Premium (also disables the pairip license check so the repackaged app runs).",
) {
    compatibleWith(
        Compatibility(
            name = "Bend",
            packageName = "com.bowerydigital.bend",
            // Cosmetic only (patcher UI accent) — app launcher/splash background.
            appIconColor = 0x1674A8,
            targets = listOf(AppTarget("7.6.4")),
        ),
    )

    execute {
        // 1) Premium gate.
        // The premium check is asynchronous (RevenueCat), so the gate is a suspend
        // function returning a boxed Boolean (Ljava/lang/Object;), not a plain Z —
        // the Z-only returnEarly(Boolean) helper does not apply here. Complete it
        // immediately with Boolean.TRUE; callers unbox this straight to "premium
        // active" (a synchronous return value is a valid coroutine result, so the
        // method simply never suspends).
        IsPremiumEntitlementActiveFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )

        // 2) pairip license check.
        // Bend is pairip-wrapped; left intact, pairip's LicenseActivity shows the
        // "get this app from Google Play" dialog and closes the app before the
        // premium UI ever loads. Force the license response to LICENSED and
        // short-circuit its validation. (Does not bypass Play Integrity attestation
        // or pairipcore virtualization — a separate protection.)
        if (ProcessLicenseResponseFingerprint.methodOrNull == null ||
            ValidateLicenseResponseFingerprint.methodOrNull == null
        ) {
            Logger.getLogger(this::class.java.name)
                .warning("Could not find the Pairip license check. Premium applied; license check unchanged.")
        } else {
            ProcessLicenseResponseFingerprint.apply {
                // Set first parameter (responseCode) to 0 (LICENSED).
                method.addInstruction(0, "const/4 p1, 0x0")

                // Disable the repeated re-check if present.
                RepeatedCheckFingerprint.matchOrNull(originalMethod)?.apply {
                    val repeatedCheckFlagInstr = this.instructionMatches.first()
                    val reg = repeatedCheckFlagInstr.getInstruction<OneRegisterInstruction>().registerA
                    this.method.replaceInstruction(
                        // +1 to account for the instruction added above
                        repeatedCheckFlagInstr.index + 1,
                        "const/4 v${reg}, 0x0",
                    )
                }
            }

            // Short-circuit the license response validation (void method).
            ValidateLicenseResponseFingerprint.method.returnEarly()
        }
    }
}
