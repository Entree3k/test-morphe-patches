package morningentree.morphe.patches.bottled.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.all.misc.installer.spoofInstallSourcePatch
import morningentree.morphe.patches.bottled.shared.Constants
import morningentree.morphe.util.injectActiveRevenueCatEntitlements
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Bottled Premium.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    // Bottled is Expo/React Native: at signup it reports the installer package
    // (react-native-device-info -> PackageManager.getInstallerPackageName) to its
    // backend, which bans accounts on any non-Play install ("banned due to terms
    // of service"). Force the installer to read as the Play Store so a re-signed,
    // sideloaded build is not flagged. There is no native RASP / Play Integrity /
    // App Check attestation and no app-signature read, so this is the only signal
    // that needs neutralizing.
    dependsOn(spoofInstallSourcePatch)

    execute {
        // Bottled is React Native (Hermes): the premium decision lives in the JS
        // bundle, but billing runs through the native RevenueCat SDK. Inject a
        // synthetic active entitlement at the DEX serialization boundary
        // (EntitlementInfosMapperKt.map) under every common id ("Bottled Premium"
        // reads as "premium"), in both the "all" and "active" sub-maps.
        EntitlementInfosMapperFingerprint.method.injectActiveRevenueCatEntitlements()

        // Any pre-existing entitlement object also reports active.
        EntitlementInfoIsActiveFingerprint.method.returnEarly(true)
    }
}
