package morningentree.morphe.patches.vocabulary.premium

import app.morphe.patcher.Fingerprint

// Vocabulary exposes premium two ways, both in the obfuscated subscription manager
// com.hrd.managers.S1, both anchored by the stable prefs key "premium_com.hrd.vocabulary":
//
//  1. N0()Z  — the synchronous aggregate gate (prefs flag OR supabase OR trial),
//     consumed from ~100 direct call sites (settings, most feature checks).
//  2. a MutableStateFlow<Boolean> that the Compose UI observes reactively (e.g. the
//     word "more examples" screen). It is written by S1's premium setter Z1(Z)V,
//     which the RevenueCat/purchase sync calls with the REAL status — so for a
//     non-subscriber it gets set back to false even after N0() is forced true.
//
// Forcing N0() alone leaves the reactive flow false, which is why settings showed
// "Premium" but example unlocks stayed gated. We patch both.

// Aggregate gate: the only Z-returning, parameter-less method carrying the prefs key.
internal object IsUserPremiumFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("premium_com.hrd.vocabulary"),
)

// Premium setter (Z1(Z)V): the only V-returning, single-boolean method carrying the
// prefs key. It updates both the premium StateFlow and the prefs flag from its
// argument. The purchase sync calls it with the real status, which is what re-locks
// the reactive UI. Neutering it (return-void) leaves the flow at its startup-true
// value so premium state can never be pushed back to false.
internal object SetUserPremiumFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Z"),
    strings = listOf("premium_com.hrd.vocabulary"),
)
