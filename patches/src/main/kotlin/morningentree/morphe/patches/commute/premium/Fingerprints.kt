package morningentree.morphe.patches.commute.premium

import app.morphe.patcher.Fingerprint

private const val ANDROID_STORE = "Lcom/mediocre/commute/AndroidStore;"

/**
 * `isProductIdRestored(String)` just returns the `mIsPremiumProductRestored` flag; it ignores the
 * sku argument. The native side reaches it through the `storeisrestored,<sku>` JNI command to
 * decide whether premium is owned.
 */
internal object IsProductIdRestoredFingerprint : Fingerprint(
    name = "isProductIdRestored",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    custom = { _, classDef -> classDef.type == ANDROID_STORE },
)

/**
 * `getStatusAsString()` stringifies `mPurchaseStatus` (1 waiting / 2 succeeded / 3 failed) and is
 * read by the native side through the `storegetstatus` JNI command.
 */
internal object GetStatusAsStringFingerprint : Fingerprint(
    name = "getStatusAsString",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    custom = { _, classDef -> classDef.type == ANDROID_STORE },
)
