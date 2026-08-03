package morningentree.morphe.patches.boosted.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Boosted gates every premium feature through a single boolean state holder (verified in 1.6.21 as
 * `w3.d`). It owns an RxRelay (`w3.d.c`, a `j7.b`) that carries the current "premium active" flag.
 * Every consumer reads that one relay:
 *   - imperative:  `PremiumViewModel` (`f.a()Z`) -> relay.getValue(), used by `Fragment.isPremiumActive()`
 *   - reactive:    `Fragment.observeIsPremiumActive()` returns the relay directly
 *   - reactive:    `g4.b` subscribes to the relay
 * So forcing the relay to always hold `true` unlocks the whole app.
 *
 * This fingerprint targets the state holder's constructor, which seeds the relay with
 * `Boolean.FALSE`. Anchored on the stable, un-obfuscated app class `BillingRepository` it receives
 * plus the unique WorkManager job name string — never the obfuscated `w3.d` name.
 */
internal object PremiumStateInitFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.CONSTRUCTOR),
    returnType = "V",
    parameters = listOf(
        "Lcom/boostedproductivity/billing/BillingRepository;",
        "L",
    ),
    strings = listOf("BILLING_REFRESH_WORKER"),
)

/**
 * The relay writer (verified in 1.6.21 as `w3.c.accept(Object)V`, an RxJava `Consumer`). It walks the
 * active-purchase list, sets a boolean to whether any purchase matches a premium SKU, boxes it with
 * `Boolean.valueOf(...)` and pushes it into the relay. Forcing that boolean to `true` makes every
 * emission report premium.
 *
 * Anchored on the stable premium product-id strings, not the obfuscated `w3.c` / `accept` names. The
 * combination of all three SKU strings in a single `accept(Object)V` is unique to this consumer.
 */
internal object PremiumStateWriteFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Ljava/lang/Object;"),
    strings = listOf(
        "inapp.boosted.premium",
        "subs.boosted.premium.yearly",
        "subs.boosted.premium.monthly",
    ),
)
