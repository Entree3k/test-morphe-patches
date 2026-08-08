package morningentree.morphe.patches.pinout.premium

import app.morphe.patcher.Fingerprint

private const val ANDROID_STORE = "Lcom/mediocre/pinout/AndroidStore;"

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
 * `getStatusAsString()` stringifies `mPurchaseStatus`, whose values are the class' own
 * `STORE_WAITING` (1) / `STORE_SUCCEEDED` (2) / `STORE_FAILED` (3) constants. The native side reads
 * it through the `storegetstatus` JNI command.
 */
internal object GetStatusAsStringFingerprint : Fingerprint(
    name = "getStatusAsString",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    custom = { _, classDef -> classDef.type == ANDROID_STORE },
)
