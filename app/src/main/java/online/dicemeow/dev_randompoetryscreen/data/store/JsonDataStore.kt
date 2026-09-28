package online.dicemeow.dev_randompoetryscreen.data.store

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import online.dicemeow.dev_randompoetryscreen.data.model.Anthology
import online.dicemeow.dev_randompoetryscreen.data.model.AppData
import online.dicemeow.dev_randompoetryscreen.data.model.EntryType
import online.dicemeow.dev_randompoetryscreen.data.model.PoetryEntry
import online.dicemeow.dev_randompoetryscreen.R
import java.io.File
import java.util.UUID

class JsonDataStore(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val dataFile: File by lazy {
        File(context.filesDir, DATA_FILE_NAME)
    }

    @Synchronized
    fun load(): AppData {
        if (!dataFile.exists()) {
            return loadDefaultData()
        }
        return try {
            val json = dataFile.readText(Charsets.UTF_8)
            val raw = gson.fromJson(json, AppDataRaw::class.java)
            raw?.toAppData()?.validate() ?: AppData()
        } catch (e: Exception) {
            AppData()
        }
    }

    private fun loadDefaultData(): AppData {
        return try {
            val poetryData = loadDefaultAnthology(R.raw.default_poetry)
            val prophetData = loadDefaultAnthology(R.raw.prophet)
            
            val allAnthologies = mutableListOf<Anthology>()
            poetryData?.anthologies?.let { allAnthologies.addAll(it) }
            prophetData?.anthologies?.let { allAnthologies.addAll(it) }
            
            AppData(anthologies = allAnthologies)
        } catch (e: Exception) {
            AppData()
        }
    }
    
    private fun loadDefaultAnthology(resourceId: Int): AppData? {
        return try {
            val json = context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
            val raw = gson.fromJson(json, AppDataRaw::class.java)
            raw?.toAppData()
        } catch (e: Exception) {
            null
        }
    }

    private fun AppData.validate(): AppData {
        val validAnthologies = anthologies.filter { a ->
            a.id.isNotBlank() && a.name.isNotBlank()
        }.map { a ->
            val validEntries = a.entries.filter { e ->
                e.id.isNotBlank() && e.anthologyId == a.id && e.content.isNotBlank()
            }
            a.copy(entries = validEntries)
        }.filter { it.entries.isNotEmpty() || it.id.isNotBlank() }
        return AppData(anthologies = validAnthologies)
    }

    @Synchronized
    fun save(data: AppData) {
        val raw = AppDataRaw.fromAppData(data)
        val json = gson.toJson(raw)
        dataFile.writeText(json, Charsets.UTF_8)
    }

    fun exportToJson(data: AppData): String {
        val raw = AppDataRaw.fromAppData(data)
        return gson.toJson(raw)
    }

    fun importFromJson(json: String): AppData? {
        return try {
            val raw = gson.fromJson(json, AppDataRaw::class.java)
            raw?.toAppData()
        } catch (e: Exception) {
            null
        }
    }

    fun generateId(): String = UUID.randomUUID().toString()

    private data class EntryRaw(
        val id: String,
        val type: String,
        val content: String
    )

    private data class AnthologyRaw(
        val id: String,
        val name: String,
        val entries: List<EntryRaw>
    )

    private data class AppDataRaw(
        val version: Int = 1,
        val anthologies: List<AnthologyRaw> = emptyList()
    ) {
        fun toAppData(): AppData {
            val anthologies = this.anthologies.map { a ->
                Anthology(
                    id = a.id,
                    name = a.name,
                    entries = a.entries.map { e ->
                        PoetryEntry(
                            id = e.id,
                            anthologyId = a.id,
                            type = if (e.type == "PARAGRAPH") EntryType.PARAGRAPH else EntryType.LINE,
                            content = e.content
                        )
                    }
                )
            }
            return AppData(anthologies = anthologies)
        }

        companion object {
            fun fromAppData(data: AppData): AppDataRaw {
                return AppDataRaw(
                    version = 1,
                    anthologies = data.anthologies.map { a ->
                        AnthologyRaw(
                            id = a.id,
                            name = a.name,
                            entries = a.entries.map { e ->
                                EntryRaw(
                                    id = e.id,
                                    type = if (e.type == EntryType.PARAGRAPH) "PARAGRAPH" else "LINE",
                                    content = e.content
                                )
                            }
                        )
                    }
                )
            }
        }
    }

    companion object {
        private const val DATA_FILE_NAME = "poetry_data.json"
    }
}
