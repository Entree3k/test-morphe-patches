package morningentree.morphe.patches.recorder.pairip

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import java.util.logging.Logger

private const val STARTUP_LAUNCHER = "Lcom/pairip/StartupLauncher;"

/**
 * Neutralizes ONLY Pairip's startup VM program (`StartupLauncher.launch()`), which runs the native
 * anti-tamper/integrity program before any app code (invoked from the app's ComponentFactory
 * `<clinit>`). On a re-signed build that program self-destructs inside libpairipcore.so (Scudo
 * "corrupted chunk header" abort at process uptime ~1s).
 *
 * Unlike the universal Disable-Pairip "Gut Pairip VM" option, this deliberately does NOT touch
 * `VMRunner.invoke()` or `VMRunner.<clinit>` — Google Recorder routes real functionality (broadcast
 * receivers, network change notifier, and other core classes) through the VM, so those must keep
 * working. The startup program's result is discarded, so skipping it removes the integrity gate
 * without disturbing the functional VM programs.
 *
 * NOTE: this only helps a standalone re-signed (or virtualization-clone) install. If the native VM
 * runtime also self-verifies on every `executeVM` call, the crash will simply move to the first real
 * `invoke()` and the target remains mount-over-original only.
 */
@Suppress("unused")
val disablePairipStartupPatch = bytecodePatch(
    name = "Disable Pairip startup integrity program",
    description = "No-ops StartupLauncher.launch() so Pairip's native startup anti-tamper program " +
        "never runs, while leaving the functional VM (VMRunner.invoke) intact.",
    default = false,
) {
    execute {
        val logger = Logger.getLogger(this::class.java.name)

        var neutralized = 0
        classDefForEach { classDef ->
            if (classDef.type != STARTUP_LAUNCHER) return@classDefForEach
            for (method in mutableClassDefBy(classDef).methods) {
                if (method.name == "launch" && method.returnType == "V") {
                    method.addInstruction(0, "return-void")
                    neutralized++
                }
            }
        }

        if (neutralized == 0) {
            logger.warning("Pairip startup: StartupLauncher.launch() not found; no changes applied.")
        } else {
            logger.info("Pairip startup: StartupLauncher.launch() neutralized.")
        }
    }
}
