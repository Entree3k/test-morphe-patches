/**
 * Copyright 2026 Hoo-dles
 * https://github.com/hoo-dles/morphe-patches
 */

package morningentree.morphe.patches.shared.misc.pairip.native

import org.scijava.nativelib.NativeLoader
import java.io.File
import java.io.FileOutputStream

const val NATIVE_DIR_PREFIX = "pairip/native"

object CrossEnvNativeLoader {
    fun load(libName: String) {
        val isAndroid = System.getProperty("java.vendor")?.contains("Android", ignoreCase = true) == true
        if (isAndroid) {
            // On Android java.io.tmpdir is the app's writable cache dir.
            val workDir = File(System.getProperty("java.io.tmpdir") ?: ".")
            AndroidLoader.load(libName, workDir)
        } else {
            DesktopLoader.load(libName)
        }
    }
}

private object DesktopLoader {
    fun load(libName: String) {
        NativeLoader.loadLibrary(libName, NATIVE_DIR_PREFIX)
    }
}

private object AndroidLoader {
    fun load(libName: String, codeCache: File) {
        val targetDir = File(codeCache, "native_libs").apply { mkdirs() }
        val targetSoFile = File(targetDir, "lib$libName.so")

        targetDir.setWritable(true, true)
        targetSoFile.setWritable(true, true)

        val input = bundledResourceStream("$NATIVE_DIR_PREFIX/android_arm64/lib$libName.so")
            ?: throw RuntimeException("Could not extract bundled library ($libName)")

        input.use { ins ->
            FileOutputStream(targetSoFile).use { output ->
                ins.copyTo(output)
            }
        }

        // Android 15+: Remove write permission before loading for W^X
        targetSoFile.setWritable(false, false)
        targetSoFile.setReadable(true, false)
        targetSoFile.setExecutable(true, false)

        targetDir.setWritable(false, false)
        targetDir.setExecutable(true, false)

        System.load(targetSoFile.absolutePath)
    }
}
