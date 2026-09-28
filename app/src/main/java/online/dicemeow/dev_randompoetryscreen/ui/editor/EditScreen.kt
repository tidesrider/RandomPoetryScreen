package online.dicemeow.dev_randompoetryscreen.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import online.dicemeow.dev_randompoetryscreen.AppContainer
import online.dicemeow.dev_randompoetryscreen.ui.ViewModelFactories
import online.dicemeow.dev_randompoetryscreen.ui.anthology.PixelButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    anthologyId: String,
    entryId: String,
    onBack: () -> Unit,
    appContainer: AppContainer
) {
    val viewModel: EditViewModel = viewModel(
        factory = ViewModelFactories.editFactory(appContainer, anthologyId, entryId)
    )
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var showFindReplace by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditing) "> EDIT" else "> NEW",
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
                    IconButton(onClick = { showFindReplace = !showFindReplace }) {
                        Icon(Icons.Default.FindReplace, contentDescription = "查找替换", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(uiState.text))
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "复制", tint = MaterialTheme.colorScheme.primary)
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
                .padding(horizontal = 16.dp)
        ) {
            if (showFindReplace) {
                FindReplacePanel(
                    findText = uiState.findText,
                    replaceText = uiState.replaceText,
                    matchCount = uiState.matchCount,
                    onFindChange = viewModel::updateFindText,
                    onReplaceChange = viewModel::updateReplaceText,
                    onReplaceAll = { viewModel.replaceAll() }
                )
                Spacer(modifier = Modifier.size(8.dp))
            }

            OutlinedTextField(
                value = uiState.text,
                onValueChange = viewModel::updateText,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                ),
                placeholder = { Text("// 在此输入文本...", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            )

            Text(
                text = "// ${uiState.text.length} 字符 | ${uiState.text.lines().size} 行",
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Right,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PixelButton(
                    text = "保存为行",
                    onClick = viewModel::saveAsLine,
                    modifier = Modifier.weight(1f)
                )
                PixelButton(
                    text = "保存为段落",
                    onClick = viewModel::saveAsParagraph,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    uiState.message?.let { msg ->
        AlertDialog(
            onDismissRequest = {
                viewModel.clearMessage()
                if (msg.startsWith("已保存")) onBack()
            },
            title = { Text("提示", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearMessage()
                    if (msg.startsWith("已保存")) onBack()
                }) { Text("确定") }
            }
        )
    }
}

@Composable
private fun FindReplacePanel(
    findText: String,
    replaceText: String,
    matchCount: Int,
    onFindChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onReplaceAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        OutlinedTextField(
            value = findText,
            onValueChange = onFindChange,
            label = { Text("查找", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        )
        Spacer(modifier = Modifier.size(4.dp))
        OutlinedTextField(
            value = replaceText,
            onValueChange = onReplaceChange,
            label = { Text("替换为", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "匹配: $matchCount",
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            PixelButton(
                text = "全部替换",
                onClick = onReplaceAll
            )
        }
    }
}
