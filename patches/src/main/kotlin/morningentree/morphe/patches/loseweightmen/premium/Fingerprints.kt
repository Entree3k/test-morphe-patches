package morningentree.morphe.patches.loseweightmen.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Anchors the obfuscated billing helper `MyIabHelper.kt` (v2.4.37: `LTd/p;`).
 *
 * The class exposes three premium gates, all with signature `(Landroid/content/Context;)Z`:
 *   - a public *static* "remove ads / is premium" aggregate (the method matched here),
 *   - an instance "subscription active" check,
 *   - an instance "within subscription period" check.
 *
 * [enablePremiumPatch] reaches the other two by enumerating the class, so we only need to
 * land on one member to recover the (obfuscated) class type.
 *
 * We match the static gate by its unique encrypted-SKU string constant (a data literal that
 * is stable for this build) plus its exact signature — never by the obfuscated class/method
 * names, which drift between releases.
 *
 * Note: Morphe matches [accessFlags] exactly, so every flag the method carries must be listed.
 * The target `C(Landroid/content/Context;)Z` is `public static final`, hence [AccessFlags.FINAL].
 */
internal object IsPremiumGateFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("LmUmbARzLXc2aQFoTS4Kbz9lOGUjZ110EXA-ZgByImUtLj9lAmcgdD9vFXNfbxRtKW5hci9tWnYVYSpz"),
)
