package morningentree.morphe.patches.all.misc.time

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val PACKAGE_INFO = "Landroid/content/pm/PackageInfo;"

private val INSTALL_TIME_FIELDS = setOf("firstInstallTime", "lastUpdateTime")

@Suppress("unused")
val resetTrialPeriodPatch = bytecodePatch(
    name = "Reset trial period",
    description = "Makes apps see themselves as freshly installed (spoofs the install/update time to " +
        "the current time) so time-limited trials that count days since install never expire. Does " +
        "not affect trials validated on a server or stored in the app's own saved timestamp.",
    default = false,
) {
    execute {
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach

                // Collect matches first, then rewrite in descending index order: each rewrite
                // inserts a move-result-wide (shifting only higher indices), so processing
                // high -> low keeps the remaining target indices valid.
                val matches = instructions.mapIndexedNotNull { index, instruction ->
                    if (instruction.opcode != Opcode.IGET_WIDE) return@mapIndexedNotNull null
                    val field = (instruction as? ReferenceInstruction)?.reference as? FieldReference
                        ?: return@mapIndexedNotNull null
                    if (field.definingClass != PACKAGE_INFO ||
                        field.name !in INSTALL_TIME_FIELDS ||
                        field.type != "J"
                    ) return@mapIndexedNotNull null
                    val destRegister = (instruction as TwoRegisterInstruction).registerA
                    index to destRegister
                }

                for ((index, destRegister) in matches.asReversed()) {
                    method.replaceInstruction(
                        index,
                        "invoke-static {}, Ljava/lang/System;->currentTimeMillis()J",
                    )
                    method.addInstruction(index + 1, "move-result-wide v$destRegister")
                }
            }
        }
    }
}
