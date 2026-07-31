package morningentree.morphe.patches.bend.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import morningentree.morphe.patches.bend.shared.Constants
import morningentree.morphe.patches.bend.shared.disablePairipPatch
import morningentree.morphe.util.getReference

private const val SUBSCRIPTION_PLATFORM =
    "Lcom/bowerydigital/bend/core/subscription/SubscriptionPlatform;"

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Bend premium by forcing the RevenueCat entitlement resolver to always report an active subscription.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    // Bend is wrapped by Pairip: attachBaseContext runs SignatureCheck.verifyIntegrity (which
    // crashes the re-signed APK) and the pairipcore VM startup program before any app code, so
    // premium would never be reachable without neutralizing Pairip first.
    dependsOn(disablePairipPatch)

    execute {
        ActiveSubscriptionFingerprint.method.apply {
            // Recover the obfuscated active-subscription holder type (`be`) from the method's own
            // return construction: the constructor whose first parameter is SubscriptionPlatform.
            // The app's names are R8-obfuscated, so we never hardcode them.
            val holderType = instructions.firstNotNullOfOrNull { insn ->
                insn.getReference<MethodReference>()?.takeIf {
                    it.name == "<init>" &&
                        it.parameterTypes.firstOrNull()?.toString() == SUBSCRIPTION_PLATFORM
                }
            }?.definingClass ?: throw PatchException(
                "Could not locate the active-subscription holder constructor; app internals changed.",
            )

            // Return a valid PLAY_STORE active subscription immediately (non-null == premium). A
            // suspend function may complete synchronously, so both direct callers and the reactive
            // UpdatedCustomerInfoListener observe premium. The method is `.locals 4`, so v0-v2 are
            // free — the injected block returns before the original body runs.
            addInstructions(
                0,
                """
                    sget-object v0, $SUBSCRIPTION_PLATFORM->PLAY_STORE:$SUBSCRIPTION_PLATFORM
                    new-instance v1, $holderType
                    const-string v2, "play.google.com"
                    invoke-direct {v1, v0, v2}, $holderType-><init>(${SUBSCRIPTION_PLATFORM}Ljava/lang/String;)V
                    return-object v1
                """,
            )
        }
    }
}
