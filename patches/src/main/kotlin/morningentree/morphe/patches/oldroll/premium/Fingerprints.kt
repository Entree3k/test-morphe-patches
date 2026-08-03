package morningentree.morphe.patches.oldroll.premium

import app.morphe.patcher.Fingerprint

/**
 * OldRoll (com.accordion.analogcam, code under com.lightcone.analogcam) gates every camera behind
 * the `AnalogCamera` model's unlock family. Verified in 6.5.2, the whole family funnels through one
 * private base method:
 *
 *   isUnlockedCommon()            <- the base gate (VIP / per-camera purchase / limited-free / promos)
 *     ^ isUnlockedWithoutFreeUse()
 *         ^ isUnlocked()
 *             ^ isUnlockedAndCanUse()
 *             ^ isUnlockedWithBFreeUse()
 *     ^ isUnlockedWithoutCaptureDcrUnlock()
 *
 * Forcing `isUnlockedCommon()` to return true makes every `AnalogCamera` instance report unlocked,
 * so every camera in the picker is selectable and usable.
 *
 * The billing/VIP state itself lives in obfuscated managers (`manager.j.r0()` = global VIP,
 * `manager.j.j0(id)` = per-camera purchase) whose names drift every release. The `AnalogCamera`
 * model, by contrast, keeps its real (un-obfuscated) class and method names, so we anchor there with
 * an exact class+method match rather than any obfuscated name or fragile instruction pattern.
 */
internal object IsCameraUnlockedFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lcom/lightcone/analogcam/model/camera/AnalogCamera;" &&
            method.name == "isUnlockedCommon"
    },
)
