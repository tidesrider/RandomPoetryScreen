package online.dicemeow.dev_randompoetryscreen.data.importer

import online.dicemeow.dev_randompoetryscreen.data.model.EntryType
import online.dicemeow.dev_randompoetryscreen.data.model.SplitMode
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

object EpubParser {

    private val TAG_REGEX = Regex("<[^>]+>")
    private val H1_HTML = Regex("<h1[^>]*>(.*?)</h1>", RegexOption.DOT_MATCHES_ALL)
    private val H2_HTML = Regex("<h2[^>]*>(.*?)</h2>", RegexOption.DOT_MATCHES_ALL)

    fun parse(bytes: ByteArray, splitMode: SplitMode): ImportResult {
        val htmlFiles = extractHtmlFiles(bytes)
        if (htmlFiles.isEmpty()) return ImportResult(emptyList(), message = "无法解析EPUB文件")

        val orderedText = htmlFiles.joinToString("\n\n") { html ->
            extractTextFromHtml(html)
        }

        return when (splitMode) {
            SplitMode.BY_LINE -> {
                val lines = orderedText.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
                ImportResult(lines.map { it to EntryType.LINE })
            }
            SplitMode.NO_SPLIT -> {
                ImportResult(listOf(orderedText.trim() to EntryType.PARAGRAPH))
            }
            SplitMode.BY_TITLE -> splitByTitle(htmlFiles)
        }
    }

    private fun splitByTitle(htmlFiles: List<String>): ImportResult {
        data class Section(val title: String?, val content: StringBuilder)

        val sections = mutableListOf<Section>()
        var current = Section(null, StringBuilder())

        for (html in htmlFiles) {
            val h1Matches = H1_HTML.findAll(html).toList()
            val h2Matches = H2_HTML.findAll(html).toList()
            val hasH1 = h1Matches.isNotEmpty()
            val hasH2 = h2Matches.isNotEmpty()

            if (!hasH1 && !hasH2) {
                val text = extractTextFromHtml(html)
                if (text.isNotBlank()) current.content.append(text).append("\n")
                continue
            }

            val useH1 = hasH1 && (h1Matches.size > 1 || !hasH2)
            val headingRegex = if (useH1) H1_HTML else H2_HTML

            var lastEnd = 0
            for (match in headingRegex.findAll(html)) {
                if (current.content.isNotEmpty()) {
                    sections.add(current)
                }
                val title = stripTags(match.groupValues[1]).trim()
                val before = html.substring(lastEnd, match.range.first)
                val beforeText = extractTextFromHtml(before)
                if (beforeText.isNotBlank() && sections.isEmpty() && current.content.isEmpty()) {
                    current.content.append(beforeText)
                    sections.add(current)
                    current = Section(title, StringBuilder())
                } else {
                    current = Section(title, StringBuilder())
                }
                lastEnd = match.range.last + 1
            }
            val remainder = html.substring(lastEnd)
            val remText = extractTextFromHtml(remainder)
            if (remText.isNotBlank()) current.content.append(remText).append("\n")
        }
        if (current.content.isNotEmpty()) sections.add(current)

        if (sections.isEmpty()) {
            val fullText = htmlFiles.joinToString("\n") { extractTextFromHtml(it) }
            val lines = fullText.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
            return ImportResult(
                lines.map { it to EntryType.LINE },
                message = "未识别到标题格式，将按行拆分"
            )
        }

        val result = sections.map { s ->
            val titlePart = s.title?.let { "$it\n" } ?: ""
            val content = (titlePart + s.content.toString().trim()).trim()
            content to EntryType.PARAGRAPH
        }.filter { it.first.isNotEmpty() }

        return ImportResult(result)
    }

    private fun extractHtmlFiles(bytes: ByteArray): List<String> {
        val htmlFiles = mutableMapOf<String, String>()
        try {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val name = entry.name
                    if (name.endsWith(".html") || name.endsWith(".xhtml") || name.endsWith(".htm")) {
                        val content = zis.readBytes().toString(Charsets.UTF_8)
                        htmlFiles[name] = content
                    }
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            return emptyList()
        }

        val opfPath = findOpfPath(bytes) ?: return htmlFiles.values.toList()
        val opfContent = readEntry(bytes, opfPath) ?: return htmlFiles.values.toList()
        val manifest = parseOpfManifest(opfContent)

        val opfDir = opfPath.substringBeforeLast('/', "")
        return manifest.mapNotNull { item ->
            val path = if (opfDir.isNotEmpty()) "$opfDir/${item.href}" else item.href
            htmlFiles[path] ?: htmlFiles[item.href]
        }
    }

    private fun findOpfPath(bytes: ByteArray): String? {
        val containerXml = readEntry(bytes, "META-INF/container.xml") ?: return null
        return try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(containerXml.reader())
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "rootfile") {
                    return parser.getAttributeValue(null, "full-path")
                }
                event = parser.next()
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private data class ManifestItem(val id: String, val href: String, val mediaType: String)

    private fun parseOpfManifest(opfContent: String): List<ManifestItem> {
        val items = mutableListOf<ManifestItem>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(opfContent.reader())
            var event = parser.eventType
            var inSpine = false
            val spineOrder = mutableListOf<String>()
            val allItems = mutableListOf<ManifestItem>()

            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "item" -> {
                                val id = parser.getAttributeValue(null, "id") ?: ""
                                val href = parser.getAttributeValue(null, "href") ?: ""
                                val mediaType = parser.getAttributeValue(null, "media-type") ?: ""
                                allItems.add(ManifestItem(id, href, mediaType))
                            }
                            "spine" -> inSpine = true
                            "itemref" -> {
                                val idref = parser.getAttributeValue(null, "idref")
                                if (idref != null) spineOrder.add(idref)
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "spine") inSpine = false
                    }
                }
                event = parser.next()
            }

            if (spineOrder.isNotEmpty()) {
                val itemMap = allItems.associateBy { it.id }
                for (idref in spineOrder) {
                    itemMap[idref]?.let { item ->
                        if (item.mediaType.contains("html") || item.href.endsWith(".html") || item.href.endsWith(".xhtml")) {
                            items.add(item)
                        }
                    }
                }
            }
            if (items.isEmpty()) {
                items.addAll(allItems.filter {
                    it.mediaType.contains("html") || it.href.endsWith(".html") || it.href.endsWith(".xhtml")
                })
            }
        } catch (e: Exception) {
            // fallback
        }
        return items
    }

    private fun readEntry(bytes: ByteArray, entryName: String): String? {
        try {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (entry.name == entryName) {
                        return zis.readBytes().toString(Charsets.UTF_8)
                    }
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    private fun extractTextFromHtml(html: String): String {
        var text = html
        text = text.replace(Regex("(?i)<br\\s*/?>"), "\n")
        text = text.replace(Regex("(?i)</p>"), "\n")
        text = text.replace(Regex("(?i)</div>"), "\n")
        text = text.replace(Regex("(?i)<h[1-6][^>]*>"), "\n")
        text = text.replace(Regex("(?i)</h[1-6]>"), "\n")
        text = text.replace(Regex("(?i)<li[^>]*>"), "\n- ")
        text = stripTags(text)
        text = text.replace("&nbsp;", " ")
        text = text.replace("&amp;", "&")
        text = text.replace("&lt;", "<")
        text = text.replace("&gt;", ">")
        text = text.replace("&quot;", "\"")
        text = text.replace("&#39;", "'")
        text = text.replace(Regex("\n{3,}"), "\n\n")
        return text.trim()
    }

    private fun stripTags(html: String): String {
        return TAG_REGEX.replace(html, "")
    }
}
