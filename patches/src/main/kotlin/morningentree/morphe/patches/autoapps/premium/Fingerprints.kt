package morningentree.morphe.patches.autoapps.premium

import app.morphe.patcher.Fingerprint

// The three static license reads in the shared licensing library. Names are stable public
// API of com.joaomgcd.common (never obfuscated), so match by class + method name.
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
