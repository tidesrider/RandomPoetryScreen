package online.dicemeow.dev_randompoetryscreen.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import online.dicemeow.dev_randompoetryscreen.data.model.Anthology
import online.dicemeow.dev_randompoetryscreen.data.model.AppData
import online.dicemeow.dev_randompoetryscreen.data.model.EntryType
import online.dicemeow.dev_randompoetryscreen.data.model.PoetryEntry
import online.dicemeow.dev_randompoetryscreen.data.store.JsonDataStore

class PoetryRepository private constructor(
    private val store: JsonDataStore
) {
    private val _data = MutableStateFlow(AppData())
    val data: StateFlow<AppData> = _data.asStateFlow()

    init {
        _data.value = store.load()
    }

    private fun persist() {
        store.save(_data.value)
    }

    fun getAnthology(id: String): Anthology? = _data.value.anthologies.find { it.id == id }

    fun createAnthology(name: String): Anthology {
        val anthology = Anthology(id = store.generateId(), name = name)
        _data.value = _data.value.copy(
            anthologies = _data.value.anthologies + anthology
        )
        persist()
        return anthology
    }

    fun renameAnthology(id: String, newName: String) {
        _data.value = _data.value.copy(
            anthologies = _data.value.anthologies.map {
                if (it.id == id) it.copy(name = newName) else it
            }
        )
        persist()
    }

    fun deleteAnthology(id: String) {
        _data.value = _data.value.copy(
            anthologies = _data.value.anthologies.filterNot { it.id == id }
        )
        persist()
    }

    fun mergeAnthologies(ids: List<String>, newName: String): Anthology {
        val toMerge = _data.value.anthologies.filter { it.id in ids }
        val mergedId = store.generateId()
        val merged = Anthology(
            id = mergedId,
            name = newName,
            entries = toMerge.flatMap { it.entries }
                .map { it.copy(id = store.generateId(), anthologyId = mergedId) }
        )
        _data.value = _data.value.copy(
            anthologies = _data.value.anthologies.filterNot { it.id in ids } + merged
        )
        persist()
        return merged
    }

    fun addEntry(anthologyId: String, content: String, type: EntryType): PoetryEntry {
        val entry = PoetryEntry(
            id = store.generateId(),
            anthologyId = anthologyId,
            type = type,
            content = content
        )
        _data.value = _data.value.copy(
            anthologies = _data.value.anthologies.map { a ->
                if (a.id == anthologyId) a.copy(entries = a.entries + entry) else a
            }
        )
        persist()
        return entry
    }

    fun addEntries(anthologyId: String, entries: List<Pair<String, EntryType>>) {
        val newEntries = entries.map { (content, type) ->
            PoetryEntry(
                id = store.generateId(),
                anthologyId = anthologyId,
                type = type,
                content = content
            )
        }
        _data.value = _data.value.copy(
            anthologies = _data.value.anthologies.map { a ->
                if (a.id == anthologyId) a.copy(entries = a.entries + newEntries) else a
            }
        )
        persist()
    }

    fun updateEntry(anthologyId: String, entryId: String, content: String, type: EntryType) {
        _data.value = _data.value.copy(
            anthologies = _data.value.anthologies.map { a ->
                if (a.id == anthologyId) {
                    a.copy(entries = a.entries.map { e ->
                        if (e.id == entryId) e.copy(content = content, type = type) else e
                    })
                } else a
            }
        )
        persist()
    }

    fun deleteEntry(anthologyId: String, entryId: String) {
        _data.value = _data.value.copy(
            anthologies = _data.value.anthologies.map { a ->
                if (a.id == anthologyId) {
                    a.copy(entries = a.entries.filterNot { it.id == entryId })
                } else a
            }
        )
        persist()
    }

    fun getEntries(anthologyIds: Set<String>): List<PoetryEntry> {
        return _data.value.anthologies
            .filter { it.id in anthologyIds }
            .flatMap { it.entries }
    }

    fun getAllEntries(): List<PoetryEntry> = _data.value.anthologies.flatMap { it.entries }

    fun exportData(): String = store.exportToJson(_data.value)

    fun importData(json: String, merge: Boolean): Boolean {
        val imported = store.importFromJson(json) ?: return false
        _data.value = if (merge) {
            val existingIds = _data.value.anthologies.map { it.id }.toMutableSet()
            val newAnthologies = imported.anthologies.map { a ->
                var newId = a.id
                if (newId in existingIds) {
                    newId = store.generateId()
                    a.copy(id = newId, entries = a.entries.map { it.copy(anthologyId = newId, id = store.generateId()) })
                } else {
                    existingIds.add(newId)
                    a
                }
            }
            AppData(anthologies = _data.value.anthologies + newAnthologies)
        } else {
            imported
        }
        persist()
        return true
    }

    companion object {
        @Volatile
        private var instance: PoetryRepository? = null

        fun getInstance(context: Context): PoetryRepository {
            return instance ?: synchronized(this) {
                instance ?: PoetryRepository(
                    JsonDataStore(context.applicationContext)
                ).also { instance = it }
            }
        }
    }
}
