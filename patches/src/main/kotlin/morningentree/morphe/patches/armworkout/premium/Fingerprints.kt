package morningentree.morphe.patches.armworkout.premium

import app.morphe.patcher.Fingerprint

/**
 * Arm Workout keeps its "purchased / remove-ads" state in an obfuscated Kotpref config
 * (v2.4.3: `Lod/c;`, extending the Kotpref base `Lw2/i;`). The billing purchase handler
 * (`Ldc/b$a;->a(...)`) checks ownership of the stable SKU
 * "armworkout.armworkoutformen.armexercises.premiumyearly" and writes the result into a
 * Boolean preference delegate held in field `l` (`Ly2/b;`, `$$delegatedProperties` index 2).
 * The generated getter for that property (v2.4.3: `H()Z`) reads that same delegate and is
 * called at ~13 feature/ad-gate sites — forcing it true unlocks premium everywhere.
 *
 * The config class and getter names are obfuscated and therefore version-specific; that is
 * acceptable here because [Constants.COMPATIBILITY] pins the exact build (2.4.3) this was
 * verified against, so the fingerprint can only ever apply to that build. We match on the
 * exact class type and getter signature verified in smali.
 */
internal object PremiumStateGetterFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lod/c;" && method.name == "H"
    },
)
