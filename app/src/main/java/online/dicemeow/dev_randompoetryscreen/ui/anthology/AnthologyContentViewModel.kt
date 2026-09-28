package online.dicemeow.dev_randompoetryscreen.ui.anthology

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import online.dicemeow.dev_randompoetryscreen.data.model.Anthology
import online.dicemeow.dev_randompoetryscreen.data.model.PoetryEntry
import online.dicemeow.dev_randompoetryscreen.data.repository.PoetryRepository

data class AnthologyContentUiState(
    val anthology: Anthology? = null,
    val entries: List<PoetryEntry> = emptyList()
)

class AnthologyContentViewModel(
    private val repository: PoetryRepository,
    private val anthologyId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnthologyContentUiState())
    val uiState: StateFlow<AnthologyContentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.data.collect { data ->
                val anthology = data.anthologies.find { it.id == anthologyId }
                _uiState.value = AnthologyContentUiState(
                    anthology = anthology,
                    entries = anthology?.entries ?: emptyList()
                )
            }
        }
    }

    fun deleteEntry(entryId: String) {
        repository.deleteEntry(anthologyId, entryId)
    }
}
