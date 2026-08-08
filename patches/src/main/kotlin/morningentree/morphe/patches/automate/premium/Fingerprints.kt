package morningentree.morphe.patches.automate.premium

import app.morphe.patcher.Fingerprint

/**
 * `AutomateService.f(...)` is the per-block "checkPremiumAllow" gate the engine runs before it
 * executes each block. It returns true when the flow is allowed to keep running and, on the free
 * tier, falls into the premium-purchase flow once a flow exceeds the free block budget (the `0x1e`
 * running-statement cap read out of the `runningStatementCount` bundle). Forcing it to true removes
 * the block limit — which is Automate's entire paywall — for every flow.
 *
 * The class name is stable and the three string literals are the log tag and bundle keys used right
 * here, so this anchors without touching any obfuscated name.
 */
internal object CheckPremiumAllowFingerprint : Fingerprint(
    definingClass = "Lcom/llamalab/automate/AutomateService;",
    returnType = "Z",
    strings = listOf("checkPremiumAllow", "runningStatementCount", "count"),
)
