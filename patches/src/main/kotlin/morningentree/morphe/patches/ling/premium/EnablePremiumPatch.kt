package morningentree.morphe.patches.ling.premium

import app.morphe.patcher.patch.rawResourcePatch
import morningentree.morphe.patches.ling.shared.Constants
import morningentree.morphe.patches.shared.misc.hermes.hermesPatch
import morningentree.morphe.patches.shared.misc.pairip.getStripPairipPatch

const val RETURN_TRUE = "78 00 5C 00"

@Suppress("unused")
val enablePremiumPatch = rawResourcePatch(
    name = "Enable Premium",
    description = "Unlocks Ling Pro"
) {
    compatibleWith(Constants.COMPATIBILITY)

    // Ling 8.9.0 newly ships Pairip and virtualizes core lifecycle methods
    // (MainApplication.onCreate, MainActivity.onCreate, Firebase/GMS auth callbacks, ...)
    // into the Pairip VM — their smali bodies are now reflective `Method.invoke` calls on
    // static fields that the VM populates at startup. A plain signature no-op is therefore
    // not enough: gutting the VM would leave those fields null and NPE on launch.
    //
    // getStripPairipPatch de-virtualizes instead: it restores the real method bodies from
    // the embedded Pairip DEX assets, no-ops SignatureCheck/LicenseClient/VMRunner, replaces
    // libpairipcore.so with a stub, applies the native .data patches to libhermes/libreactnative,
    // and re-injects the reflection fields via the generated `ling` extension (patches/pairip/ling.json).
    dependsOn(getStripPairipPatch("ling"))

    // The Pro gate itself lives in the Hermes JS bundle, not the VM. One Redux selector,
    // (state) => state.payments.isProUser, compiled to a 17-byte function; overwrite its
    // first 4 bytes with LoadConstTrue; Ret so it always returns true. String-table ids
    // drift every release, so this pattern is re-derived per version (8.9.0 here).
    dependsOn(hermesPatch {
        val selectIsProUser =
            "6C 00 01 37 00 00 01 A0 9D 37 00 00 02 81 E6 5C 00" to RETURN_TRUE

        setOf(selectIsProUser)
    })
}
