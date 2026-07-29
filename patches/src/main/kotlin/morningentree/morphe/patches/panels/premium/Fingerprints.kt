package morningentree.morphe.patches.panels.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

// Matches the app-wide "full version" gate: reads getBoolean("fullVersion", false) from the
// SharedPreferences wrapper and returns it. Forcing this to true unlocks every premium
// feature (called from 13 sites across the app). "fullVersion" also appears in a putBoolean
// writer, but that method returns V and takes a parameter, so returnType=Z + no params +
// getBoolean uniquely resolves this reader.
object FullVersionCheckFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(
        string("fullVersion"),
        methodCall(name = "getBoolean"),
    )
)
