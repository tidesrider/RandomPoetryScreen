package online.dicemeow.dev_randompoetryscreen.data.importer

import online.dicemeow.dev_randompoetryscreen.data.model.EntryType

object MarkdownParser {

    private val H1_REGEX = Regex("^#\\s+(.+)$", RegexOption.MULTILINE)
    private val H2_REGEX = Regex("^##\\s+(.+)$", RegexOption.MULTILINE)

    fun splitByTitle(content: String): ImportResult {
        val h1Matches = H1_REGEX.findAll(content).toList()
        val h2Matches = H2_REGEX.findAll(content).toList()

        val hasTitles = h1Matches.isNotEmpty() || h2Matches.isNotEmpty()
        if (!hasTitles) {
            val lines = content.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
            return ImportResult(
                lines.map { it to EntryType.LINE },
                message = "未识别到标题格式，将按行拆分"
            )
        }

        val useH1 = h1Matches.size > 1 || (h1Matches.isNotEmpty() && h2Matches.isEmpty())
        val useH2 = !useH1 && h2Matches.isNotEmpty()

        val segments = mutableListOf<Pair<String, EntryType>>()

        if (useH1) {
            val boundaries = h1Matches.map { it.range.first }
            segments.addAll(splitByBoundaries(content, boundaries))
        } else if (useH2) {
            val boundaries = h2Matches.map { it.range.first }
            segments.addAll(splitByBoundaries(content, boundaries))
        } else {
            val lines = content.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
            return ImportResult(
                lines.map { it to EntryType.LINE },
                message = "未识别到标题格式，将按行拆分"
            )
        }

        if (segments.isEmpty()) {
            val lines = content.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
            return ImportResult(
                lines.map { it to EntryType.LINE },
                message = "未识别到标题格式，将按行拆分"
            )
        }

        return ImportResult(segments)
    }

    private fun splitByBoundaries(content: String, boundaries: List<Int>): List<Pair<String, EntryType>> {
        val result = mutableListOf<Pair<String, EntryType>>()
        val allBounds = listOf(0) + boundaries + listOf(content.length)

        for (i in 1 until allBounds.size) {
            val start = allBounds[i - 1]
            val end = allBounds[i]
            val segment = content.substring(start, end).trim()
            if (segment.isNotEmpty()) {
                result.add(segment to EntryType.PARAGRAPH)
            }
        }

        if (result.size > 1 && result.first().first.lines().firstOrNull()?.matches(Regex("^#{1,2}\\s+.+")) == false) {
            val first = result.first()
            if (!first.first.startsWith("#")) {
                result[0] = first.first to EntryType.LINE
            }
        }

        return result
    }
}
