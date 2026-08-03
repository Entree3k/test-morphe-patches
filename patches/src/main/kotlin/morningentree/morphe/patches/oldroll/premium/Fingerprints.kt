package morningentree.morphe.patches.oldroll.premium

import app.morphe.patcher.Fingerprint

/**
 * OldRoll (com.accordion.analogcam, code under com.lightcone.analogcam) gates cameras through
 * `AnalogCamera.isUnlockedCommon()`, but that method is a **leaf**: forcing it true reports a camera as
 * ready even when its downloadable (hot-update) resources were never fetched — which crashed the app on
 * launch. Instead we force the **source** of premium: the global "user owns Pro / is VIP" flag.
 *
 * In `isUnlockedCommon()` a Pro camera is unlocked iff `isPRO() && manager.j.r0()`. `r0()Z` (no args,
 * on the obfuscated singleton `Lcom/lightcone/analogcam/manager/j;`, obtained via `j.S()`) is that
 * app-wide VIP flag. Forcing it true makes the whole app treat the user exactly as a paying VIP — the
 * same state a real purchaser is in — so every Pro camera unlocks through the app's normal path and its
 * resource-provisioning still runs (no leaf-level "unlocked but no assets" crash).
 *
 * ⚠️ `j` / `r0` are R8-obfuscated single-letter names → version-pinned to 6.5.2. (Matched by the exact
 * DEX type string + a no-arg `Z` shape; morphe reads the real DEX, so Windows' case-insensitive
 * `j`/`J` filename collision on the extracted smali is irrelevant at patch time.)
 */
internal object VipStatusFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lcom/lightcone/analogcam/manager/j;" && method.name == "r0"
    },
)
