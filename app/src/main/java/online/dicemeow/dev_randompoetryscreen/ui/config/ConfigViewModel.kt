package online.dicemeow.dev_randompoetryscreen.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import online.dicemeow.dev_randompoetryscreen.data.model.Anthology
import online.dicemeow.dev_randompoetryscreen.data.model.ColorSchemeStyle
import online.dicemeow.dev_randompoetryscreen.data.model.PoetryGenerationMethod
import online.dicemeow.dev_randompoetryscreen.data.model.PoetryLineCountRange
import online.dicemeow.dev_randompoetryscreen.data.model.TimeDisplayType
import online.dicemeow.dev_randompoetryscreen.data.model.UserConfig
import online.dicemeow.dev_randompoetryscreen.data.repository.PoetryRepository
import online.dicemeow.dev_randompoetryscreen.data.store.PreferencesStore

data class ConfigUiState(
    val config: UserConfig = UserConfig(),
    val anthologies: List<Anthology> = emptyList()
)

class ConfigViewModel(
    private val preferencesStore: PreferencesStore,
    private val repository: PoetryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val config = preferencesStore.userConfig.first()
            _uiState.value = ConfigUiState(
                config = config,
                anthologies = repository.data.value.anthologies
            )
        }
        viewModelScope.launch {
            repository.data.collect { data ->
                _uiState.value = _uiState.value.copy(anthologies = data.anthologies)
            }
        }
        viewModelScope.launch {
            preferencesStore.userConfig.collect { config ->
                _uiState.value = _uiState.value.copy(config = config)
            }
        }
    }

    fun updateDisplayInterval(seconds: Int) {
        viewModelScope.launch {
            preferencesStore.update { it.copy(displayIntervalSec = seconds.coerceAtLeast(1)) }
        }
    }

    fun updateDisplaySpeed(msPerChar: Int) {
        viewModelScope.launch {
            preferencesStore.update { it.copy(displaySpeedMsPerChar = msPerChar.coerceAtLeast(1)) }
        }
    }

    fun updateLineCountMin(min: Int) {
        viewModelScope.launch {
            val current = _uiState.value.config
            val newMin = min.coerceIn(1, 99)
            val newMax = if (newMin > current.lineCountRange.max) newMin else current.lineCountRange.max
            preferencesStore.update { it.copy(lineCountRange = PoetryLineCountRange(newMin, newMax)) }
        }
    }

    fun updateLineCountMax(max: Int) {
        viewModelScope.launch {
            val current = _uiState.value.config
            val newMax = max.coerceIn(1, 99)
            val newMin = if (newMax < current.lineCountRange.min) newMax else current.lineCountRange.min
            preferencesStore.update { it.copy(lineCountRange = PoetryLineCountRange(newMin, newMax)) }
        }
    }

    fun toggleAnthologySelection(id: String) {
        viewModelScope.launch {
            val current = _uiState.value.config
            val newSet = if (id in current.selectedAnthologyIds) {
                current.selectedAnthologyIds - id
            } else {
                current.selectedAnthologyIds + id
            }
            preferencesStore.update { it.copy(selectedAnthologyIds = newSet) }
        }
    }

    fun updateGenerationMethod(method: PoetryGenerationMethod) {
        viewModelScope.launch {
            preferencesStore.update { it.copy(generationMethod = method) }
        }
    }

    fun updateTimeDisplayType(type: TimeDisplayType) {
        viewModelScope.launch {
            preferencesStore.update { it.copy(timeDisplayType = type) }
        }
    }

    fun updatePomodoroDuration(minutes: Int) {
        viewModelScope.launch {
            preferencesStore.update { it.copy(pomodoroDurationMin = minutes.coerceIn(1, 120)) }
        }
    }

    fun updateColorSchemeStyle(style: ColorSchemeStyle) {
        viewModelScope.launch {
            preferencesStore.update { it.copy(colorSchemeStyle = style) }
        }
    }
}
