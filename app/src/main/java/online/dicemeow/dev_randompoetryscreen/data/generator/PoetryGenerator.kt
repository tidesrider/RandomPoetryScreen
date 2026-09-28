package online.dicemeow.dev_randompoetryscreen.data.generator

import online.dicemeow.dev_randompoetryscreen.data.model.EntryType
import online.dicemeow.dev_randompoetryscreen.data.model.PoetryEntry
import online.dicemeow.dev_randompoetryscreen.data.model.PoetryGenerationMethod
import online.dicemeow.dev_randompoetryscreen.data.model.PoetryLineCountRange
import kotlin.random.Random

object PoetryGenerator {

    fun generate(
        entries: List<PoetryEntry>,
        method: PoetryGenerationMethod,
        lineRange: PoetryLineCountRange,
        random: Random = Random.Default
    ): String {
        if (entries.isEmpty()) return ""

        return when (method) {
            PoetryGenerationMethod.RANDOM_PARAGRAPH -> generateRandomParagraph(entries, random)
            PoetryGenerationMethod.RANDOM_COMBINE_LINES -> generateRandomCombineLines(entries, lineRange, random)
            PoetryGenerationMethod.RANDOM_COMBINE_PARAGRAPH_AND_LINES -> generateRandomCombineMixed(entries, lineRange, random)
        }
    }

    private fun generateRandomParagraph(entries: List<PoetryEntry>, random: Random): String {
        val paragraphs = entries.filter { it.type == EntryType.PARAGRAPH }
        val pool = if (paragraphs.isNotEmpty()) paragraphs else entries
        return pool.random(random).content
    }

    private fun generateRandomCombineLines(
        entries: List<PoetryEntry>,
        lineRange: PoetryLineCountRange,
        random: Random
    ): String {
        val lines = entries.filter { it.type == EntryType.LINE }
        val pool = if (lines.isNotEmpty()) lines else entries.flatMap { it.content.split('\n') }
            .filter { it.isNotBlank() }
            .map { PoetryEntry("", "", EntryType.LINE, it) }

        if (pool.isEmpty()) return ""

        val targetCount = random.nextInt(lineRange.min, lineRange.max + 1)
            .coerceAtMost(pool.size)

        return pool.shuffled(random).take(targetCount).joinToString("\n") { it.content }
    }

    private fun generateRandomCombineMixed(
        entries: List<PoetryEntry>,
        lineRange: PoetryLineCountRange,
        random: Random
    ): String {
        val paragraphs = entries.filter { it.type == EntryType.PARAGRAPH }
        val lines = entries.filter { it.type == EntryType.LINE }

        if (paragraphs.isEmpty() && lines.isEmpty()) return ""
        if (paragraphs.isEmpty()) return generateRandomCombineLines(entries, lineRange, random)
        if (lines.isEmpty()) return generateRandomParagraph(entries, random)

        val targetSum = random.nextInt(lineRange.min, lineRange.max + 1)

        val pMode = paragraphs.map { it.lineCount }
            .groupingBy { it }.eachCount()
            .maxByOrNull { it.value }?.key ?: 1

        val pNum = if (pMode > 0) targetSum / pMode else 0
        val selectedParagraphs = paragraphs.shuffled(random).take(random.nextInt(0, pNum + 1))

        val currentSum = selectedParagraphs.sumOf { it.lineCount }

        val result = if (currentSum < targetSum) {
            val need = targetSum - currentSum
            val fillLines = lines.shuffled(random).take(need.coerceAtMost(lines.size))
            val parts = selectedParagraphs.map { it.content } + fillLines.map { it.content }
            parts.shuffled(random).joinToString("\n")
        } else {
            selectedParagraphs.map { it.content }.joinToString("\n")
        }

        return result
    }
}
