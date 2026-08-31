package morningentree.morphe.patches.stepup.steps

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * `com.thestepupapp.stepup.repositories.GenericTrackerHelper.a(...)` — the single,
 * universal "normalise today's step reading" entry point that EVERY step data source
 * funnels through (Phone/Fitness Recording API, Health Connect, Fitbit, Garmin,
 * Samsung Health). Verified: all of those `*DataSource` / `*Repository` classes invoke
 * this one static method.
 *
 * Real (deobfuscated-by-shape) signature:
 * ```smali
 * .method public static a(
 *     Ljava/lang/String;                                    # p0 log tag / userId
 *     Landroid/content/SharedPreferences;                   # p1
 *     Lcom/thestepupapp/stepup/repositories/TrackerType;    # p2 selected tracker
 *     JJ                                                     # p3,p5 now / start epoch millis
 *     Ljava/lang/String;                                    # p7 zone id string
 *     Ljava/lang/Integer;                                   # p8 <-- the fresh step count
 *     Ljava/time/ZoneId;                                    # p9
 *     Lcom/thestepupapp/stepup/network/beans/StepInformation; # p10 previous cached reading
 * )Lcom/thestepupapp/stepup/network/beans/Result;
 * ```
 *
 * Internally it takes `p8` (the just-measured cumulative step count for today), applies a
 * monotonic "only accept if larger than the previous reading" guard against `p10.getStep()`,
 * then constructs the `StepInformation` that is displayed, persisted, and uploaded to the
 * server. Because the SAME `p8` value feeds both the guard comparison and the stored object,
 * multiplying it here scales the whole pipeline consistently and exactly once — the leaderboard
 * value the server stores and the value the client later recomputes stay equal, so the app's
 * client-vs-server `InflatedStepsMonitor` never sees a divergence.
 *
 * Pinned by the unique, human-readable log literal below (verified to occur exactly once across
 * all dex) plus the public+static shape. The obfuscated method name `a` is intentionally not
 * referenced.
 */
internal object TrackerStepMergeFingerprint : Fingerprint(
    returnType = "Lcom/thestepupapp/stepup/network/beans/Result;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    strings = listOf(":trackerSteps is null which is not expected : nowEpochMillis:"),
)
