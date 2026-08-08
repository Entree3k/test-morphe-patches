package morningentree.morphe.patches.smashhit.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.smashhit.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Smash Hit premium and all game modes without a purchase.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // The Java-side premium check.
        OwnsPremiumProductFingerprint.method.returnEarly(true)

        // The owned-sku set lookup every ownership question funnels through. The method is
        // declared synchronized, so inject after the leading monitor-enter and release the
        // monitor before returning.
        IsProductOwnedFingerprint.method.addInstructions(
            1,
            """
                const/4 p1, 0x1
                monitor-exit p0
                return p1
            """,
        )

        // The engine polls "hasrefreshedownedproducts" before it will ever ask
        // "isproductowned", and reads "storeisrestored" after a restore flow. Both answer with
        // the string form of a boolean, so both must report true for the checks above to be
        // reached and trusted.
        listOf(
            HasRefreshedOwnedProductsFingerprint,
            IsPremiumProductRestoredFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(
                0,
                """
                    const-string p1, "true"
                    return-object p1
                """,
            )
        }
    }
}
