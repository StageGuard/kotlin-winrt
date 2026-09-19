package io.github.composefluent.winrt.gallery.processor

/** Trimming removes whitespace only; preserve the original UTF-16 position of every remaining character. */
internal data class KotlinSourceFragment(val source: String, val originalOffsets: IntArray) {
    companion object {
        fun from(source: String, start: Int, end: Int, rendered: String): KotlinSourceFragment {
            var cursor = start
            val offsets = IntArray(rendered.length) { index ->
                val next = source.indexOf(rendered[index], cursor)
                require(next in cursor until end && source.substring(cursor, next).all(Char::isWhitespace)) {
                    "Snippet transformation must only remove whitespace at $cursor"
                }
                cursor = next + 1
                next
            }
            require(source.substring(cursor, end).all(Char::isWhitespace))
            return KotlinSourceFragment(rendered, offsets)
        }
    }
}

internal data class KotlinCodeOriginData(val path: String, val fragment: KotlinSourceFragment)
