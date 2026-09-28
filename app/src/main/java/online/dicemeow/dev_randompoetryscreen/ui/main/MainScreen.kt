package online.dicemeow.dev_randompoetryscreen.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import online.dicemeow.dev_randompoetryscreen.AppContainer
import online.dicemeow.dev_randompoetryscreen.ui.ViewModelFactories
import online.dicemeow.dev_randompoetryscreen.ui.components.DecryptionTextScrollable

@Composable
fun MainScreen(
    onNavigateToAnthologyList: () -> Unit,
    onNavigateToConfig: () -> Unit,
    appContainer: AppContainer
) {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel(
        factory = ViewModelFactories.mainFactory(appContainer)
    )

    val uiState by viewModel.uiState.collectAsState()
    val window = remember { (context as? android.app.Activity)?.window }
    val colors = MaterialTheme.colorScheme
    var buttonsVisible by remember { mutableStateOf(false) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .clickable {
                buttonsVisible = !buttonsVisible
            }
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(12.dp)
                .alpha(if (buttonsVisible) 0.7f else 0f)
        ) {
            IconButton(
                onClick = {
                    buttonsVisible = false
                    onNavigateToAnthologyList()
                },
                modifier = Modifier
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "诗集",
                    tint = colors.primary
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(12.dp)
                .alpha(if (buttonsVisible) 0.7f else 0f)
        ) {
            IconButton(
                onClick = {
                    buttonsVisible = false
                    onNavigateToConfig()
                },
                modifier = Modifier
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "配置",
                    tint = colors.primary
                )
            }
        }

        if (uiState.timeDisplay.isNotEmpty()) {
            Text(
                text = uiState.timeDisplay,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                color = colors.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
        }

        if (uiState.hasData && uiState.currentPoem.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                DecryptionTextScrollable(
                    targetText = uiState.currentPoem,
                    speedMsPerChar = uiState.config.displaySpeedMsPerChar,
                    displayIntervalMs = uiState.config.displayIntervalSec * 1000L,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 32.sp,
                        letterSpacing = 1.sp
                    ),
                    revealedColor = colors.primary,
                    linesPerBlock = 6
                )
            }
        } else if (!uiState.hasData) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "> NO_DATA_FOUND",
                    color = colors.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = "// 请点击左上角按钮创建诗集",
                    color = colors.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}