package morningentree.morphe.patches.bend.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlock Bend Premium",
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
        // The premium check is asynchronous (RevenueCat), so the gate is a suspend
        // function returning a boxed Boolean (Ljava/lang/Object;), not a plain Z —
        // the shared returnEarly(Boolean) helper (Z-only) does not apply here.
        // Complete it immediately with Boolean.TRUE; callers unbox this straight to
        // "premium active" (a synchronous return value is a valid coroutine result,
        // so the method simply never suspends).
        IsPremiumEntitlementActiveFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )
    }
}
