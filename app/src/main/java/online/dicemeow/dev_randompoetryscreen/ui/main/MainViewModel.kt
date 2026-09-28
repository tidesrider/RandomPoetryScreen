package online.dicemeow.dev_randompoetryscreen.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import online.dicemeow.dev_randompoetryscreen.data.generator.PoetryGenerator
import online.dicemeow.dev_randompoetryscreen.data.model.TimeDisplayType
import online.dicemeow.dev_randompoetryscreen.data.model.UserConfig
import online.dicemeow.dev_randompoetryscreen.data.repository.PoetryRepository
import online.dicemeow.dev_randompoetryscreen.data.store.PreferencesStore
import kotlin.random.Random

data class MainUiState(
    val currentPoem: String = "",
    val isRevealing: Boolean = false,
    val config: UserConfig = UserConfig(),
    val timeDisplay: String = "",
    val hasData: Boolean = false
)

class MainViewModel(
    private val repository: PoetryRepository,
    private val preferencesStore: PreferencesStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val appStartTime = System.currentTimeMillis()
    private var pomodoroStartTime = System.currentTimeMillis()
    @Volatile
    private var intervalStartTime = System.currentTimeMillis()

    private var cycleJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            val config = preferencesStore.userConfig.first()
            val hasData = repository.getAllEntries().isNotEmpty()
            _uiState.value = _uiState.value.copy(config = config, hasData = hasData)
            startCycle(config)
        }
        viewModelScope.launch {
            preferencesStore.userConfig.collect { config ->
                _uiState.value = _uiState.value.copy(config = config)
            }
        }
        startTimeUpdater()
    }

    private fun startCycle(config: UserConfig) {
        cycleJob?.cancel()
        cycleJob = viewModelScope.launch {
            while (true) {
                val currentConfig = _uiState.value.config
                val poem = generatePoem(currentConfig)
                if (poem.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        currentPoem = poem,
                        isRevealing = true,
                        hasData = true
                    )
                    val revealDuration = poem.length * currentConfig.displaySpeedMsPerChar.toLong()
                    kotlinx.coroutines.delay(revealDuration + 500)
                    _uiState.value = _uiState.value.copy(isRevealing = false)
                    intervalStartTime = System.currentTimeMillis()
                    kotlinx.coroutines.delay(currentConfig.displayIntervalSec * 1000L)
                } else {
                    _uiState.value = _uiState.value.copy(hasData = false, currentPoem = "")
                    kotlinx.coroutines.delay(3000)
                }
            }
        }
    }

    fun refreshConfig() {
        viewModelScope.launch {
            val config = preferencesStore.userConfig.first()
            val hasData = repository.getAllEntries().isNotEmpty()
            _uiState.value = _uiState.value.copy(config = config, hasData = hasData)
            startCycle(config)
        }
    }

    private fun generatePoem(config: UserConfig): String {
        val anthologyIds = config.selectedAnthologyIds.ifEmpty {
            repository.data.value.anthologies.map { it.id }.toSet()
        }
        val entries = repository.getEntries(anthologyIds)
        if (entries.isEmpty()) return ""
        return PoetryGenerator.generate(
            entries = entries,
            method = config.generationMethod,
            lineRange = config.lineCountRange,
            random = Random.Default
        )
    }

    private fun startTimeUpdater() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(500)
                val config = _uiState.value.config
                val timeText = when (config.timeDisplayType) {
                    TimeDisplayType.REAL_TIME -> {
                        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        sdf.format(java.util.Date())
                    }
                    TimeDisplayType.APP_RUNTIME -> {
                        val elapsed = (System.currentTimeMillis() - appStartTime) / 1000
                        val h = elapsed / 3600
                        val m = (elapsed % 3600) / 60
                        val s = elapsed % 60
                        String.format("%02d:%02d:%02d", h, m, s)
                    }
                    TimeDisplayType.REFRESH_COUNTDOWN -> {
                        val elapsedSec = (System.currentTimeMillis() - intervalStartTime) / 1000
                        val remaining = config.displayIntervalSec - elapsedSec
                        if (remaining > 0) "${remaining}s" else "0s"
                    }
                    TimeDisplayType.POMODORO_COUNTDOWN -> {
                        val totalSec = config.pomodoroDurationMin * 60
                        val elapsed = ((System.currentTimeMillis() - pomodoroStartTime) / 1000).toInt() % (totalSec * 2)
                        val remaining = if (elapsed < totalSec) totalSec - elapsed else (totalSec * 2) - elapsed
                        val m = remaining / 60
                        val s = remaining % 60
                        String.format("%02d:%02d", m, s)
                    }
                }
                _uiState.value = _uiState.value.copy(timeDisplay = timeText)
            }
        }
    }
}
