package morningentree.morphe.patches.fylo.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * `com.aeroloom.fylofilemanager.data.billing.DebugFeatureUnlock.isActive(Context)`
 * — the app's own master "unlock everything" switch.
 *
 * In a release build it returns a hardcoded `false`:
 * ```smali
 * .method public final isActive(Landroid/content/Context;)Z
 *     const-string p0, "context"
 *     invoke-static {p1, p0}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullParameter(...)
 *     const/4 p0, 0x0
 *     return p0
 * .end method
 * ```
 *
 * `BillingManager.<init>` calls this and stores the result in the `debugUnlocked`
 * field. When `debugUnlocked` is true the manager seeds `_entitlement = LIFETIME`
 * and `_isPro = true`, and `setEntitlement()` keeps them pinned that way on every
 * billing refresh — so forcing this one method to return true unlocks every Pro
 * and AI feature app-wide (see the class's own `notice`: "Debug build: every Pro
 * and AI feature is unlocked for testing").
 *
 * Pinned by the stable (non-obfuscated) class type, method name and
 * `(Context)Z` shape.
 */
internal object DebugFeatureUnlockIsActiveFingerprint : Fingerprint(
    definingClass = "Lcom/aeroloom/fylofilemanager/data/billing/DebugFeatureUnlock;",
    name = "isActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;"),
)
