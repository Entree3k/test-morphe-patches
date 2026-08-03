package morningentree.morphe.patches.oldroll.premium

import app.morphe.patcher.Fingerprint

/**
 * Camera unlock gate. OldRoll gates every camera through `AnalogCamera.isUnlockedCommon()Z` — the base
 * of the whole unlock family (isUnlockedWithoutFreeUse -> isUnlocked -> isUnlockedAndCanUse /
 * isUnlockedWithBFreeUse, plus isUnlockedWithoutCaptureDcrUnlock all funnel into it). Forcing it true
 * makes every camera report unlocked. `AnalogCamera` keeps real (un-obfuscated) class/method names, so
 * an exact class+method `custom` match is R8-proof.
 */
internal object IsCameraUnlockedFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lcom/lightcone/analogcam/model/camera/AnalogCamera;" &&
            method.name == "isUnlockedCommon"
    },
)

/**
 * Anti-piracy / modified-app detection. OldRoll ships a Lightcone anti-crack module in the obfuscated
 * `we` package. `we/i.f()Z` is the verdict — "is this build pirated?" (delegates to a `we/j` callback
 * implemented by `com.lightcone.analogcam.app.a`, which inspects the app signature). When true, the app
 * schedules a blocking, exit-only "your version has been cracked … the application will be
 * automatically withdrawn" popup (`we/i.d()` -> string `pirate_pop_text`), re-shown on every activity.
 *
 * Because Morphe re-signs the APK, this verdict trips on ANY patched build and makes the app unusable —
 * this, not the unlock patch, is what closed OldRoll on launch. Forcing `f()` false = "genuine".
 *
 * `we`/`i`/`f` are R8-obfuscated → version-pinned to 6.5.2 (matched by exact DEX type + method name;
 * morphe reads the real DEX so the Windows `we`/`We` filename case-collision is irrelevant at patch time).
 */
internal object PiracyVerdictFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef -> classDef.type == "Lwe/i;" && method.name == "f" },
)

/**
 * `we/i.d()V` — schedules the pirate popup (posts a delayed runnable that shows the exit-only dialog and
 * registers a lifecycle callback to re-show it). No-oped as belt-and-suspenders so the popup can never
 * appear even if some path calls it without first checking `f()`.
 */
internal object PiracyPopupSchedulerFingerprint : Fingerprint(
    returnType = "V",
    parameters = emptyList(),
    custom = { method, classDef -> classDef.type == "Lwe/i;" && method.name == "d" },
)
