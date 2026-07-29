package morningentree.morphe.patches.autoapps.premium

import app.morphe.patcher.Fingerprint

// Play-Billing purchase check. Every "is purchased" entrypoint (U/V/I/J/Z/a0) funnels
// through R -> per-SKU check -> and reduces the per-SKU booleans through this method,
// which returns Boolean.TRUE if any SKU is owned. This is what the hub UI reads to mark a
// project/app locked vs unlocked. Anchored on the stable developer log string (the class
// and method names are obfuscated single letters).
internal object PurchasedMultipleResultFingerprint : Fingerprint(
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf("Ljava/util/List;"),
    strings = listOf("Purchased Multiple Result: "),
)

// The three static license reads in the shared licensing library that other installed
// AutoApps query (via IPC) to learn whether they are licensed. Names are stable public API
// of com.joaomgcd.common (never obfuscated), so match by class + method name.
internal object IsLicensedFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;", "Z"),
    custom = { method, classDef ->
        classDef.type == "Lcom/joaomgcd/common/license/ServiceCheckLicense;" &&
                method.name == "isLicensed"
    },
)

internal object IsLicensedDefaultFalseFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;"),
    custom = { method, classDef ->
        classDef.type == "Lcom/joaomgcd/common/license/ServiceCheckLicense;" &&
                method.name == "isLicensedDefaultFalse"
    },
)

internal object IsLicensedDefaultTrueFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;"),
    custom = { method, classDef ->
        classDef.type == "Lcom/joaomgcd/common/license/ServiceCheckLicense;" &&
                method.name == "isLicensedDefaultTrue"
    },
)
