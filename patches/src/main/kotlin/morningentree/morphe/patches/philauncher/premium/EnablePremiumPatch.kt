package morningentree.morphe.patches.philauncher.premium

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.philauncher.shared.disablePairipLicenseCheckPatch
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Phi Launcher Pro",
) {
    compatibleWith(
        Compatibility(
            name = "Phi Launcher",
            packageName = "com.launcher.hype",
            appIconColor = 0x6C63FF,
            targets = listOf(AppTarget("3.5.1")),
        ),
    )

    // Pairip's license check runs in attachBaseContext and blocks the re-signed APK at launch,
    // so premium would never even be reachable without neutralizing it first.
    dependsOn(disablePairipLicenseCheckPatch)

    execute {
        // Force the app-wide Pro gate true. Every feature check reads this getter directly, and
        // the reactive BillingViewModel flow is re-initialised from it, so this single edit unlocks
        // both the synchronous checks and the observed Compose screens — and it is robust against
        // the server (Supabase) status refresh, which only writes the field this getter now ignores.
        IsProUserFingerprint.method.returnEarly(true)
    }
}
