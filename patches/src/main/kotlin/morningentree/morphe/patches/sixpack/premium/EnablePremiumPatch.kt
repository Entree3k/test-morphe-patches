package morningentree.morphe.patches.sixpack.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.sixpack.shared.Constants
import morningentree.morphe.util.returnEarly

private const val CONTEXT = "Landroid/content/Context;"

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Six Pack in 30 Days premium — removes ads and unlocks all workouts and plans.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Recover the (obfuscated) billing-helper class from the matched master gate.
        val helperType = RemoveAdsGateFingerprint.method.definingClass

        // The helper's premium gates are the boolean methods that take either no arguments
        // (the "any subscription owned" check) or a single Context (the master remove-ads
        // aggregate and the cached remove_ads preference). Feature and ad-gate sites call each
        // directly, so force them all to true. The only other boolean method here takes a
        // String (a free-trial-offer pricing check) and is left untouched.
        classDefForEach { classDef ->
            if (classDef.type != helperType) return@classDefForEach

            mutableClassDefBy(classDef).methods
                .filter { method ->
                    method.returnType == "Z" &&
                        method.parameterTypes.map { it.toString() }.let {
                            it.isEmpty() || it == listOf(CONTEXT)
                        }
                }
                .forEach { it.returnEarly(true) }
        }
    }
}
