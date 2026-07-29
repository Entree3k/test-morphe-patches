package morningentree.morphe.patches.autoappshub.premium

import app.morphe.patcher.Fingerprint

/**
 * The per-app AutoApps-subscription gate (verified in 1.8.13 as `Lv2/s;->c(String)Ljava/lang/Boolean;`).
 *
 * Given an app's SKU/package it inspects the user's owned-purchase list for one of the AutoApps
 * subscriptions (`fullsub`, `fullsubextra`, `fullsubyearly`) or the app's own SKU, and returns
 * TRUE/FALSE (or null if purchase data isn't loaded yet). The hub's app-list adapter
 * (`Lv2/h;->n()Z` = `AutoApp.isUnlockedByItself() || c(pkg)`) reads this to decide whether each
 * AutoApp shows unlocked. Forcing it to TRUE makes every app appear owned via the subscription.
 *
 * Anchored on the stable subscription-SKU strings, not the obfuscated `v2/s`/`c` names. The three
 * strings also appear in the sibling `e(AutoAppsContainer, e)Ljava/lang/Object;`, but the
 * Boolean-return + single String-param signature uniquely selects `c()`.
 */
internal object SubscriptionUnlockFingerprint : Fingerprint(
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf("Ljava/lang/String;"),
    strings = listOf("fullsub", "fullsubextra", "fullsubyearly"),
)

/**
 * The AutoApps subscription master flag (verified in 1.8.13 as
 * `Lcom/joaomgcd/autoapps/BroadcastReceiverAutoAppsActionCheckLicenseResponse;->isLicensed(Context)Z`).
 *
 * Reads the cached `com.joaomgcd.autoapps.EXTRA_IS_LICENSED` pref that the hub sets when it confirms
 * the AutoApps subscription is active; a `true` result drives the `FullVersionUnlocked` event and the
 * `islicensed` broadcast. Forcing it true keeps the hub-wide "subscription active" state on.
 *
 * Anchored on the unique `EXTRA_IS_LICENSED` string. The setter and the receiver's `executeSpecific`
 * also carry the string, but only this getter returns `Z` from a single `Context` param.
 */
internal object AutoAppsLicensedFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("com.joaomgcd.autoapps.EXTRA_IS_LICENSED"),
)
