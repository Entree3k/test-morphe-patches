package morningentree.morphe.patches.macrodroid.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

private const val PACKAGE_MANAGER = "Landroid/content/pm/PackageManager;"
private const val SIGNATURE = "Landroid/content/pm/Signature;"

/**
 * The persisted Pro flag getter on the (unobfuscated) Settings class. It reads the `vcp_count`
 * preference and reports Pro when it is a positive multiple of the app's magic modulus, and the Pro
 * UI state (the `tcc$c` object surfaced by the billing view model) is derived from it. Forcing it
 * true makes the whole app read as Pro, which also makes the server device-check teardown — which
 * only rewrites `vcp_count` — a no-op.
 *
 * The setter shares the `vcp_count` literal but returns void, so the boolean return type keeps this
 * anchored on the getter.
 */
internal object ProStatusFingerprint : Fingerprint(
    definingClass = "Lcom/arlosoft/macrodroid/settings/Settings;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("vcp_count"),
)

/**
 * The main app-signature check: `public static (Context)Z`, true when the running signature is NOT
 * one of the bundled originals (i.e. tampered). A re-signed APK trips it, so force it false.
 */
internal object SignatureCheckFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(definingClass = PACKAGE_MANAGER, name = "getPackageInfo"),
        methodCall(definingClass = SIGNATURE, name = "toCharsString"),
    ),
)

/**
 * The template-store signature check: an instance `public final (Context)Z` with the same
 * "official signature?" logic. Its result feeds the template-store API auth hash ("1" vs ""), so it
 * must report the original (false) for a re-signed build to keep community templates working.
 */
internal object TemplateStoreSignatureCheckFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(definingClass = PACKAGE_MANAGER, name = "getPackageInfo"),
        methodCall(definingClass = SIGNATURE, name = "toCharsString"),
    ),
)
