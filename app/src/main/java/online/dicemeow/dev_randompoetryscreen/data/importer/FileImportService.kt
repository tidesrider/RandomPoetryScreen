package online.dicemeow.dev_randompoetryscreen.data.importer

import android.content.Context
import android.net.Uri
import online.dicemeow.dev_randompoetryscreen.data.model.SplitMode

class FileImportService(private val context: Context) {

    fun importFile(uri: Uri, fileName: String, splitMode: SplitMode): ImportResult {
        val fileType = FileType.fromFileName(fileName)
            ?: return ImportResult(emptyList(), message = "不支持的文件格式: $fileName")

        return try {
            when (fileType) {
                FileType.EPUB -> {
                    val bytes = readBytes(uri)
                    EpubParser.parse(bytes, splitMode)
                }
                FileType.MARKDOWN, FileType.TXT -> {
                    val text = readText(uri)
                    TextImporter.import(text, fileName, splitMode, fileType)
                }
            }
        } catch (e: Exception) {
            ImportResult(emptyList(), message = "导入失败: ${e.message}")
        }
    }

    fun readTextForExport(uri: Uri): String? {
        return try {
            readText(uri)
        } catch (e: Exception) {
            null
        }
    }

    fun getFileName(uri: Uri): String? {
        var result: String? = null
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    result = cursor.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        return result ?: uri.lastPathSegment
    }

    private fun readText(uri: Uri): String {
        context.contentResolver.openInputStream(uri)?.use { input ->
            return input.bufferedReader(Charsets.UTF_8).readText()
        }
        return ""
    }

    private fun readBytes(uri: Uri): ByteArray {
        context.contentResolver.openInputStream(uri)?.use { input ->
            return input.readBytes()
        }
        return ByteArray(0)
    }
}
