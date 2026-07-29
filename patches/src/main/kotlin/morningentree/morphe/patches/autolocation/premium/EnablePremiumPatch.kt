package morningentree.morphe.patches.autolocation.premium

import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.autolocation.shared.Constants
import morningentree.morphe.util.returnEarly
import java.util.logging.Logger

/**
 * AutoLocation (com.joaomgcd.autolocation) is a joaomgcd Tasker plugin. Its free/trial state is the
 * app-wide boolean `isLite()Z` (licensed/full => `false`). Every gating site reads it: the main
 * screen (`ActivityMain.isLite()` -> `q1/y.B`, the DirectPurchase path), every config/settings screen
 * via the common base `com/joaomgcd/common/billing/PreferenceActivitySingleInAppFullVersion.isLite()`
 * (-> `billing/z.e` -> `z.d`, the Google-Play-license path), the Tasker config activities, and the
 * obfuscated presenter base.
 *
 * The two backends (DirectPurchase vs Play LVL) and the per-app class letters are obfuscated and drift,
 * but the *method name* `isLite` is a stable joaomgcd semantic that is kept un-obfuscated everywhere.
 * So instead of chasing either backend we force every `isLite()Z` gate to report the full version.
 */
@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks AutoLocation's full version by forcing every lite/trial (isLite) check to " +
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

        logger.info("AutoLocation Enable Premium: forced $patched isLite() gate(s) to full version.")
    }
}
