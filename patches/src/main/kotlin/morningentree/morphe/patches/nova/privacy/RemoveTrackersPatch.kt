package morningentree.morphe.patches.nova.privacy

import app.morphe.patcher.patch.resourcePatch
import morningentree.morphe.patches.nova.shared.Constants
import morningentree.morphe.util.asElementSequence
import morningentree.morphe.util.get
import org.w3c.dom.Element
import java.util.logging.Logger

// Third-party ad / analytics / attribution SDK class-name prefixes bundled into Nova 8.8.6.
// Any manifest component (activity/service/receiver/provider) whose android:name starts with one of
// these is removed, which also kills the SDKs' auto-init ContentProviders so they never start.
private val TRACKER_COMPONENT_PREFIXES = listOf(
    // ByteDance / Pangle ad SDK
    "com.bytedance.", "com.pgl.", "com.bykv.",
    // Other ad networks
    "com.vungle.", "com.fyber.", "com.moloco.", "com.digitalturbine.",
    "com.facebook.ads.",
    "com.google.android.gms.ads.",
    // Analytics / attribution
    "com.amplitude.",
    "com.google.android.gms.measurement.",
    "io.branch.", "nova.branch.",
    "com.instabridge.",
    "ninja.sesame.",
)

// Ad-ID / Privacy-Sandbox ad-services permissions — removed so the app can't read the advertising ID
// or use the Topics/Attribution APIs.
private val TRACKER_PERMISSIONS = setOf(
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    "android.permission.ACCESS_ADSERVICES_TOPICS",
)

// Analytics/telemetry collection flags forced off via <application> meta-data.
private val COLLECTION_FLAGS_OFF = mapOf(
    "firebase_analytics_collection_enabled" to "false",
    "firebase_analytics_collection_deactivated" to "true",
    "firebase_crashlytics_collection_enabled" to "false",
    "google_analytics_adid_collection_enabled" to "false",
    "google_analytics_default_allow_ad_personalization_signals" to "false",
)

/**
 * Aggressively strips the ad / analytics / attribution SDKs that shipped with Nova after the Branch /
 * Instabridge acquisition, turning it into an offline-friendly privacy launcher.
 *
 * Removes every manifest component belonging to Pangle/ByteDance, Vungle, Fyber, Moloco, Digital
 * Turbine, Facebook Audience Network, AdMob, GMS measurement (Google/Firebase Analytics dispatch),
 * Amplitude, Branch, Instabridge and the Sesame search bridge; forces Firebase analytics/crashlytics
 * collection off; and drops the ad-ID / ad-services permissions.
 *
 * ⚠️ Removing Branch/Instabridge/Sesame **disables Nova's app-drawer web/app "search" backend** — that
 * is the tradeoff for cutting the attribution SDK. Ads themselves are already gone once Prime is
 * unlocked (they are a free-tier feature), and pairing this with "Remove internet permission" blocks
 * anything that survives. Opt-in (`default = false`) because of the search tradeoff.
 */
@Suppress("unused")
val removeTrackersPatch = resourcePatch(
    name = "Remove trackers & analytics",
    description = "Strips bundled ad/analytics/attribution SDKs (Pangle, Vungle, Fyber, Moloco, " +
        "Digital Turbine, Facebook Ads, AdMob, Amplitude, Google/Firebase Analytics, Branch, " +
        "Instabridge) and disables analytics collection. Note: this also disables Nova's app-drawer " +
        "web/app search, which is powered by Branch.",
    default = false,
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        document("AndroidManifest.xml").use { document ->
            val manifest = document["manifest"]
            val application = document["application"]

            // 1. Remove tracker/ad SDK components (incl. their auto-init ContentProviders).
            var removedComponents = 0
            listOf("activity", "activity-alias", "service", "receiver", "provider").forEach { tag ->
                application.getElementsByTagName(tag)
                    .asElementSequence()
                    .filter { el ->
                        val name = el.getAttribute("android:name")
                        TRACKER_COMPONENT_PREFIXES.any(name::startsWith)
                    }
                    .toList()
                    .forEach {
                        it.parentNode.removeChild(it)
                        removedComponents++
                    }
            }

            // 2. Remove the Firebase Analytics connector registrar so it isn't component-discovered.
            application.getElementsByTagName("meta-data")
                .asElementSequence()
                .filter { it.getAttribute("android:name").contains("analytics.connector") }
                .toList()
                .forEach { it.parentNode.removeChild(it) }

            // 3. Force analytics/crashlytics/ad-id collection flags off (upsert <application> meta-data).
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

            // 4. Strip ad-ID / ad-services permissions.
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
                "Nova Remove trackers: removed $removedComponents component(s), " +
                    "$removedPermissions ad permission(s); analytics collection forced off.",
            )
        }
    }
}
