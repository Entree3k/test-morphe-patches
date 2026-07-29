package morningentree.morphe.patches.panels.misc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

// MainActivity.onCreate reads the app's own signature, compares it to a stored value, and
// calls Process.killProcess() on mismatch. Re-signing after patching trips this, so the app
// dies on launch. Anchored on the unobfuscated MainActivity/onCreate names plus the two
// framework calls the check is built from.
object MainActivitySignatureCheckFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lcom/fossor/panels/MainActivity;" && method.name == "onCreate"
    },
    filters = listOf(
        methodCall(definingClass = "Landroid/content/pm/Signature;", name = "toCharsString"),
        methodCall(definingClass = "Ljava/lang/String;", name = "equals"),
    ),
)
