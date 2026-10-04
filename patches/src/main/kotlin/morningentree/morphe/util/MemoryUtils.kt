package morningentree.morphe.util

import kotlin.math.max

// In-memory Boyer-Moore search. Returns the index of the first occurrence of
// [pattern] in this array, or -1 if not found.
fun ByteArray.find(pattern: ByteArray): Int {
    val right = IntArray(256) { -1 }

    for ((i, element) in pattern.withIndex())
        right[element.toInt().and(0xFF)] = i

    var skip: Int
    for (i in 0..this.size - pattern.size) {
        skip = 0

        for (j in pattern.size - 1 downTo 0) {
            if (pattern[j] != this[i + j]) {
                skip = max(1, j - right[this[i + j].toInt().and(0xFF)])

                break
            }
        }

        if (skip == 0) return i
    }
    return -1
}
