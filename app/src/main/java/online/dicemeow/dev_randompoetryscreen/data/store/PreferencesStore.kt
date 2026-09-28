package online.dicemeow.dev_randompoetryscreen.data.store

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import online.dicemeow.dev_randompoetryscreen.data.model.ColorSchemeStyle
import online.dicemeow.dev_randompoetryscreen.data.model.PoetryGenerationMethod
import online.dicemeow.dev_randompoetryscreen.data.model.PoetryLineCountRange
import online.dicemeow.dev_randompoetryscreen.data.model.TimeDisplayType
import online.dicemeow.dev_randompoetryscreen.data.model.UserConfig

private val Context.userPreferencesDataStore by preferencesDataStore("user_config")

inline fun <reified T : Enum<T>> enumValueOfOrNull(value: String?): T? {
    if (value == null) return null
    return try {
        enumValueOf<T>(value)
    } catch (e: IllegalArgumentException) {
        null
    }
}

class PreferencesStore(private val context: Context) {

    private object Keys {
        val DISPLAY_INTERVAL = intPreferencesKey("display_interval_sec")
        val DISPLAY_SPEED = intPreferencesKey("display_speed_ms_per_char")
        val LINE_COUNT_MIN = intPreferencesKey("line_count_min")
        val LINE_COUNT_MAX = intPreferencesKey("line_count_max")
        val SELECTED_ANTHOLOGIES = stringSetPreferencesKey("selected_anthology_ids")
        val GENERATION_METHOD = stringPreferencesKey("generation_method")
        val TIME_DISPLAY_TYPE = stringPreferencesKey("time_display_type")
        val POMODORO_DURATION = intPreferencesKey("pomodoro_duration_min")
        val COLOR_SCHEME_STYLE = stringPreferencesKey("color_scheme_style")
    }

    val userConfig: Flow<UserConfig> = context.userPreferencesDataStore.data.map { prefs ->
        UserConfig(
            displayIntervalSec = prefs[Keys.DISPLAY_INTERVAL] ?: 15,
            displaySpeedMsPerChar = prefs[Keys.DISPLAY_SPEED] ?: 60,
            lineCountRange = PoetryLineCountRange(
                min = (prefs[Keys.LINE_COUNT_MIN] ?: 1).coerceIn(1, 99),
                max = (prefs[Keys.LINE_COUNT_MAX] ?: 12).coerceIn(1, 99)
            ),
            selectedAnthologyIds = prefs[Keys.SELECTED_ANTHOLOGIES] ?: emptySet(),
            generationMethod = enumValueOfOrNull<PoetryGenerationMethod>(
                prefs[Keys.GENERATION_METHOD]
            ) ?: PoetryGenerationMethod.RANDOM_PARAGRAPH,
            timeDisplayType = enumValueOfOrNull<TimeDisplayType>(
                prefs[Keys.TIME_DISPLAY_TYPE]
            ) ?: TimeDisplayType.REAL_TIME,
            pomodoroDurationMin = prefs[Keys.POMODORO_DURATION] ?: 25,
            colorSchemeStyle = enumValueOfOrNull<ColorSchemeStyle>(
                prefs[Keys.COLOR_SCHEME_STYLE]
            ) ?: ColorSchemeStyle.TERMINAL_GREEN
        )
    }

    suspend fun update(transform: (UserConfig) -> UserConfig) {
        context.userPreferencesDataStore.edit { prefs ->
            val current = UserConfig(
                displayIntervalSec = prefs[Keys.DISPLAY_INTERVAL] ?: 15,
                displaySpeedMsPerChar = prefs[Keys.DISPLAY_SPEED] ?: 60,
                lineCountRange = PoetryLineCountRange(
                    min = (prefs[Keys.LINE_COUNT_MIN] ?: 1).coerceIn(1, 99),
                    max = (prefs[Keys.LINE_COUNT_MAX] ?: 12).coerceIn(1, 99)
                ),
                selectedAnthologyIds = prefs[Keys.SELECTED_ANTHOLOGIES] ?: emptySet(),
                generationMethod = enumValueOfOrNull<PoetryGenerationMethod>(
                    prefs[Keys.GENERATION_METHOD]
                ) ?: PoetryGenerationMethod.RANDOM_PARAGRAPH,
                timeDisplayType = enumValueOfOrNull<TimeDisplayType>(
                prefs[Keys.TIME_DISPLAY_TYPE]
            ) ?: TimeDisplayType.REAL_TIME,
            pomodoroDurationMin = prefs[Keys.POMODORO_DURATION] ?: 25,
            colorSchemeStyle = enumValueOfOrNull<ColorSchemeStyle>(
                prefs[Keys.COLOR_SCHEME_STYLE]
            ) ?: ColorSchemeStyle.TERMINAL_GREEN
        )
        val updated = transform(current)
        prefs[Keys.DISPLAY_INTERVAL] = updated.displayIntervalSec
        prefs[Keys.DISPLAY_SPEED] = updated.displaySpeedMsPerChar
        prefs[Keys.LINE_COUNT_MIN] = updated.lineCountRange.min
        prefs[Keys.LINE_COUNT_MAX] = updated.lineCountRange.max
        prefs[Keys.SELECTED_ANTHOLOGIES] = updated.selectedAnthologyIds
        prefs[Keys.GENERATION_METHOD] = updated.generationMethod.name
        prefs[Keys.TIME_DISPLAY_TYPE] = updated.timeDisplayType.name
        prefs[Keys.POMODORO_DURATION] = updated.pomodoroDurationMin
        prefs[Keys.COLOR_SCHEME_STYLE] = updated.colorSchemeStyle.name
    }

}}
