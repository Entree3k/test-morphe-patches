package morningentree.morphe.patches.smashhit.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess

private const val ANDROID_STORE = "Lcom/mediocre/smashhit/AndroidStore;"
private const val COMMAND_HANDLER = "Lcom/mediocre/smashhit/CommandHandler;"
private const val COMMAND_MODEL = "Lcom/mediocre/smashhit/CommandThreadsafeModel;"
private const val ATOMIC_BOOLEAN = "Ljava/util/concurrent/atomic/AtomicBoolean;"

/**
 * `AndroidStore.ownsPremiumProduct()` — the Java-side premium check, true when either the premium
 * or the dynamic premium sku is in the owned set.
 */
internal object OwnsPremiumProductFingerprint : Fingerprint(
    name = "ownsPremiumProduct",
    returnType = "Z",
    parameters = emptyList(),
    custom = { _, classDef -> classDef.type == ANDROID_STORE },
)

/**
 * `CommandThreadsafeModel.isProductOwned(String)` — the owned-sku set lookup that both
 * [OwnsPremiumProductFingerprint] and the `isproductowned` JNI command go through. Declared
 * synchronized, so its body starts with a `monitor-enter`.
 */
internal object IsProductOwnedFingerprint : Fingerprint(
    name = "isProductOwned",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    custom = { _, classDef -> classDef.type == COMMAND_MODEL },
)

/**
 * The `hasrefreshedownedproducts` command handler. The engine polls it before it will ever ask
 * `isproductowned`, so without this the ownership check is never reached on a patched app.
 *
 * It is a synthetic lambda with an obfuscation-proof shape only in the field it reads — a dozen
 * sibling lambdas share the same signature and the same `AtomicBoolean.get()` + `boolToString()`
 * call pair, so anchor on the field itself.
 */
internal object HasRefreshedOwnedProductsFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    parameters = listOf("[Ljava/lang/String;"),
    filters = listOf(
        fieldAccess(
            definingClass = COMMAND_MODEL,
            name = "hasRefreshedOwnedProducts",
            type = ATOMIC_BOOLEAN,
        ),
    ),
    custom = { _, classDef -> classDef.type == COMMAND_HANDLER },
)

/**
 * The `storeisrestored` command handler — the restore-purchase result the engine reads after a
 * restore flow. Same lambda shape as [HasRefreshedOwnedProductsFingerprint], anchored on its field.
 */
internal object IsPremiumProductRestoredFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    parameters = listOf("[Ljava/lang/String;"),
    filters = listOf(
        fieldAccess(
            definingClass = COMMAND_MODEL,
            name = "isPremiumProductRestored",
            type = ATOMIC_BOOLEAN,
        ),
    ),
    custom = { _, classDef -> classDef.type == COMMAND_HANDLER },
)
