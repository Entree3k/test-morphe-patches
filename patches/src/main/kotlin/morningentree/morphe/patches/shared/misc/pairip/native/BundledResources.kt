package morningentree.morphe.patches.shared.misc.pairip.native

import java.io.InputStream

private object PairipResourceAnchor

/**
 * Reads a resource bundled on the patches module classpath
 * (i.e. a file under `patches/src/main/resources/<path>`). Replaces the patcher's
 * `app.morphe.util.inputStreamFromBundledResource`, which this morphe version does not expose.
 */
internal fun bundledResourceStream(path: String): InputStream? =
    PairipResourceAnchor::class.java.getResourceAsStream("/$path")
