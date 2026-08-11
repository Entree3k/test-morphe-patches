package morningentree.morphe.patches.all.misc.tracking

/**
 * Curated blocklist of common tracking / analytics / ads hosts, baked into the repo so the
 * universal host-blocker patch needs no user-supplied file. Edit freely — one host per line,
 * `#` comments allowed. With wildcard matching, "example.com" also covers "sub.example.com".
 *
 * Note: only hosts that appear as literal strings in the app's own bytecode are rewritten.
 * Telemetry routed through Google Play Services (e.g. Clearcut) has no literal URL and is unaffected.
 */
internal const val UNIVERSAL_TRACKING_HOSTS = """
    # --- Firebase / Google analytics & measurement ---
    app-measurement.com
    firebaselogging-pa.googleapis.com
    firebaselogging.googleapis.com
    google-analytics.com
    ssl.google-analytics.com
    www.google-analytics.com
    analytics.google.com
    firebase-settings.crashlytics.com
    crashlyticsreports-pa.googleapis.com
    firebaseremoteconfig.googleapis.com
    firebaseinstallations.googleapis.com

    # --- Google ads ---
    googleads.g.doubleclick.net
    ad.doubleclick.net
    stats.g.doubleclick.net
    pagead2.googlesyndication.com
    googlesyndication.com
    www.googleadservices.com
    googleadservices.com
    admob.com
    admob-gmats.uc.r.appspot.com

    # --- Third-party mobile ad networks ---
    applovin.com
    ms.applovin.com
    unityads.unity3d.com
    config.unityads.unity3d.com
    auction.unityads.unity3d.com
    adservice.google.com
    inner-active.mobi
    ads.mopub.com
    ads.api.vungle.com
    api.vungle.com
    ade.googlesyndication.com
    ads.flurry.com
    data.flurry.com

    # --- Third-party analytics / attribution ---
    api.mixpanel.com
    api.amplitude.com
    api2.amplitude.com
    app.adjust.com
    app.adjust.world
    s2s.adjust.com
    api.segment.io
    cdn.segment.com
    graph.facebook.com
    connect.facebook.net
    api.appsflyer.com
    t.appsflyer.com
    events.appsflyer.com
    sdk.iad-01.braze.com
    api.branch.io
    google-analytics.l.google.com
"""
