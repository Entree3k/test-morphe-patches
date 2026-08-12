package morningentree.morphe.patches.habitica.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.habitica.shared.Constants
import morningentree.morphe.util.returnEarly

/**
 * Marks the Habitica account as an active subscriber on the client.
 *
 * The two client-side gates are forced true:
 *   - `SubscriptionPlan.isActive()Z` — the subscription validity check
 *   - `User.isSubscribed()Z` — `purchased.plan.isActive()`, the app-wide gate
 *
 * This flips every UI/feature that is decided locally (subscriber cosmetics,
 * hidden "subscribe" prompts, subscriber-only client toggles).
 *
 * ## Important: Habitica is server-authoritative
 *
 * The real subscription economy — monthly gems, Mystic Hourglasses, the gems-
 * for-gold shop, increased drop caps — is granted by the Habitica server against
 * the actual account, and is NOT unlocked by a client patch. Treat this as a
 * "client subscriber status" unlock, not a grant of server-side benefits.
 */
@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Marks the account as a subscriber on the client (server-side subscriber " +
        "benefits like gems/hourglasses are granted by Habitica's server and are not affected).",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        SubscriptionPlanIsActiveFingerprint.method.returnEarly(true)
        UserIsSubscribedFingerprint.method.returnEarly(true)
    }
}
