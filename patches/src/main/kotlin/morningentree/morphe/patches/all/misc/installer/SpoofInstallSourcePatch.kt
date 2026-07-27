package morningentree.morphe.patches.all.misc.installer

import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import java.util.logging.Logger

private const val PACKAGE_MANAGER = "Landroid/content/pm/PackageManager;"
private const val INSTALL_SOURCE_INFO = "Landroid/content/pm/InstallSourceInfo;"
private const val SESSION_INFO = "Landroid/content/pm/PackageInstaller\$SessionInfo;"

private fun Instruction.methodReferenceOrNull(): MethodReference? =
    (this as? ReferenceInstruction)?.reference as? MethodReference

private fun MethodReference.isInstallerGetter() =
    (definingClass == PACKAGE_MANAGER &&
        name == "getInstallerPackageName" &&
        parameterTypes.size == 1 &&
        parameterTypes[0].toString() == "Ljava/lang/String;" &&
        returnType == "Ljava/lang/String;") ||
        (definingClass == INSTALL_SOURCE_INFO &&
            name in setOf(
                "getInitiatingPackageName",
                "getInstallingPackageName",
                "getOriginatingPackageName",
                "getUpdateOwnerPackageName",
            ) &&
            parameterTypes.isEmpty() &&
            returnType == "Ljava/lang/String;") ||
        (definingClass == SESSION_INFO &&
            name in setOf(
                "getInstallerPackageName",
                "getInstallInitiatingPackageName",
                "getInstallOriginatingPackageName",
            ) &&
            parameterTypes.isEmpty() &&
            returnType == "Ljava/lang/String;")

private fun Instruction.isInstallSourceTarget() =
    opcode in setOf(Opcode.INVOKE_VIRTUAL, Opcode.INVOKE_VIRTUAL_RANGE) &&
        methodReferenceOrNull()?.isInstallerGetter() == true

private fun Method.hasInstallSourceTarget() =
    instructionsOrNull?.any { it.isInstallSourceTarget() } == true

/**
 * Universal "Spoof install source" patch — shows in Morphe for **any** app (no `compatibleWith`).
 *
 * Based on Rushi's patch (Layer 1 only). Walks every class and replaces the `move-result-object`
 * after each install-source getter call with a `const-string` returning the chosen installer,
 * covering:
 *   - `PackageManager.getInstallerPackageName(String)`                         [API 5+]
 *   - `InstallSourceInfo.get{Initiating,Installing,Originating,UpdateOwner}PackageName()` [API 30+]
 *   - `PackageInstaller.SessionInfo.getInstaller/Initiating/OriginatingPackageName()`    [API 21+/31+]
 *
 * This is the pure-DEX layer that fixes the common "must be installed from Google Play" gate. It does
 * not cover checks made from native code or via reflection (Rushi's binder-proxy Layer 2, which needs
 * a compiled extension, is intentionally omitted here to keep the patch self-contained).
 */
@Suppress("unused")
val spoofInstallSourcePatch = bytecodePatch(
    name = "Spoof install source",
    description = "Makes the app think it was installed from a specific store (default: Google " +
        "Play). Useful when an app blocks features or errors because it detects it was not " +
        "installed from the Play Store. Only affects what the app sees, not the real system record.",
    default = false,
) {
    val installerPackageName by stringOption(
        key = "installerPackageName",
        default = "com.android.vending",
        values = mapOf(
            "Google Play Store" to "com.android.vending",
            "Samsung Galaxy Store" to "com.sec.android.app.samsungapps",
            "Huawei AppGallery" to "com.huawei.appmarket",
            "Amazon Appstore" to "com.amazon.venezia",
            "F-Droid" to "org.fdroid.fdroid",
        ),
        title = "Store to impersonate",
        description = "Most apps only check for the Google Play Store, so the default is usually " +
            "correct. Pick from the list, or type any package name directly.",
        required = true,
    ) { it == null || it.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+\$")) }

    execute {
        val logger = Logger.getLogger(this::class.java.name)
        val targetInstaller = installerPackageName ?: "com.android.vending"
        var patchedCount = 0

        classDefForEach { classDef ->
            if (classDef.methods.none { it.hasInstallSourceTarget() }) return@classDefForEach

            mutableClassDefBy(classDef).methods.forEach { method ->
                if (!method.hasInstallSourceTarget()) return@forEach

                val instructionList = method.instructionsOrNull?.toList() ?: return@forEach

                instructionList.forEachIndexed { index, instruction ->
                    if (!instruction.isInstallSourceTarget()) return@forEachIndexed

                    val moveResult = instructionList.getOrNull(index + 1) as? OneRegisterInstruction
                        ?: return@forEachIndexed
                    if (moveResult.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed

                    method.replaceInstruction(
                        index + 1,
                        "const-string v${moveResult.registerA}, \"$targetInstaller\"",
                    )
                    patchedCount++
                }
            }
        }

        logger.info("Spoof install source: $patchedCount call site(s) patched -> \"$targetInstaller\".")
    }
}
