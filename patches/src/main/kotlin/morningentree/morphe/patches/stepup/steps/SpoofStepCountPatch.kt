package morningentree.morphe.patches.stepup.steps

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import morningentree.morphe.patches.stepup.shared.Constants
import java.util.logging.Logger

/**
 * Multiplies the phone's measured step count by a chosen factor.
 *
 * Step Up reads today's cumulative steps from whichever tracker you selected (the "Phone"
 * option is Google's on-device Fitness Recording API; "Health Connect" is Google's Fit
 * successor) and every source funnels the fresh count through one method,
 * `GenericTrackerHelper.a(...)`, before it is displayed, stored and synced. This patch scales
 * that single value, so the boost is applied exactly once and stays internally consistent:
 * the number shown in-app, the number persisted locally, and the number uploaded to the
 * leaderboard are all the same multiple of your real steps.
 *
 * The factor may be fractional (e.g. 2.3, 2.5) — the scaling is done in float, so any decimal
 * works. A modest, slightly-irregular factor tends to look more human than a clean whole number.
 *
 * Consistency matters for staying under the radar. The app's own `InflatedStepsMonitor`
 * compares the client's stored step count against the server's — both of which now reflect
 * the multiplied value — so a uniform multiplier does not create the client/server divergence
 * it looks for. The multiplier is proportional (2.5x of zero is still zero), so it never
 * fabricates steps out of no movement, which is what the "no steps from sensor" watchdog flags.
 *
 * Caveats worth knowing: this is a social/competitive app with a server backend and anomaly
 * telemetry (reported to the operators via Crashlytics). A modest factor that still looks human
 * is far safer than a large one; 10x of a normal day is not a plausible human number and is the
 * kind of thing a backend fraud check can act on. Choose accordingly.
 */
@Suppress("unused")
val spoofStepCountPatch = bytecodePatch(
    name = "Spoof step count",
    description = "Multiplies your real step count by a chosen factor (whole or decimal, e.g. 2, 2.5, " +
        "3, 10) at the single point every tracker source funnels through. Boost is consistent across " +
        "the in-app display, local storage and the leaderboard upload. Proportional to real movement.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    val multiplier by stringOption(
        key = "stepMultiplier",
        default = "2",
        values = mapOf(
            "2x (recommended — most plausible)" to "2",
            "2.3x" to "2.3",
            "2.5x" to "2.5",
            "3x" to "3",
            "3.2x" to "3.2",
            "3.5x" to "3.5",
            "4x" to "4",
            "5x" to "5",
            "10x (aggressive — easiest to flag)" to "10",
        ),
        title = "Step multiplier",
        description = "How much to multiply your real steps by. Whole numbers or decimals are both " +
            "accepted (1.0–20.0). Higher factors are more likely to look implausible to the app's " +
            "server-side anomaly checks — a modest 2x–2.5x is the safest choice.",
        required = true,
    ) { value ->
        // Accept any decimal in a sane range; keeps the presets above plus custom entries valid.
        value?.toDoubleOrNull()?.let { it in 1.0..20.0 } == true
    }

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        // Validated to a Double in [1.0, 20.0] above. Encode it as its IEEE-754 float bit pattern
        // so the smali can load it with a plain `const` and multiply in float — this supports
        // fractional factors (2.3, 2.5, …) with no integer-overflow risk.
        val factor = multiplier?.toDoubleOrNull() ?: 2.0
        val factorBits = java.lang.Float.floatToRawIntBits(factor.toFloat())
        val factorHex = "0x%08x".format(factorBits)

        // Inject at index 0: multiply the incoming step count (p8, an Integer) by `factor` in float,
        // then let the existing `move-object/from16 v3, p8` pick up the boosted value. p8 is
        // null-guarded so the app's existing "trackerSteps is null" failure path is untouched. At
        // method entry v0 and v1 are free locals (.locals 11, none assigned yet), used as scratch.
        TrackerStepMergeFingerprint.method.addInstructions(
            0,
            """
                if-eqz p8, :morphe_skip_multiplier
                invoke-virtual/range {p8 .. p8}, Ljava/lang/Integer;->intValue()I
                move-result v0
                int-to-float v0, v0
                const v1, $factorHex
                mul-float v0, v0, v1
                float-to-int v0, v0
                invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object p8
                :morphe_skip_multiplier
                nop
            """.trimIndent(),
        )

        logger.info("Step Up: step count multiplier set to ${factor}x (float $factorHex) at GenericTrackerHelper.")
    }
}
