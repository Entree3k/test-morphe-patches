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
 * Anti-piracy / license verdict. OldRoll ships a Lightcone anti-crack module in the obfuscated `we`
 * package. `we/i.f()Z` is the verdict, and it resolves down to the SAME flag as the VIP/unlock state:
 *   we/i.f() -> we/j.a() (impl O4/e) -> app/a.b() -> app/a.p() -> manager/j.r0()
 * `r0()==true` means "licensed / owns Pro" (proven by `isUnlockedCommon`, which unlocks a Pro camera on
 * `isPRO() && r0()`), so `f()==true` = genuine and the blocking, exit-only "your version has been
 * cracked … the application will be automatically withdrawn" popup (`we/i.d()` -> string
 * `pirate_pop_text`, re-shown on every activity, Exit button = `Process.killProcess`+`System.exit`)
 * fires when `f()==false`.
 *
 * Because Morphe re-signs the APK, `r0()`/`f()` read false and this popup trips on ANY patched build —
 * this, not the unlock patch, closed OldRoll on launch. We force it "genuine" (true).
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

/**
 * Global "owns Pro / is VIP" flag: `manager.j.r0()Z` (no-arg, on the obfuscated singleton
 * `Lcom/lightcone/analogcam/manager/j;` obtained via `j.S()`). In `isUnlockedCommon` a Pro camera is
 * unlocked iff `isPRO() && j.r0()`, and the purchase/ownership UI reads this same state to show
 * "Pro / lifetime owned" and to drop watermarks. `isUnlockedCommon()`→true unlocks the *cameras*, but
 * the "lifetime owned" display + watermark gating hang off `r0()`, so we force it too.
 *
 * ⚠️ `j`/`r0` are R8-obfuscated single letters → version-pinned to 6.5.2, and (Windows case-collision)
 * `j.smali` can't be read from the extraction. So this is matched by exact DEX type + no-arg `Z` shape
 * and applied via `methodOrNull` — if it ever fails to resolve, the essential piracy bypass + camera
 * unlock still apply.
 */
internal object VipStatusFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lcom/lightcone/analogcam/manager/j;" && method.name == "r0"
    },
)
