package online.dicemeow.dev_randompoetryscreen.data.importer

import online.dicemeow.dev_randompoetryscreen.data.model.EntryType
import online.dicemeow.dev_randompoetryscreen.data.model.SplitMode

data class ImportResult(
    val entries: List<Pair<String, EntryType>>,
    val message: String? = null
)

object TextImporter {

    fun import(
        content: String,
        fileName: String,
        splitMode: SplitMode,
        fileType: FileType
    ): ImportResult {
        return when (fileType) {
            FileType.TXT -> importTxt(content, splitMode)
            FileType.MARKDOWN -> importMarkdown(content, splitMode)
            FileType.EPUB -> ImportResult(emptyList(), message = "EPUB文件需要二进制解析")
        }
    }

    private fun importTxt(content: String, splitMode: SplitMode): ImportResult {
        return when (splitMode) {
            SplitMode.BY_LINE -> {
                val lines = content.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
                ImportResult(lines.map { it to EntryType.LINE })
            }
            SplitMode.NO_SPLIT -> {
                ImportResult(listOf(content.trim() to EntryType.PARAGRAPH))
            }
            SplitMode.BY_TITLE -> {
                val lines = content.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
                ImportResult(lines.map { it to EntryType.LINE })
            }
        }
    }

    private fun importMarkdown(content: String, splitMode: SplitMode): ImportResult {
        return when (splitMode) {
            SplitMode.BY_LINE -> {
                val lines = content.split('\n')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                ImportResult(lines.map { it to EntryType.LINE })
            }
            SplitMode.NO_SPLIT -> {
                val cleaned = content.replace(Regex("^#{1,6}\\s+", RegexOption.MULTILINE), "").trim()
                ImportResult(listOf(cleaned to EntryType.PARAGRAPH))
            }
            SplitMode.BY_TITLE -> MarkdownParser.splitByTitle(content)
        }
    }
}

enum class FileType {
    TXT,
    MARKDOWN,
    EPUB;

    companion object {
        fun fromFileName(name: String): FileType? {
            val lower = name.lowercase()
            return when {
                lower.endsWith(".txt") -> TXT
                lower.endsWith(".md") || lower.endsWith(".markdown") -> MARKDOWN
                lower.endsWith(".epub") -> EPUB
                else -> null
            }
        }

        fun supportsTitleSplit(fileType: FileType): Boolean = fileType != TXT
    }
}
