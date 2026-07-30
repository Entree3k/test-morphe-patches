package morningentree.morphe.patches.nova.prime

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import morningentree.morphe.patches.nova.shared.Constants
import morningentree.morphe.util.getReference

@Suppress("unused")
val enablePrimePatch = bytecodePatch(
    name = "Enable Prime",
    description = "Unlocks Nova Launcher Prime and everything behind the Prime paywall, locally " +
        "(no Google Play licensing / network check needed).",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        SetPrimeFromPreferencesFingerprint.method.apply {
            val insns = instructions.toList()

            // The license level is read as `getInt("1", 0)` (the first getInt in this method). Nova's
            // own code then sets isPrime = (level == 0x200), so overwriting that read's result with
            // 0x200 flips both the "unlocked" and "isPrime" flags on every launch.
            val getIntIndex = insns.indexOfFirst {
                it.opcode == Opcode.INVOKE_INTERFACE &&
                    it.getReference<MethodReference>()?.name == "getInt"
            }
            if (getIntIndex < 0) {
                throw PatchException("Could not find the license-level getInt read in Nova's license method.")
            }

            val moveResultIndex = (getIntIndex + 1 until insns.size).first {
                insns[it].opcode == Opcode.MOVE_RESULT
            }
            val register = (insns[moveResultIndex] as OneRegisterInstruction).registerA

            addInstruction(moveResultIndex + 1, "const/16 v$register, 0x200")
        }

        // Forcing the startup state in a() is not enough on its own: Nova's async licensing
        // subsystem (created solely in Lvu/y0;->c(Context)) later flips the Prime gate Lny/h2;->h
        // back to false — the key-app signature check in Lvu/x0; fails for a re-signed/absent Prime
        // app, the LVL callbacks revoke, and the aa/p watchdog resets it 10s after any grant.
        // Neuter that single entry point: set the in-memory gate (h = unlocked, c = isPrime) and the
        // master "licensed" flag Lvu/y0;->b true, then return before any checker or watchdog spawns.
        // With no checker ever created, nothing can revoke Prime. (Method is `.locals 6`, static with
        // one param — v0/v1 are free; we return before the original body.)
        LicenseCheckEntryFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lny/a3;->a:Lny/h2;
                const/4 v1, 0x1
                iput-boolean v1, v0, Lny/h2;->h:Z
                iput-boolean v1, v0, Lny/h2;->c:Z
                sput-boolean v1, Lvu/y0;->b:Z
                return-void
            """,
        )
    }
}
