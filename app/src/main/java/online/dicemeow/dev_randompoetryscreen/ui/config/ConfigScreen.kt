package online.dicemeow.dev_randompoetryscreen.ui.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import online.dicemeow.dev_randompoetryscreen.AppContainer
import online.dicemeow.dev_randompoetryscreen.data.model.ColorSchemeStyle
import online.dicemeow.dev_randompoetryscreen.data.model.PoetryGenerationMethod
import online.dicemeow.dev_randompoetryscreen.data.model.TimeDisplayType
import online.dicemeow.dev_randompoetryscreen.ui.ViewModelFactories
import online.dicemeow.dev_randompoetryscreen.ui.anthology.PixelButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    onBack: () -> Unit,
    appContainer: AppContainer
) {
    val viewModel: ConfigViewModel = viewModel(
        factory = ViewModelFactories.configFactory(appContainer)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "> CONFIG",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    TextButton(onClick = onBack) {
                        Text("确定", color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ConfigCard(title = "显示时间间隔 (秒)") {
                var inputValue by remember { mutableStateOf(uiState.config.displayIntervalSec.toString()) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { v ->
                            if (v.isEmpty() || v.toIntOrNull() != null) {
                                inputValue = v
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f).onFocusChanged {
                            if (!it.isFocused) {
                                val value = inputValue.toIntOrNull() ?: 1
                                inputValue = value.toString()
                                viewModel.updateDisplayInterval(value)
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("秒", color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
                }
            }

            ConfigCard(title = "显示速度 (毫秒/字符)") {
                var inputValue by remember { mutableStateOf(uiState.config.displaySpeedMsPerChar.toString()) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { v ->
                            if (v.isEmpty() || v.toIntOrNull() != null) {
                                inputValue = v
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f).onFocusChanged {
                            if (!it.isFocused) {
                                val value = inputValue.toIntOrNull() ?: 1
                                inputValue = value.toString()
                                viewModel.updateDisplaySpeed(value)
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("ms/字", color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
                }
            }

            ConfigCard(title = "诗歌行数范围 [1-99]") {
                var minInput by remember { mutableStateOf(uiState.config.lineCountRange.min.toString()) }
                var maxInput by remember { mutableStateOf(uiState.config.lineCountRange.max.toString()) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = minInput,
                        onValueChange = { v ->
                            if (v.isEmpty() || v.toIntOrNull() != null) {
                                minInput = v
                            }
                        },
                        label = { Text("最小", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).onFocusChanged {
                            if (!it.isFocused) {
                                val value = minInput.toIntOrNull() ?: 1
                                minInput = value.toString()
                                viewModel.updateLineCountMin(value)
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("~", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedTextField(
                        value = maxInput,
                        onValueChange = { v ->
                            if (v.isEmpty() || v.toIntOrNull() != null) {
                                maxInput = v
                            }
                        },
                        label = { Text("最大", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).onFocusChanged {
                            if (!it.isFocused) {
                                val value = maxInput.toIntOrNull() ?: 1
                                maxInput = value.toString()
                                viewModel.updateLineCountMax(value)
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            ConfigCard(title = "随机显示范围") {
                if (uiState.anthologies.isEmpty()) {
                    Text(
                        text = "// 暂无诗集",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "// 不选则使用全部",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    uiState.anthologies.forEach { anthology ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleAnthologySelection(anthology.id) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = anthology.id in uiState.config.selectedAnthologyIds,
                                onCheckedChange = { viewModel.toggleAnthologySelection(anthology.id) }
                            )
                            Text(
                                text = anthology.name,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            ConfigCard(title = "诗歌生成方式") {
                PoetryGenerationMethod.values().forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateGenerationMethod(method) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.config.generationMethod == method,
                            onClick = { viewModel.updateGenerationMethod(method) }
                        )
                        Text(
                            text = when (method) {
                                PoetryGenerationMethod.RANDOM_PARAGRAPH -> "随机段落"
                                PoetryGenerationMethod.RANDOM_COMBINE_LINES -> "随机组合行"
                                PoetryGenerationMethod.RANDOM_COMBINE_PARAGRAPH_AND_LINES -> "随机组合段落和行"
                            },
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            ConfigCard(title = "时间显示") {
                TimeDisplayType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateTimeDisplayType(type) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.config.timeDisplayType == type,
                            onClick = { viewModel.updateTimeDisplayType(type) }
                        )
                        Text(
                            text = when (type) {
                                TimeDisplayType.REFRESH_COUNTDOWN -> "刷新倒计时"
                                TimeDisplayType.APP_RUNTIME -> "应用运行时间"
                                TimeDisplayType.POMODORO_COUNTDOWN -> "番茄钟倒计时"
                                TimeDisplayType.REAL_TIME -> "实时时间"
                            },
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    }
                }
                if (uiState.config.timeDisplayType == TimeDisplayType.POMODORO_COUNTDOWN) {
                    var inputValue by remember { mutableStateOf(uiState.config.pomodoroDurationMin.toString()) }
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("番茄时长: ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        OutlinedTextField(
                            value = inputValue,
                            onValueChange = { v ->
                                if (v.isEmpty() || v.toIntOrNull() != null) {
                                    inputValue = v
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f).onFocusChanged {
                                if (!it.isFocused) {
                                    val value = inputValue.toIntOrNull() ?: 1
                                    inputValue = value.toString()
                                    viewModel.updatePomodoroDuration(value)
                                }
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("分钟", color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
            }

            ConfigCard(title = "配色风格") {
                ColorSchemeStyle.values().forEach { style ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateColorSchemeStyle(style) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.config.colorSchemeStyle == style,
                            onClick = { viewModel.updateColorSchemeStyle(style) }
                        )
                        Text(
                            text = when (style) {
                                ColorSchemeStyle.TERMINAL_GREEN -> "终端绿"
                                ColorSchemeStyle.MONOCHROME -> "黑白"
                                ColorSchemeStyle.CYAN_BLUE -> "青蓝"
                                ColorSchemeStyle.AMBER_ORANGE -> "琥珀橙"
                            },
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun ConfigCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(8.dp))
            content()
        }
    }
}
