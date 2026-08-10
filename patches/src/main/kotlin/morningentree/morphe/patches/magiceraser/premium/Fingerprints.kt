package morningentree.morphe.patches.magiceraser.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * `com.duygiangdg.magiceraser.utils.F0.a()` (obfuscated class/method names) —
 * the app-wide "is subscribed / premium" gate.
 *
 * It reads the current subscription status (a `LiveData<String>` exposed by the
 * billing service `services.O`, persisted under the pref key
 * `"subscription_status"`) and returns:
 *   status in {"no_subscription", "unknown_subscription"} -> false (free user)
 *   any other value (a real subscription)                 -> true  (premium)
 *
 * ```smali
 * .method public static a()Z
 *     invoke-static {}, Lcom/duygiangdg/magiceraser/services/O;->h()...  # singleton
 *     iget-object ... ->c:Landroidx/lifecycle/u;                        # status LiveData
 *     ... LiveData;->e()  -> String
 *     const-string v1, "no_subscription"      / equals
 *     const-string v1, "unknown_subscription" / equals
 *     xor-int/2addr ...                        # NOT(no || unknown)
 *     return v0
 * ```
 *
 * Every gated screen routes through this static method — HomeActivity, all AI*
 * activities, BG/collage/expand editors and their brush panels (13 call sites) —
 * so forcing it to return true unlocks premium everywhere at once.
 *
 * Pinned by the two status-string literals plus the static no-arg boolean shape;
 * the obfuscated class name `F0` / method name `a` are intentionally not
 * referenced (they drift every release). The activities that also carry these
 * strings do so in non-static, parameterized methods, so the static + emptyList
 * shape uniquely selects this gate.
 */
internal object IsSubscribedFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = emptyList(),
    strings = listOf("no_subscription", "unknown_subscription"),
)
