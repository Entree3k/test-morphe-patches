package morningentree.morphe.patches.prompterpal.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

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
