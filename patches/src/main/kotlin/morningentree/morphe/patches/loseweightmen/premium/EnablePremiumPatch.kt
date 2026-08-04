package morningentree.morphe.patches.loseweightmen.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.loseweightmen.shared.Constants
import morningentree.morphe.util.returnEarly

private const val CONTEXT = "Landroid/content/Context;"

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Lose Weight App for Men premium — removes ads and unlocks all workouts and plans.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Recover the (obfuscated) MyIabHelper class from the matched static gate.
        val helperType = IsPremiumGateFingerprint.method.definingClass

        // MyIabHelper declares exactly three premium checks, all `(Context)Z`: the static
        // remove-ads/premium aggregate plus the two instance subscription checks it delegates
        // to. Feature and ad-gating call sites invoke each of them directly, so force every
        // `(Context)Z` member of this class to return true.
        classDefForEach { classDef ->
            if (classDef.type != helperType) return@classDefForEach

            mutableClassDefBy(classDef).methods
                .filter { method ->
                    method.returnType == "Z" &&
                        method.parameterTypes.map { it.toString() } == listOf(CONTEXT)
                }
                .forEach { it.returnEarly(true) }
        }
    }
}
