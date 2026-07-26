package morningentree.morphe.patches.vocabulary.premium

import app.morphe.patcher.Fingerprint

// Targets the app-wide premium gate in the obfuscated subscription manager
// (com.hrd.managers.S1). It is the only Z-returning, parameter-less method that
// reads the SharedPreferences flag "premium_com.hrd.vocabulary" (via getBoolean),
// combining it with the Supabase premium flag and the trial check to produce the
// single "is the user premium" answer consumed from ~100 direct call sites.
//
// The obfuscated method name drifts between versions (L0()Z in 5.4.0, N0()Z in
// 5.5.1), so it must NOT be fingerprinted by name. The prefs key embeds the
// package name and is stable across releases, making it the reliable anchor.
internal object IsUserPremiumFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("premium_com.hrd.vocabulary"),
)

// The premium SETTER, S1.Z1(Z)V. It writes both the "premium_com.hrd.vocabulary"
// prefs flag AND the premium MutableStateFlow that the Compose UI observes
// reactively (e.g. the word detail / "more examples" unlock screen zc). The
// RevenueCat purchase sync (com.hrd.managers.n) calls this with the real status,
// pushing the flow back to false for a non-subscriber — which re-locks those
// reactive screens even though the synchronous gate (IsUserPremiumFingerprint) is
// forced true. Neutering the setter leaves the flow at its startup value (which the
// premium initializer sets to the now-always-true synchronous gate).
//
// IMPORTANT: this is matched by an exact class+name `custom` predicate, NOT by the
// prefs-key string. Both this setter and N0() contain that string, and a shared
// `strings` anchor let the resolver collide the two fingerprints onto N0() —
// injecting `return-void` into a Z-returning method, which is invalid bytecode and
// bricked app launch. The custom predicate guarantees this only ever hits Z1().
internal object SetUserPremiumFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Z"),
    custom = { method, classDef ->
        classDef.type == "Lcom/hrd/managers/S1;" && method.name == "Z1"
    },
)
