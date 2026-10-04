package morningentree.morphe.patches.all.misc.network

import app.morphe.patcher.patch.resourcePatch
import morningentree.morphe.util.asElementSequence
import morningentree.morphe.util.get

// Based on adobo's patch

@Suppress("unused")
val removeInternetPermissionPatch = resourcePatch(
    name = "Remove internet permission",
    description = "Removes the INTERNET permission so the app cannot access the network at all",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document["manifest"]

            manifest.getElementsByTagName("uses-permission")
                .asElementSequence()
                .filter { it.getAttribute("android:name") == "android.permission.INTERNET" }
                .toList()
                .forEach { it.parentNode.removeChild(it) }
        }
    }
}
