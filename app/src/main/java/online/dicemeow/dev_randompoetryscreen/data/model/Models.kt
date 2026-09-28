package online.dicemeow.dev_randompoetryscreen.data.model

enum class EntryType { LINE, PARAGRAPH }

enum class SplitMode {
    BY_LINE,
    BY_TITLE,
    NO_SPLIT
}

enum class PoetryGenerationMethod {
    RANDOM_PARAGRAPH,
    RANDOM_COMBINE_LINES,
    RANDOM_COMBINE_PARAGRAPH_AND_LINES
}

enum class TimeDisplayType {
    REFRESH_COUNTDOWN,
    APP_RUNTIME,
    POMODORO_COUNTDOWN,
    REAL_TIME
}

enum class ColorSchemeStyle {
    TERMINAL_GREEN,
    MONOCHROME,
    CYAN_BLUE,
    AMBER_ORANGE
}

data class PoetryEntry(
    val id: String,
    val anthologyId: String,
    val type: EntryType,
    val content: String
) {
    val lineCount: Int
        get() = if (type == EntryType.LINE) 1 else content.count { it == '\n' } + 1
}

data class Anthology(
    val id: String,
    val name: String,
    val entries: List<PoetryEntry> = emptyList()
)

data class AppData(
    val anthologies: List<Anthology> = emptyList()
)

data class PoetryLineCountRange(
    val min: Int = 1,
    val max: Int = 12
) {
    init {
        require(min in 1..99)
        require(max in 1..99)
        require(min <= max)
    }
}

data class UserConfig(
    val displayIntervalSec: Int = 15,
    val displaySpeedMsPerChar: Int = 60,
    val lineCountRange: PoetryLineCountRange = PoetryLineCountRange(),
    val selectedAnthologyIds: Set<String> = emptySet(),
    val generationMethod: PoetryGenerationMethod = PoetryGenerationMethod.RANDOM_PARAGRAPH,
    val timeDisplayType: TimeDisplayType = TimeDisplayType.REAL_TIME,
    val pomodoroDurationMin: Int = 25,
    val colorSchemeStyle: ColorSchemeStyle = ColorSchemeStyle.TERMINAL_GREEN
)
