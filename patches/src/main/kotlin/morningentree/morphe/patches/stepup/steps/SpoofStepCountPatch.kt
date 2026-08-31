package morningentree.morphe.patches.stepup.steps

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import morningentree.morphe.patches.stepup.shared.Constants
import java.util.logging.Logger

@Suppress("unused")
val spoofStepCountPatch = bytecodePatch(
    name = "Spoof step count",
    description = "Multiplies your real step count by a chosen factor (2x/3x/4x/5x/10x) at the single " +
        "point every tracker source funnels through. Boost is consistent across the in-app display, " +
        "local storage and the leaderboard upload. Proportional to real movement.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    val multiplier by stringOption(
        key = "stepMultiplier",
        default = "2",
        values = mapOf(
            "2x (recommended — most plausible)" to "2",
            "3x" to "3",
            "4x" to "4",
            "5x" to "5",
            "10x (aggressive — easiest to flag)" to "10",
        ),
        title = "Step multiplier",
        description = "How much to multiply your real steps by. Higher factors are more likely to " +
            "look implausible to the app's server-side anomaly checks — 2x is the safest choice.",
        required = true,
    ) { it in setOf("2", "3", "4", "5", "10") }

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        // Constrained by the option validator to {2,3,4,5,10}; all fit mul-int/lit8's -128..127 range.
        val factor = multiplier?.toIntOrNull() ?: 2

        // Inject at - index 0: multiply the incoming step count (p8, an Integer) by `factor`, then
        // let the existing `move-object/from16 v3, p8` pick up the boosted value. p8 is null-guarded
        // so the app's existing "trackerSteps is null" failure path is left untouched. At method
        // entry v0 is a free local (.locals 11, none assigned yet), used here as scratch.
        TrackerStepMergeFingerprint.method.addInstructions(
            0,
            """
                if-eqz p8, :morphe_skip_multiplier
                invoke-virtual/range {p8 .. p8}, Ljava/lang/Integer;->intValue()I
                move-result v0
                mul-int/lit8 v0, v0, $factor
                invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object p8
                :morphe_skip_multiplier
                nop
            """.trimIndent(),
        )

        logger.info("Step Up: step count multiplier set to ${factor}x at GenericTrackerHelper.")
    }
}
