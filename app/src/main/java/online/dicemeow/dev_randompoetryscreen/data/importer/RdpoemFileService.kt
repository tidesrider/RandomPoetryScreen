package online.dicemeow.dev_randompoetryscreen.data.importer

import android.content.Context
import android.net.Uri

class RdpoemFileService(private val context: Context) {

    fun exportToUri(uri: Uri, jsonContent: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(jsonContent.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun importFromUri(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            }
        } catch (e: Exception) {
            null
        }
    }
}
