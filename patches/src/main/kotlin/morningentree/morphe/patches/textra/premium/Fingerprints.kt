package morningentree.morphe.patches.textra.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * The getter of the "lc" license preference (default -1), which is the single source of truth for
 * Pro: the app reads it as an int where 1 means Pro, 0 means the free tier and -1 is the initial
 * trial. Its two sibling helpers ("is free" and "is Pro") and the bridge getter all delegate here,
 * so this covers every read.
 *
 * Everything about the class is obfuscated, but the shape is not: this is the only
 * `public final declared-synchronized` no-argument method returning [Integer] in the app — the two
 * sibling preference getters and the one in ML Kit are all non-final. The [Integer] calls pin the
 * body, so a rename that changes the shape fails the patch instead of matching something else.
 */
internal object LicensePreferenceGetterFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.DECLARED_SYNCHRONIZED),
    returnType = "Ljava/lang/Integer;",
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = "Ljava/lang/Integer;", name = "intValue"),
        methodCall(definingClass = "Ljava/lang/Integer;", name = "valueOf"),
    ),
)
