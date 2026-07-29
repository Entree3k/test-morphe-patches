package morningentree.morphe.patches.nova.privacy

import app.morphe.patcher.patch.resourcePatch
import morningentree.morphe.patches.nova.shared.Constants
import morningentree.morphe.util.asElementSequence
import morningentree.morphe.util.get
import org.w3c.dom.Element
import java.util.logging.Logger

// Ad-ID / Privacy-Sandbox ad-services permissions — removed so the app can't read the advertising ID
// or use the Topics/Attribution APIs. Removing a <uses-permission> never removes code, so it can't
// crash the app; the OS simply denies the capability.
private val TRACKER_PERMISSIONS = setOf(
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    "android.permission.ACCESS_ADSERVICES_TOPICS",
)

// Analytics/telemetry collection flags forced off via <application> meta-data. These are read by the
// SDKs themselves (Firebase/GA honor them) — safe, no component/class is touched.
private val COLLECTION_FLAGS_OFF = mapOf(
    "firebase_analytics_collection_enabled" to "false",
    "firebase_analytics_collection_deactivated" to "true",
    "firebase_crashlytics_collection_enabled" to "false",
    "google_analytics_adid_collection_enabled" to "false",
    "google_analytics_default_allow_ad_personalization_signals" to "false",
)

/**
 * Disables the bundled analytics/ad tracking in Nova **without removing any manifest components**.
 *
 * ⚠️ HISTORY: an earlier version of this patch stripped the ad/analytics SDK components (Pangle,
 * Vungle, Branch, Instabridge, GMS measurement, the Firebase analytics-connector registrar, …). That
 * **crashed Nova on launch** — Firebase validates its component-discovery dependency graph at startup,
 * and Nova's obfuscated `Application` initialises several of those SDKs directly, so a missing
 * component / un-initialised auto-init provider throws before the launcher can draw. In a fully
 * R8-obfuscated app that can't be made safe by static inspection alone.
 *
 * So this patch now only does the crash-proof things:
 *  - forces analytics/crashlytics/ad-id **collection flags off** (SDKs read these and self-disable), and
 *  - strips the **ad-ID / ad-services permissions**.
 *
 * The actual "trackers can't phone home" guarantee comes from the companion **"Remove internet
 * permission"** patch — enable both together. With no network, every remaining SDK is inert; with
 * Prime unlocked the ad SDKs are never invoked anyway (ads are a free-tier feature).
 */
@Suppress("unused")
val removeTrackersPatch = resourcePatch(
    name = "Disable analytics & ad tracking",
    description = "Turns off Firebase/Google analytics & crashlytics collection and removes the " +
        "advertising-ID / ad-services permissions. Does NOT remove SDK components (that crashes " +
        "Nova on launch). Pair with \"Remove internet permission\" to fully block trackers.",
    default = false,
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        document("AndroidManifest.xml").use { document ->
            val manifest = document["manifest"]
            val application = document["application"]

            // 1. Force analytics/crashlytics/ad-id collection flags off (upsert <application> meta-data).
            COLLECTION_FLAGS_OFF.forEach { (name, value) ->
                val existing = application.getElementsByTagName("meta-data")
                    .asElementSequence()
                    .firstOrNull { it.getAttribute("android:name") == name }

                if (existing != null) {
                    existing.removeAttribute("android:resource")
                    existing.setAttribute("android:value", value)
                } else {
                    val meta = document.createElement("meta-data") as Element
                    meta.setAttribute("android:name", name)
                    meta.setAttribute("android:value", value)
                    application.appendChild(meta)
                }
            }

            // 2. Strip ad-ID / ad-services permissions.
            var removedPermissions = 0
            manifest.getElementsByTagName("uses-permission")
                .asElementSequence()
                .filter { it.getAttribute("android:name") in TRACKER_PERMISSIONS }
                .toList()
                .forEach {
                    it.parentNode.removeChild(it)
                    removedPermissions++
                }

            logger.info(
                "Nova disable analytics: collection flags forced off, " +
                    "$removedPermissions ad permission(s) removed.",
            )
        }
    }
}
