package morningentree.morphe.patches.vocabulary.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Vocabulary",
        packageName = "com.hrd.vocabulary",
        // Cosmetic only (patcher UI accent) — app's colorPrimary.
        appIconColor = 0xC7604F,
        targets = listOf(
            AppTarget("5.5.1"),
            AppTarget("5.4.0"),
        ),
    )
}
