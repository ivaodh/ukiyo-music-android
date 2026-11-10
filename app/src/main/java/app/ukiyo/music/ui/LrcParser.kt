package app.ukiyo.music.ui

data class LrcLine(
    val timeMs: Long,
    val text: String
)

private val timestampRegex = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{2,3}))?]""")

fun parseLrc(text: String?): List<LrcLine> {
    if (text.isNullOrBlank()) return emptyList()
    val lines = mutableListOf<LrcLine>()
    text.lineSequence().forEach { raw ->
        val matches = timestampRegex.findAll(raw).toList()
        if (matches.isEmpty()) return@forEach
        val lyric = raw.replace(timestampRegex, "").trim()
        if (lyric.isBlank()) return@forEach
        matches.forEach { match ->
            val minutes = match.groupValues[1].toLongOrNull() ?: 0
            val seconds = match.groupValues[2].toLongOrNull() ?: 0
            val fraction = match.groupValues[3]
            val millis = when (fraction.length) {
                1 -> fraction.toLongOrNull()?.times(100) ?: 0
                2 -> fraction.toLongOrNull()?.times(10) ?: 0
                3 -> fraction.toLongOrNull() ?: 0
                else -> 0
            }
            lines.add(LrcLine((minutes * 60 + seconds) * 1000 + millis, lyric))
        }
    }
    return lines.sortedBy { it.timeMs }
}

fun currentLrcIndex(lines: List<LrcLine>, positionMs: Long): Int {
    if (lines.isEmpty()) return -1
    var index = -1
    for (i in lines.indices) {
        if (lines[i].timeMs <= positionMs) index = i else break
    }
    return index
}
