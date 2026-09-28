package online.dicemeow.dev_randompoetryscreen.ui.editor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import online.dicemeow.dev_randompoetryscreen.data.model.EntryType
import online.dicemeow.dev_randompoetryscreen.data.repository.PoetryRepository

data class EditUiState(
    val text: String = "",
    val findText: String = "",
    val replaceText: String = "",
    val matchCount: Int = 0,
    val isEditing: Boolean = false,
    val entryType: EntryType = EntryType.LINE,
    val message: String? = null
)

class EditViewModel(
    private val repository: PoetryRepository,
    private val anthologyId: String,
    private val entryId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    init {
        if (entryId != "new") {
            val entry = repository.getAnthology(anthologyId)?.entries?.find { it.id == entryId }
            if (entry != null) {
                _uiState.value = EditUiState(
                    text = entry.content,
                    isEditing = true,
                    entryType = entry.type
                )
            }
        }
    }

    fun updateText(text: String) {
        _uiState.value = _uiState.value.copy(text = text)
        updateMatchCount()
    }

    fun updateFindText(text: String) {
        _uiState.value = _uiState.value.copy(findText = text)
        updateMatchCount()
    }

    fun updateReplaceText(text: String) {
        _uiState.value = _uiState.value.copy(replaceText = text)
    }

    private fun updateMatchCount() {
        val find = _uiState.value.findText
        val text = _uiState.value.text
        if (find.isEmpty()) {
            _uiState.value = _uiState.value.copy(matchCount = 0)
            return
        }
        val count = text.split(find).size - 1
        _uiState.value = _uiState.value.copy(matchCount = count)
    }

    fun replaceAll(): Int {
        val find = _uiState.value.findText
        val replace = _uiState.value.replaceText
        if (find.isEmpty()) return 0
        val count = _uiState.value.matchCount
        val newText = _uiState.value.text.replace(find, replace)
        _uiState.value = _uiState.value.copy(text = newText)
        updateMatchCount()
        return count
    }

    fun saveAsLine() {
        val content = _uiState.value.text.trim()
        if (content.isEmpty()) {
            _uiState.value = _uiState.value.copy(message = "内容为空")
            return
        }
        if (_uiState.value.isEditing) {
            repository.updateEntry(anthologyId, entryId, content, EntryType.LINE)
        } else {
            repository.addEntry(anthologyId, content, EntryType.LINE)
        }
        _uiState.value = _uiState.value.copy(message = "已保存为行")
    }

    fun saveAsParagraph() {
        val content = _uiState.value.text.trim()
        if (content.isEmpty()) {
            _uiState.value = _uiState.value.copy(message = "内容为空")
            return
        }
        if (_uiState.value.isEditing) {
            repository.updateEntry(anthologyId, entryId, content, EntryType.PARAGRAPH)
        } else {
            repository.addEntry(anthologyId, content, EntryType.PARAGRAPH)
        }
        _uiState.value = _uiState.value.copy(message = "已保存为段落")
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
