package morningentree.morphe.patches.dhgate.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY_DHGATE = Compatibility(
        name = "DHgate",
        packageName = "com.dhgate.buyermob",
        appIconColor = 0x00A94F,
        targets = listOf(AppTarget("7.1.1")),
    )
}
