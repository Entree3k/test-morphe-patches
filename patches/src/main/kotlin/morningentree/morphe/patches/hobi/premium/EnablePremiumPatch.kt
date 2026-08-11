package morningentree.morphe.patches.hobi.premium

import app.morphe.patcher.patch.rawResourcePatch
import morningentree.morphe.patches.hobi.shared.Constants
import morningentree.morphe.patches.shared.misc.hermes.hermesPatch

/**
 * `LoadConstTrue r0; Ret r0` in Hermes bytecode v98 (`0x95` = LoadConstTrue,
 * `0x76` = Ret). The [hermesPatch] helper zero-pads this to the length of the
 * search pattern, so the whole matched function body is overwritten: the first
 * two instructions become "return true" and everything after the `Ret` is dead
 * code (decodes as `Unreachable`). Function length is unchanged, so no offsets
 * shift and the bundle only needs its trailing SHA-1 footer recomputed.
 */
const val RETURN_TRUE = "95 00 76 00"

/**
 * Unlocks Hobi Pro.
 *
 * ## App type: React Native + Expo, Hermes bytecode (v98)
 *
 * Hobi's `com/hobi/android` smali is only RN scaffolding; all business logic —
 * including the premium decision — lives in the compiled JS bundle
 * `assets/index.android.bundle` (Hermes magic `C6 1F BC 03`). Billing is
 * `react-native-iap` (Play Billing + Amazon IAP), which makes no entitlement
 * decision itself, so the gate is JS state.
 *
 * ## Target: the `isPremiumActive` function (Hermes function #18325)
 *
 * The whole UI gates Pro through a single function attached as the
 * `isPremiumActive` property. Disassembled (v98), it reads a property chain off
 * its argument and returns a boolean:
 *
 * ```
 * LoadParam        r1, arg1
 * LoadConstNull    r0
 * JStrictEqual     …                 ; arg == null?
 * GetById          r1, r1, 'sub…'
 * GetById          r1, r1, '…'
 * GetById          r2, r1, '…'
 * LoadConstFalse   r1
 * JmpFalse         …
 * GetById          r1, r2, '…'
 * Ret              r1
 * LoadConstUndefined r0
 * Ret              r0
 * ```
 *
 * Overwriting its entry with `LoadConstTrue r0; Ret r0` forces every premium
 * check to see `true`, regardless of the (server-driven) subscription state —
 * which is why we patch the JS gate directly rather than spoofing a purchase
 * (react-native-iap purchases are typically validated server-side).
 *
 * ## ⚠️ Version-specific byte pattern
 *
 * The 44-byte signature bakes in this build's Hermes string-table ids and
 * property-cache indices, which re-number whenever the JS bundle is recompiled.
 * It is verified unique in 3.4.0; expect it to drift on updates and re-derive
 * from a fresh disassembly (find function `isPremiumActive`, read its file
 * offset, dump the bytes).
 */
@Suppress("unused")
val enablePremiumPatch = rawResourcePatch(
    name = "Enable Premium",
    description = "Unlocks Hobi Pro.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    dependsOn(hermesPatch {
        // Hermes function #18325 "isPremiumActive" (44 bytes) @ 0x00668315 in 3.4.0.
        val isPremiumActive =
            "89 01 01 94 00 D3 23 01 00 45 01 01 00 0D 9D 45 01 01 01 E9 87 " +
                "45 02 01 02 31 93 96 01 B2 09 02 45 01 02 03 29 91 76 01 93 00 76 00" to
                RETURN_TRUE

        setOf(isPremiumActive)
    })
}
