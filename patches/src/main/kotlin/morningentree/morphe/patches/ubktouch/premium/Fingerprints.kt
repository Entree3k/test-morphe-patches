package morningentree.morphe.patches.ubktouch.premium

import app.morphe.patcher.Fingerprint

/**
 * `MainPref.unlocked()Ljava/lang/Boolean;` — the stable, un-obfuscated wrapper around the app's
 * license check. It lazily computes the unlocked state from the obfuscated static `fp1.e0()Z`
 * (which reads a stored boolean flag) and caches it.
 *
 * `fp1.e0()` is the true app-wide gate — called from ~49 sites directly — but it is obfuscated and
 * has no strings of its own to anchor on. So we fingerprint this stable wrapper (real class/method
 * names; the app is not obfuscated here) and resolve `fp1.e0()` through the reference inside it.
 */
internal object MainPrefUnlockedFingerprint : Fingerprint(
    returnType = "Ljava/lang/Boolean;",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Leu/toneiv/ubktouch/model/preferences/MainPref;" && method.name == "unlocked"
    },
)
