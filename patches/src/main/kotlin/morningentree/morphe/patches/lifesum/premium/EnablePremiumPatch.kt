package morningentree.morphe.patches.lifesum.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlock Lifesum Premium",
) {
    compatibleWith(
        Compatibility(
            name = "Lifesum",
            packageName = "com.sillens.shapeupclub",
            // Cosmetic only (patcher UI accent) — Lifesum brand green.
            appIconColor = 0x41CD8C,
            targets = listOf(AppTarget("20.8.0")),
        ),
    )

    execute {
        // Source of truth: the backend profile's "premium" flag. It is read exactly
        // once by the network -> ProfileModel mapper and populates
        // ProfileModel.premium (Ll/yrc;.a), which ~30 sites read directly. Returning
        // Boolean.TRUE here unlocks all of them once the profile syncs. The getter
        // returns Ljava/lang/Boolean; (not Z), so returnEarly(Boolean) does not
        // apply — return the boxed TRUE directly. (.locals 0, so p0 is reused.)
        ApiUserProfileGetPremiumFingerprint.method.addInstructions(
            0,
            """
                sget-object p0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object p0
            """.trimIndent(),
        )

        // Semantic gate (returnType Z) used directly by feature checks; force it true
        // for immediate effect even before the first profile sync completes.
        HasPremiumFingerprint.method.returnEarly(true)
    }
}
