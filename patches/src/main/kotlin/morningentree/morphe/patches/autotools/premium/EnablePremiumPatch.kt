package morningentree.morphe.patches.autotools.premium

import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.autotools.shared.Constants
import morningentree.morphe.util.returnEarly
import java.util.logging.Logger

/**
 * AutoTools (com.joaomgcd.autotools) is a joaomgcd Tasker plugin sharing the same common billing/
 * license library as AutoLocation/AutoAppsHub. Its free/trial state is the app-wide boolean
 * `isLite()Z` (licensed/full => `false`), read by the main screen, every config/settings screen via
 * the common base `com/joaomgcd/common/billing/PreferenceActivitySingleInAppFullVersion.isLite()`, the
 * Tasker config activities, and the obfuscated presenter base.
 *
 * The underlying license backends (DirectPurchase vs Google-Play LVL) and the per-app class letters
 * are obfuscated and drift, but the *method name* `isLite` is a stable joaomgcd semantic kept
 * un-obfuscated everywhere, so we force every `isLite()Z` gate to report the full version.
 */
@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks AutoTools' full version by forcing every lite/trial (isLite) check to " +
        "report the paid full version.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val logger = Logger.getLogger(this::class.java.name)
        var patched = 0

        fun isLiteGate(name: String, returnType: String, params: List<*>) =
            name == "isLite" && returnType == "Z" && params.isEmpty()

        classDefForEach { classDef ->
            if (classDef.methods.none { isLiteGate(it.name, it.returnType, it.parameterTypes) }) {
                return@classDefForEach
            }

            mutableClassDefBy(classDef).methods.forEach { method ->
                if (!isLiteGate(method.name, method.returnType, method.parameterTypes)) return@forEach
                // Skip abstract/native declarations (no body to rewrite).
                if (method.instructionsOrNull == null) return@forEach

                method.returnEarly(false)
                patched++
            }
        }

        logger.info("AutoTools Enable Premium: forced $patched isLite() gate(s) to full version.")
    }
}
