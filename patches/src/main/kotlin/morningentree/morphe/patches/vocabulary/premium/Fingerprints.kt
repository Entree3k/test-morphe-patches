package morningentree.morphe.patches.vocabulary.premium

import app.morphe.patcher.Fingerprint

// Targets the app-wide premium gate in the obfuscated subscription manager
// (com.hrd.managers.S1). It is the only Z-returning, parameter-less method that
// reads the SharedPreferences flag "premium_com.hrd.vocabulary" (via getBoolean),
// combining it with the Supabase premium flag and the trial check to produce the
// single "is the user premium" answer consumed from ~100 call sites across the app.
//
// The obfuscated method name drifts between versions (L0()Z in 5.4.0, N0()Z in
// 5.5.1), so it must NOT be fingerprinted by name. The prefs key embeds the
// package name and is stable across releases, making it the reliable anchor.
internal object IsUserPremiumFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("premium_com.hrd.vocabulary"),
)
