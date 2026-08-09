package morningentree.morphe.patches.adler.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.adler.shared.Constants
import morningentree.morphe.patches.adler.shared.disablePairipSignatureCheckPatch
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Notepad Premium by removing all ads. Use with Spoof Install Source.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    // The app is wrapped by Pairip; its SignatureCheck.verifyIntegrity throws on
    // a re-signed APK (instant launch crash), so neutralize it.
    dependsOn(disablePairipSignatureCheckPatch)

    execute {
        // AdlerApp.d() returns true when ads should be shown. Forcing it to
        // return false makes every ad path treat the user as premium (ad-free).
        ShouldShowAdsFingerprint.method.returnEarly(false)
    }
}
