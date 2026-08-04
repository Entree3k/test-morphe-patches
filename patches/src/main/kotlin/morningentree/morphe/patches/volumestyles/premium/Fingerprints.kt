package morningentree.morphe.patches.volumestyles.premium

import app.morphe.patcher.Fingerprint

/**
 * The entitlement-state enum (verified in 5.2.1 as `Lqs;`, an obfuscated
 * `enum { UNKNOWN, UNPURCHASED, PENDING, PURCHASED, PURCHASED_AND_ACKNOWLEDGED }` — the same tombayley
 * billing lib as Super Status Bar). Matched by its five R8-stable constant-name strings (its own
 * `<clinit>`); the patch reads the enum's class type and the UNKNOWN/owned fields off it.
 */
internal object EntitlementStateEnumFingerprint : Fingerprint(
    returnType = "V",
    strings = listOf(
        "UNKNOWN",
        "UNPURCHASED",
        "PENDING",
        "PURCHASED",
        "PURCHASED_AND_ACKNOWLEDGED",
    ),
)

/**
 * The billing repository's per-SKU state setter (verified in 5.2.1 as `Lft;->k(String, qs)V`). It looks
 * up the SKU's `MutableStateFlow<qs>` in `ft.g` and sets it to the passed state; the reconciler calls
 * it with `UNPURCHASED` for every un-owned SKU, so it is the single write path that governs the flow
 * the whole UI observes.
 *
 * Anchored on: void return, `(String, <enum>)` params (the enum arg is obfuscated -> `"L"`), and the
 * unique log literal `"Unknown SKU "`. That string + this signature pin `ft.k` without its obfuscated
 * name. The patch forces its state argument to the owned constant.
 */
internal object SetEntitlementStateFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "L"),
    strings = listOf("Unknown SKU "),
)
