package morningentree.morphe.patches.flud.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.flud.shared.Constants

private fun dismissActivity() =
    "invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V\n" +
        "invoke-virtual {p0}, Landroid/app/Activity;->finish()V\n" +
        "return-void"

/**
 * Flud (com.delphicoder.flud) is ad-supported. The Pro version is a separate paid APK
 * (com.delphicoder.flud.pro) — there is no in-app billing in the free version.
 *
 * Ads served: AdMob interstitial (ca-app-pub-8308447967239879/5050482671) mediated via
 * AppLovin MAX, plus an AdMob banner. Both load through s13 (AdViewHelper):
 *   s13.a() — loads interstitial + banner
 *   s13.b() — decides when to show interstitial and calls InterstitialAd.show()
 *
 * Layers 3 & 4 neutralize the PairIP license check so the re-signed APK launches;
 * they are optional (`methodOrNull`) in case a build ships without PairIP.
 */
@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Removes all ads in Flud.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Layer 1: Prevent all ad loading — stops interstitial and banner from ever loading.
        AdLoaderFingerprint.methodOrNull?.addInstructions(0, "return-void")

        // Layer 2: Prevent interstitial from being shown — catches any already-loaded ad.
        InterstitialTriggerFingerprint.methodOrNull?.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return-object v0
            """,
        )

        // Layer 3: PairIP LicenseClient.checkLicense(Context) -> no-op.
        LicenseClientFingerprint.methodOrNull?.addInstructions(0, "return-void")

        // Layer 4: PairIP LicenseActivity.onCreate -> finish immediately.
        LicenseActivityOnCreateFingerprint.methodOrNull?.addInstructions(0, dismissActivity())
    }
}
