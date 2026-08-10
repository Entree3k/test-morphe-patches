package morningentree.morphe.patches.all.misc.packagename

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import morningentree.morphe.util.asElementSequence
import morningentree.morphe.util.get
import morningentree.morphe.util.set
import org.w3c.dom.Document
import org.w3c.dom.NodeList

private const val MANIFEST_NODE = "manifest"
private const val ANDROID_NAME_ATTR = "android:name"

private const val DEFAULT_SUFFIX = ".entree"

private fun ResourcePatchContext.androidManifest(
    block: Document.() -> Unit,
): Document = document("AndroidManifest.xml").use { document ->
    document.apply(block)
}

private val fullPackagePattern = """^[a-z]\w*(\.[a-z]\w*)+$""".toRegex()
private val suffixPattern = """^(\.[a-z]\w*)+$""".toRegex()

@Suppress("unused")
val changePackageNamePatch = resourcePatch(
    name = "Change package name",
    description = "Renames the app (default: append \"$DEFAULT_SUFFIX\") so it installs as a " +
        "clone next to the original. The name is configurable. Changing a package name can " +
        "lead to unexpected issues.",
    default = false,
) {

    val packageNameOption = stringOption(
        key = "packageName",
        default = "Default",
        values = mapOf("Default" to "Default"),
        title = "Package name",
        description = "Leave as \"Default\" to append \"$DEFAULT_SUFFIX\" to the original package. " +
            "Or type a custom value: a suffix starting with \".\" (appended to the original " +
            "package) or a full replacement package name.",
        required = true,
    ) { value ->
        value == "Default" ||
            (value != null && (value.matches(suffixPattern) || value.matches(fullPackagePattern)))
    }

    val updatePermissions by booleanOption(
        key = "updatePermissions",
        default = true,
        title = "Update permissions",
        description = "Update compatibility receiver permissions. Enabling this can fix " +
            "installation errors, but can also break features in certain apps.",
    )

    val updateOtherPermissions by booleanOption(
        key = "updateOtherPermissions",
        default = true,
        title = "Update other permissions",
        description = "Update other permissions declared by the app. Enabling this can fix " +
            "installation errors, but can also break features in certain apps.",
    )

    val updateProviders by booleanOption(
        key = "updateProviders",
        default = true,
        title = "Update content providers",
        description = "Update content provider URI authorities. Enabling this can fix " +
            "installation errors, but can also break features in certain apps.",
    )

    finalize {
        androidManifest {
            val manifest = this[MANIFEST_NODE]
            val packageName = manifest["package"]

            val configured = packageNameOption.value!!
            val newPackageName = when {
                // "Default" sentinel -> append the default suffix.
                configured == "Default" -> "$packageName$DEFAULT_SUFFIX"
                // Custom suffix (".mod", ".patched", …) -> append to original.
                configured.startsWith(".") -> "$packageName$configured"
                // Full replacement package name.
                else -> configured
            }

            if (newPackageName == packageName) return@androidManifest

            manifest["package"] = newPackageName

            if (updatePermissions == true || updateOtherPermissions == true) {
                val receiverNotExported = "DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"

                setOf("permission", "uses-permission")
                    .asSequence()
                    .map(::getElementsByTagName)
                    .flatMap(NodeList::asElementSequence)
                    .filter { element ->
                        val nameAttrValue = element[ANDROID_NAME_ATTR]
                        val isReceiverPermission = updatePermissions == true &&
                            nameAttrValue == "$packageName.$receiverNotExported"
                        val isCustomPermission = updateOtherPermissions == true &&
                            nameAttrValue.startsWith(packageName)

                        isReceiverPermission || isCustomPermission
                    }
                    .forEach { element ->
                        element[ANDROID_NAME_ATTR] = element[ANDROID_NAME_ATTR].replace(
                            oldValue = packageName,
                            newValue = newPackageName,
                        )
                    }
            }

            if (updateProviders == true) {
                val authoritiesAttr = "android:authorities"

                manifest
                    .getElementsByTagName("provider")
                    .asElementSequence()
                    .filter { it[authoritiesAttr].startsWith(packageName) }
                    .forEach { provider ->
                        provider[authoritiesAttr] = provider[authoritiesAttr].replace(
                            oldValue = packageName,
                            newValue = newPackageName,
                        )
                    }
            }
        }
    }
}
