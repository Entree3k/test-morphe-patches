package morningentree.morphe.patches.tide.premium

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.tide.shared.Constants
import morningentree.morphe.util.returnEarly
import java.util.logging.Logger

private const val USER = "Lio/moreless/tide/base/model/User;"
private const val VIP = "Lio/moreless/tide/base/model/User\$Vip;"

/**
 * Unlocks Tide (io.moreless.tide) VIP / membership on the client.
 *
 * ## Gate chain
 *
 * Tide gates premium content through the `User` membership getters, which fold
 * down to the `User$Vip` validity flag:
 *
 * ```
 * User.getMemberAvailable()          = vip != null && vip.getValid()
 * User$Vip.getValid()                = (isValid == true)
 * ```
 *
 * Because `vip` is null for an account that never subscribed, forcing only
 * `getValid()` would not help (the null-check short-circuits first). So we force
 * the `User`-level getters directly — plus the `User$Vip` validity getters for
 * any path that reads the Vip object — flipping the whole family:
 *   - "available / valid / lifetime"  → true
 *   - "expired"                       → false
 *
 * ## Note
 *
 * Premium content that the server only authorizes for real members (streamed or
 * token-gated audio) may still not download; this unlocks everything Tide
 * decides locally from the membership flags.
 */
@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Tide VIP membership on the client.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        // class type -> (methods forced true, methods forced false); all no-arg Z getters.
        val forceTrue = mapOf(
            USER to setOf(
                "getMemberAvailable",
                "getMemberAvailableAndInPeriod",
                "getMemberOrScenePassAvailable",
            ),
            VIP to setOf(
                "getValid",
                "isLifetimeMember",
            ),
        )
        val forceFalse = mapOf(
            USER to setOf(
                "getMemberExpired",
                "getMemberOrScenePassExpired",
            ),
            VIP to setOf(
                "getExpired",
            ),
        )

        var patched = 0
        classDefForEach { classDef ->
            val trues = forceTrue[classDef.type] ?: emptySet()
            val falses = forceFalse[classDef.type] ?: emptySet()
            if (trues.isEmpty() && falses.isEmpty()) return@classDefForEach

            for (method in mutableClassDefBy(classDef).methods) {
                if (method.returnType != "Z" || method.parameterTypes.isNotEmpty()) continue
                when (method.name) {
                    in trues -> {
                        method.returnEarly(true)
                        patched++
                    }
                    in falses -> {
                        method.returnEarly(false)
                        patched++
                    }
                }
            }
        }

        if (patched == 0) {
            throw PatchException("Tide: no membership getters found on User/User\$Vip to patch.")
        }
        logger.info("Tide: forced $patched membership getter(s).")
    }
}
