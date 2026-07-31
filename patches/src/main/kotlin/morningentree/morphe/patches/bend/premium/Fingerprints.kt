package morningentree.morphe.patches.bend.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * The RevenueCat-backed "get active subscription" resolver in `com.bowerydigital.bend`
 * (obfuscated `wg5.a(Continuation)`).
 *
 * Reads `customerInfo.getEntitlements().getActive().get("premium")`; if that entitlement is absent
 * it returns `null`, otherwise it builds and returns a `be(SubscriptionPlatform, managementUrl)`
 * holder. **Non-null == premium** across the app, and `MainActivity`'s
 * `UpdatedCustomerInfoListener` recomputes state from it on every RevenueCat push.
 *
 * Anchored on the three subscription-store domains it matches the management URL against — stable
 * string literals unique to this method. The app's own class/method names are R8-obfuscated and are
 * intentionally not referenced here; the holder type is recovered from the method at patch time.
 *
 * See bend-premium-findings.md.
 */
internal object ActiveSubscriptionFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("play.google.com", "apple.com", "paddle.com"),
    custom = { method, _ -> method.parameterTypes.size == 1 },
)
