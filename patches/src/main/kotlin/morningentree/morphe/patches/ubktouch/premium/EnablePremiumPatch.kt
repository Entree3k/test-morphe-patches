package morningentree.morphe.patches.ubktouch.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import morningentree.morphe.patches.ubktouch.shared.Constants
import morningentree.morphe.util.getReference

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks UbikiTouch premium (all features).",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val unlocked = MainPrefUnlockedFingerprint.method

        // Primary: force the underlying license check `fp1.e0()Z` (the no-arg boolean static that
        // `unlocked()` delegates to, and that ~49 sites call directly) to always return true.
        // Resolve it via its reference inside unlocked() so the obfuscated class name is never
        // hardcoded. Boolean.valueOf(Z) is the other static here, but it takes a parameter.
        val licenseRef = unlocked.instructions
            .mapNotNull { it.getReference<MethodReference>() }
            .firstOrNull { it.returnType == "Z" && it.parameterTypes.isEmpty() }

        if (licenseRef != null) {
            classDefForEach { classDef ->
                if (classDef.type != licenseRef.definingClass) return@classDefForEach
                mutableClassDefBy(classDef).methods
                    .firstOrNull {
                        it.name == licenseRef.name &&
                            it.parameterTypes.isEmpty() &&
                            it.returnType == "Z"
                    }
                    ?.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // Belt-and-suspenders: the un-obfuscated wrapper returns a boxed Boolean; force it to TRUE
        // directly too, so the gate holds even if the reference above ever fails to resolve.
        unlocked.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """,
        )
    }
}
