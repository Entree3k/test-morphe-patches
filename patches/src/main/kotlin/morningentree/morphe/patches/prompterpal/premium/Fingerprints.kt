package morningentree.morphe.patches.prompterpal.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * The app's own SubscriptionManager entitlement-mapping method
 * (`Lt00/u;->d(Lcom/revenuecat/purchases/CustomerInfo;)V`).
 *
 * It reads the RevenueCat CustomerInfo, walks `getEntitlements().getActive()`, and pushes the
 * derived premium state into three `MutableStateFlow<Boolean>` fields via a
 * `(Z, StateFlow, Object)V` helper — in order: hasLifetime, isTrial, hasSubscription. The UI
 * (editor, settings) collects the read-only wrappers of these flows to gate premium features.
 *
 * This method runs at startup (RevenueCat getCustomerInfo callback), so forcing the pushes to
 * `true` unlocks premium even for accounts that never purchased.
 *
 * Anchored on the RevenueCat entitlement identifier strings, which are stable app config.
 */
internal object SubscriptionStateFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;"),
    strings = listOf(
        "entitlement_lifetime",
        "entitlement_six_months",
        "entitlement_yearly",
    ),
)
