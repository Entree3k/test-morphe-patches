package morningentree.morphe.patches.all.misc.network

import app.morphe.patcher.patch.resourcePatch
import morningentree.morphe.util.asElementSequence
import morningentree.morphe.util.get

/**
 * Universal "Remove internet permission" patch — shows in Morphe for **any** app (no `compatibleWith`).
 * Ported from adobo (`dev.jkcarino.adobo...network.RemoveInternetPermissionPatch`).
 *
 * Strips `<uses-permission android:name="android.permission.INTERNET"/>` from the manifest. With no
 * INTERNET permission the OS blocks every socket the app opens, so bundled ad/analytics/telemetry SDKs
 * cannot phone home regardless of how they are wired up. This is the single most reliable privacy lever
 * for a launcher like Nova (which ships Amplitude, Branch, Pangle/ByteDance, Vungle, Fyber, Moloco,
 * Google/Firebase, etc.).
 *
 * Opt-in (`default = false`) because it also disables any legitimate online feature (weather, search
 * suggestions, companion sync, cloud backup).
 */
@Suppress("unused")
val removeInternetPermissionPatch = resourcePatch(
    name = "Remove internet permission",
    description = "Removes the INTERNET permission so the app cannot access the network at all. " +
        "Blocks all trackers, analytics and ads from phoning home, but also disables any legitimate " +
        "online features. Only enable for apps you want fully offline.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document["manifest"]

            manifest.getElementsByTagName("uses-permission")
                .asElementSequence()
                .filter { it.getAttribute("android:name") == "android.permission.INTERNET" }
                .toList()
                .forEach { it.parentNode.removeChild(it) }
        }
    }
}
