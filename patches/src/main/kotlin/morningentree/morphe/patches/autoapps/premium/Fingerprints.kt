package morningentree.morphe.patches.autoapps.premium

import app.morphe.patcher.Fingerprint

internal object PurchasedMultipleResultFingerprint : Fingerprint(
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf("Ljava/util/List;"),
    strings = listOf("Purchased Multiple Result: "),
)

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
