package morningentree.morphe.patches.superstatusbar.premium

import app.morphe.patcher.Fingerprint

/**
 * The entitlement-state enum (verified in 2.13.0 as `Leo;`, an obfuscated
 * `enum { UNKNOWN, UNPURCHASED, PENDING, PURCHASED, PURCHASED_AND_ACKNOWLEDGED }`).
 *
 * Super Status Bar (com.tombayley.statusbar) drives premium off a per-SKU
 * `MutableStateFlow<eo>` held in the billing repository (`Lro;`, field `g`). Every
 * premium feature the UI gates on observes that flow after it is mapped to a
 * `Boolean` by the single converter `ro.h(eo)Z` == "state == PURCHASED_AND_ACKNOWLEDGED".
 *
 * This fingerprint pins that enum via its five stable constant-name strings (the
 * enum's own `<clinit>`). The patch then reads the enum's class type and its
 * "owned" constant off the resolved `<clinit>`, and uses those to locate and force
 * the string-less `ro.h` converter — so nothing here depends on the obfuscated
 * class/field letters, which drift every release.
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
