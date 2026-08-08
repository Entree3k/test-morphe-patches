package morningentree.morphe.patches.eobdfacile.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * The single license-level getter. It reads token/checksum pairs out of the "LSNV" preferences file
 * and verifies each one, returning 3 (Expert) when the `TP`/`CP` or `TB`/`CB` + `TU`/`CU` pair
 * validates, 2 (Premium) for `TB`/`CB` alone and 0 when nothing does. The class and method names
 * are obfuscated, but the preference file and its key names are not.
 *
 * Both callers push the result straight into the native engine through `APJ.BE(level, ...)`, which
 * is what actually gates the paid features, so this is the only place the level is decided.
 */
internal object LicenseLevelFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "I",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("LSNV", "TP", "CP", "TB", "CB", "TU", "CU"),
)
