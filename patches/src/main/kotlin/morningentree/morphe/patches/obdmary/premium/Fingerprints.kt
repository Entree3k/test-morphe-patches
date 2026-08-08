package morningentree.morphe.patches.obdmary.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * The app's three entitlement getters live together in one obfuscated preferences holder and are
 * read as a group at every gate. Their preference keys are not obfuscated, and no other static
 * no-argument boolean method reads them, so anchor on those.
 */

/** Owns the one-off Diagnostics edition (also honoured through the Russian billing path). */
internal object IsDiagnosticsEditionOwnedFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("IS_DIAGNOSTICS_EDITION_OWNED", "RUS_IS_DIAGNOSTICS_EDITION_OWNED"),
)

/** True while the install is still the unpaid tier; it defaults to true. */
internal object IsFreeAppFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("IS_FREE_APP", "RUS_IS_FREE_APP"),
)

/** Holds the full-app subscription. */
internal object IsFullAppSubscriptionPurchasedFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("IS_FULL_APP_SUBS_PURCHASED"),
)
