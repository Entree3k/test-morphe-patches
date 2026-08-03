package morningentree.morphe.patches.oldroll.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.oldroll.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val unlockAllCamerasPatch = bytecodePatch(
    name = "Unlock All Cameras",
    description = "Unlocks every OldRoll camera and lifetime Pro, spoofs the app's signature/license " +
        "verdict to \"genuine\", and disables the modified-app (anti-piracy) popup that otherwise " +
        "closes the re-signed build on launch.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // The signature/license verdict and the VIP/ownership flag are the SAME method:
        //   we/i.f() -> O4/e.a() -> app/a.b() -> app/a.p() -> manager/j.r0()
        // In AnalogCamera.isUnlockedCommon a Pro camera unlocks iff `isPRO() && r0()`, i.e. r0()==true
        // means "licensed / owns Pro" (genuine), and the "your version has been cracked" popup fires
        // when r0()==false. So forcing r0() true is the master fix: spoofs the signature verification
        // to genuine (no popup), unlocks Pro cameras, and marks lifetime as owned + drops watermarks.
        //
        // Guarded with methodOrNull because `j`/`r0` are R8-obfuscated single letters (and the lowercase
        // `j.smali` can't be read from a Windows extraction) — if it ever fails to resolve, the
        // self-contained belt-and-suspenders below still open the app and unlock the cameras.
        VipStatusFingerprint.methodOrNull?.returnEarly(true)

        // Belt-and-suspenders 1 — no-op the pirate-popup scheduler so the "cracked version" dialog can
        // never be posted/shown (its Exit button calls Process.killProcess + System.exit). This alone
        // guarantees the app opens regardless of the verdict's value.
        PiracyPopupSchedulerFingerprint.method.addInstructions(0, "return-void")

        // Belt-and-suspenders 2 — force the license verdict `we/i.f()` to "genuine" (true) for any other
        // consumer that reads it. (true == licensed here, matching r0()==true above.)
        PiracyVerdictFingerprint.method.returnEarly(true)

        // Belt-and-suspenders 3 — force the verified base camera gate true so every camera unlocks even
        // if the obfuscated r0() force above is skipped.
        IsCameraUnlockedFingerprint.method.returnEarly(true)
    }
}
