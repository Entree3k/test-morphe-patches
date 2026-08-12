package morningentree.morphe.patches.habitica.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// SubscriptionPlan.isActive()Z — the core client subscription check:
//   customerId != null && (dateTerminated == null || dateTerminated.after(now) == false) && active
// Read by User.isSubscribed() and the various gem/hourglass getters.
internal object SubscriptionPlanIsActiveFingerprint : Fingerprint(
    definingClass = "Lcom/habitrpg/android/habitica/models/user/SubscriptionPlan;",
    name = "isActive",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// User.isSubscribed()Z — purchased.plan.isActive(); the app-wide "is subscriber" gate.
internal object UserIsSubscribedFingerprint : Fingerprint(
    definingClass = "Lcom/habitrpg/android/habitica/models/user/User;",
    name = "isSubscribed",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)
