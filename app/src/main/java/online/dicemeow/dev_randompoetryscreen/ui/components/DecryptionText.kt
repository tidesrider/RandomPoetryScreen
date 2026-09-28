package online.dicemeow.dev_randompoetryscreen.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import online.dicemeow.dev_randompoetryscreen.ui.theme.ArkPixelFontFamily
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val DECRYPT_CHARS = listOf(
    'A', 'B', 'C', 'D', 'E', 'F', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '+', '=', '?', '/',
    'ｱ', 'ｲ', 'ｳ', 'ｴ', 'ｵ', 'ｶ', 'ｷ', 'ｸ', 'ｹ', 'ｺ', 'ﾊ', 'ﾋ', 'ﾌ', 'ﾍ', 'ﾎ',
    'ﾏ', 'ﾐ', 'ﾑ', 'ﾒ', 'ﾓ', 'ﾔ', 'ﾕ', 'ﾖ', 'ﾗ', 'ﾘ', 'ﾙ', 'ﾚ', 'ﾛ', 'ﾜ', 'ﾝ',
    '日', '月', '風', '雲', '雨', '雪', '花', '鳥', '山', '川', '海', '空', '星',
    '光', '影', '夢', '詩', '歌', '心', '魂', '無', '空', '道', '念', '寂'
)

@Composable
fun DecryptionTextScrollable(
    targetText: String,
    speedMsPerChar: Int,
    displayIntervalMs: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    revealedColor: Color = MaterialTheme.colorScheme.primary,
    linesPerBlock: Int = 6,
    onComplete: () -> Unit = {}
) {
    val lines = targetText.split('\n')
    val totalLines = lines.size

    if (totalLines <= linesPerBlock) {
        DecryptionText(
            targetText = targetText,
            speedMsPerChar = speedMsPerChar,
            modifier = modifier,
            style = style,
            revealedColor = revealedColor,
            onComplete = onComplete
        )
        return
    }

    val blocks = remember(targetText) {
        val result = mutableListOf<String>()
        for (i in 0 until totalLines step linesPerBlock) {
            val end = minOf(i + linesPerBlock, totalLines)
            result.add(lines.subList(i, end).joinToString("\n"))
        }
        result
    }

    var currentBlockIndex by remember { mutableIntStateOf(0) }
    var blockCompleteCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(blockCompleteCallback) {
        blockCompleteCallback?.let {
            if (currentBlockIndex < blocks.size - 1) {
                delay(displayIntervalMs)
                currentBlockIndex++
            } else {
                onComplete()
            }
            blockCompleteCallback = null
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentBlockIndex,
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = slideInVertically(
                        initialOffsetY = { 300 },
                        animationSpec = tween(500)
                    ) + fadeIn(animationSpec = tween(200)),
                    initialContentExit = slideOutVertically(
                        targetOffsetY = { -300 },
                        animationSpec = tween(500)
                    ) + fadeOut(animationSpec = tween(200))
                )
            }
        ) { index ->
            DecryptionText(
                targetText = blocks[index],
                speedMsPerChar = speedMsPerChar,
                modifier = Modifier.fillMaxWidth(),
                style = style,
                revealedColor = revealedColor,
                onComplete = {
                    blockCompleteCallback = {}
                }
            )
        }
    }
}

@Composable
fun DecryptionText(
    targetText: String,
    speedMsPerChar: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    revealedColor: Color = MaterialTheme.colorScheme.primary,
    onComplete: () -> Unit = {}
) {
    if (targetText.isEmpty()) {
        Box(modifier = modifier)
        return
    }

    var revealedCount by remember(targetText) { mutableIntStateOf(0) }
    var scrambleFrame by remember(targetText) { mutableIntStateOf(0) }
    val totalChars = targetText.length

    LaunchedEffect(targetText) {
        revealedCount = 0
        coroutineScope {
            val scrambleJob = launch {
                while (true) {
                    delay(50)
                    scrambleFrame++
                }
            }
            while (revealedCount < totalChars) {
                delay(speedMsPerChar.toLong())
                revealedCount = (revealedCount + 1).coerceAtMost(totalChars)
            }
            scrambleJob.cancel()
        }
        onComplete()
    }

    val annotated = remember(revealedCount, scrambleFrame, targetText) {
        buildDecryptedString(targetText, revealedCount, scrambleFrame, revealedColor)
    }

    Text(
        text = annotated,
        modifier = modifier,
        style = style.copy(
            fontFamily = ArkPixelFontFamily,
            color = revealedColor,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    )
}

internal fun buildDecryptedString(
    target: String,
    revealedCount: Int,
    scrambleFrame: Int,
    revealedColor: Color
): AnnotatedString {
    val scrambleColor = revealedColor.copy(alpha = 0.35f)
    return buildAnnotatedString {
        for (i in target.indices) {
            val ch = target[i]
            if (ch == '\n') {
                append('\n')
                continue
            }
            if (i < revealedCount) {
                withStyle(SpanStyle(color = revealedColor)) {
                    append(ch)
                }
            } else {
                val randomChar = DECRYPT_CHARS[(scrambleFrame + i * 7) % DECRYPT_CHARS.size]
                withStyle(SpanStyle(color = scrambleColor)) {
                    append(randomChar)
                }
            }
        }
    }
}

@Composable
fun DecryptionTextSized(
    targetText: String,
    speedMsPerChar: Int,
    availableWidth: Float,
    availableHeight: Float,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val baseFontSize = remember(availableWidth, availableHeight, targetText) {
        computeFontSize(targetText, availableWidth, availableHeight, density.density)
    }

    DecryptionText(
        targetText = targetText,
        speedMsPerChar = speedMsPerChar,
        modifier = modifier.padding(horizontal = androidx.compose.ui.unit.Dp(16f)),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = baseFontSize.sp,
            lineHeight = (baseFontSize * 1.6f).sp,
            letterSpacing = 1.sp
        ),
        onComplete = onComplete
    )
}

private fun computeFontSize(
    text: String,
    availableWidthPx: Float,
    availableHeightPx: Float,
    density: Float
): Float {
    if (text.isEmpty()) return 16f
    val lines = text.split('\n')
    val maxLineLength = lines.maxOf { it.length }.coerceAtLeast(1)
    val lineCount = lines.size

    val widthDp = availableWidthPx / density
    val heightDp = availableHeightPx / density

    val sizeByWidth = (widthDp * 0.85f) / (maxLineLength * 0.62f)
    val sizeByHeight = (heightDp * 0.85f) / (lineCount * 1.6f)

    return minOf(sizeByWidth, sizeByHeight, 32f).coerceAtLeast(8f)
}