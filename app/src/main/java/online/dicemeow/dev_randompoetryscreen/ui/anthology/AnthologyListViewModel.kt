package online.dicemeow.dev_randompoetryscreen.ui.anthology

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import online.dicemeow.dev_randompoetryscreen.data.importer.FileImportService
import online.dicemeow.dev_randompoetryscreen.data.importer.FileType
import online.dicemeow.dev_randompoetryscreen.data.importer.ImportResult
import online.dicemeow.dev_randompoetryscreen.data.model.Anthology
import online.dicemeow.dev_randompoetryscreen.data.model.SplitMode
import online.dicemeow.dev_randompoetryscreen.data.repository.PoetryRepository

data class AnthologyListUiState(
    val anthologies: List<Anthology> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val message: String? = null
)

class AnthologyListViewModel(
    private val repository: PoetryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnthologyListUiState())
    val uiState: StateFlow<AnthologyListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.data.collect { data ->
                _uiState.value = _uiState.value.copy(anthologies = data.anthologies)
            }
        }
    }

    fun createAnthology(name: String): Anthology {
        return repository.createAnthology(name)
    }

    fun renameAnthology(id: String, newName: String) {
        repository.renameAnthology(id, newName)
    }

    fun deleteAnthology(id: String) {
        repository.deleteAnthology(id)
        _uiState.value = _uiState.value.copy(
            selectedIds = _uiState.value.selectedIds - id
        )
    }

    fun toggleSelection(id: String) {
        val current = _uiState.value.selectedIds
        _uiState.value = _uiState.value.copy(
            selectedIds = if (id in current) current - id else current + id
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedIds = emptySet())
    }

    fun mergeSelected(newName: String): Anthology? {
        val ids = _uiState.value.selectedIds.toList()
        if (ids.size < 2) return null
        val merged = repository.mergeAnthologies(ids, newName)
        _uiState.value = _uiState.value.copy(selectedIds = emptySet())
        return merged
    }

    fun exportAll(): String = repository.exportData()

    fun importRdpoem(json: String): Boolean = repository.importData(json, merge = true)

    fun processImport(
        fileImportService: FileImportService,
        files: List<Pair<android.net.Uri, String>>,
        splitMode: SplitMode,
        targetAnthologyId: String
    ): ImportResult {
        var totalEntries = 0
        var lastMessage: String? = null
        for ((uri, fileName) in files) {
            val result = fileImportService.importFile(uri, fileName, splitMode)
            if (result.entries.isNotEmpty()) {
                repository.addEntries(targetAnthologyId, result.entries)
                totalEntries += result.entries.size
            }
            if (result.message != null) lastMessage = result.message
        }
        return ImportResult(
            entries = emptyList(),
            message = lastMessage ?: "导入完成: $totalEntries 条"
        )
    }

    fun getAvailableSplitModes(fileTypes: Set<FileType>): List<SplitMode> {
        return if (fileTypes.all { it == FileType.TXT }) {
            listOf(SplitMode.BY_LINE, SplitMode.NO_SPLIT)
        } else if (fileTypes.all { it != FileType.TXT }) {
            listOf(SplitMode.BY_LINE, SplitMode.BY_TITLE, SplitMode.NO_SPLIT)
        } else {
            listOf(SplitMode.BY_LINE, SplitMode.NO_SPLIT)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
