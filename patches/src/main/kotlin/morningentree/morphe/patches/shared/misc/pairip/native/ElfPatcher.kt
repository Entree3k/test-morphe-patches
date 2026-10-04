/**
 * Copyright 2026 Hoo-dles
 * https://github.com/hoo-dles/morphe-patches
 */

package morningentree.morphe.patches.shared.misc.pairip.native

object ElfPatcher {
    @Volatile
    private var initialized = false

    fun init() {
        synchronized(this) {
            if (!initialized) {
                CrossEnvNativeLoader.load("elf_jni_patcher")
                initialized = true
            }
        }
     }

    @JvmStatic
    fun patch(path: String, patches: Array<RelocationEntry>): Boolean {
        check(initialized) { "ElfPatcher must be initialized via init(...) before use." }
        return addRelocations(path, patches)
    }

    @JvmStatic
    private external fun addRelocations(path: String, patches: Array<RelocationEntry>): Boolean
}
