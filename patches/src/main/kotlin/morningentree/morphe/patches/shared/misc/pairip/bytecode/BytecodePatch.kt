/**
 * Copyright 2026 Hoo-dles
 * https://github.com/hoo-dles/morphe-patches
 */

package morningentree.morphe.patches.shared.misc.pairip.bytecode

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

internal fun getBytecodePatch(appName: String) = bytecodePatch {
    execute {
        // VMRunner.<clinit> (loads libpairipcore) -> no-op; VMRunner.invoke(...) -> return null.
        // morphe's util.returnEarly is boolean-only, so insert the instructions directly.
        VMRunnerStaticCtorFingerprint.method.addInstruction(0, "return-void")
        VMRunnerInvokeFingerprint.method.addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")

        val applicationName = "Lcom/pairip/application/Application;"
        val applicationClass = mutableClassDefBy(applicationName)
        applicationClass.virtualMethods.removeIf { it.name == "attachBaseContext" }

        val staticCtorImpl = MutableMethodImplementation(1)
        val staticCtor = ImmutableMethod(
            applicationName,
            "<clinit>",
            emptyList<ImmutableMethodParameter>(),
            "V",
            AccessFlags.CONSTRUCTOR.value or AccessFlags.STATIC.value,
            null,
            null,
            staticCtorImpl
        ).toMutable()

        staticCtor.addInstructions(0, """
            invoke-static { }, Lcom/pairip/StartupLauncher;->launch()V
            return-void
        """.trimIndent())

        applicationClass.directMethods.add(staticCtor)

        StartupLaunchFingerprint.apply {
            val invokeIndex = instructionMatches.first().index
            method.replaceInstruction(invokeIndex, """
                invoke-static { }, Lmorningentree/morphe/extension/$appName/pairip/PairipHook;->inject()V
            """.trimIndent()
            )
        }
    }
}