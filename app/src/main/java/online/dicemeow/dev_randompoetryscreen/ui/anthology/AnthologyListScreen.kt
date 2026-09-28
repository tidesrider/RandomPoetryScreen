package online.dicemeow.dev_randompoetryscreen.ui.anthology

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import online.dicemeow.dev_randompoetryscreen.AppContainer
import online.dicemeow.dev_randompoetryscreen.data.importer.FileType
import online.dicemeow.dev_randompoetryscreen.data.model.SplitMode
import online.dicemeow.dev_randompoetryscreen.ui.ViewModelFactories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnthologyListScreen(
    onBack: () -> Unit,
    onOpenAnthology: (String) -> Unit,
    onCreateAndEdit: (String) -> Unit,
    appContainer: AppContainer
) {
    val viewModel: AnthologyListViewModel = viewModel(
        factory = ViewModelFactories.anthologyListFactory(appContainer)
    )
    val uiState by viewModel.uiState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showMergeDialog by remember { mutableStateOf(false) }
    var showImportTargetDialog by remember { mutableStateOf(false) }
    var showExportMessage by remember { mutableStateOf<String?>(null) }
    var importMessage by remember { mutableStateOf<String?>(null) }

    val pendingFiles = remember { mutableStateListOf<Pair<Uri, String>>() }
    var currentSplitMode by remember { mutableStateOf(SplitMode.BY_LINE) }

    val txtImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val files = uris.mapNotNull { uri ->
                val name = appContainer.fileImportService.getFileName(uri) ?: "unknown.txt"
                Pair<Uri, String>(uri, name)
            }
            if (files.isNotEmpty()) {
                pendingFiles.clear()
                pendingFiles.addAll(files)
                currentSplitMode = SplitMode.BY_LINE
                showImportTargetDialog = true
            }
        }
    }

    val mdImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val files = uris.mapNotNull { uri ->
                val name = appContainer.fileImportService.getFileName(uri) ?: "unknown.md"
                Pair<Uri, String>(uri, name)
            }
            if (files.isNotEmpty()) {
                pendingFiles.clear()
                pendingFiles.addAll(files)
                currentSplitMode = SplitMode.BY_TITLE
                showImportTargetDialog = true
            }
        }
    }

    val rdpoemImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val json = appContainer.rdpoemFileService.importFromUri(uri)
            if (json != null && viewModel.importRdpoem(json)) {
                importMessage = "导入成功"
            } else {
                importMessage = "导入失败"
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val json = viewModel.exportAll()
            val success = appContainer.rdpoemFileService.exportToUri(uri, json)
            showExportMessage = if (success) "导出成功" else "导出失败"
        }
    }

    val inSelectionMode = uiState.selectedIds.isNotEmpty()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (inSelectionMode) "> SELECT[${uiState.selectedIds.size}]" else "> ANTHOLOGIES",
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
                    if (inSelectionMode) {
                        if (uiState.selectedIds.size >= 2) {
                            IconButton(onClick = { showMergeDialog = true }) {
                                Icon(Icons.Default.Merge, contentDescription = "合并", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Delete, contentDescription = "取消", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        IconButton(onClick = { rdpoemImportLauncher.launch(arrayOf("application/json", "*/*")) }) {
                            Icon(Icons.Default.Download, contentDescription = "导入.rdpoem", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { exportLauncher.launch("poetry_data.rdpoem") }) {
                            Icon(Icons.Default.Upload, contentDescription = "导出", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PixelButton(
                    text = "导入txt文件",
                    onClick = {
                        txtImportLauncher.launch(arrayOf("text/plain"))
                    },
                    modifier = Modifier.weight(1f)
                )
                PixelButton(
                    text = "导入md文件",
                    onClick = {
                        mdImportLauncher.launch(arrayOf("text/markdown", "text/x-markdown"))
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (uiState.anthologies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "// 暂无诗集\n// 点击右下角 + 创建",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        lineHeight = 24.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.anthologies, key = { it.id }) { anthology ->
                        AnthologyRow(
                            anthology = anthology,
                            isSelected = anthology.id in uiState.selectedIds,
                            inSelectionMode = inSelectionMode,
                            onClick = {
                                if (inSelectionMode) {
                                    viewModel.toggleSelection(anthology.id)
                                } else {
                                    onOpenAnthology(anthology.id)
                                }
                            },
                            onLongClick = { viewModel.toggleSelection(anthology.id) },
                            onRename = { showRenameDialog = anthology.id },
                            onDelete = { showDeleteDialog = anthology.id }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("新建诗集", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("诗集名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        val created = viewModel.createAnthology(name.trim())
                        showCreateDialog = false
                        onCreateAndEdit(created.id)
                    }
                }) { Text("创建并编辑") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("取消") }
            }
        )
    }

    showRenameDialog?.let { anthologyId ->
        val anthology = uiState.anthologies.find { it.id == anthologyId }
        var name by remember(anthologyId) { mutableStateOf(anthology?.name ?: "") }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("重命名诗集", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("新名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.renameAnthology(anthologyId, name.trim())
                    }
                    showRenameDialog = null
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) { Text("取消") }
            }
        )
    }

    if (showMergeDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showMergeDialog = false },
            title = { Text("合并诗集", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary) },
            text = {
                Column {
                    Text("将合并 ${uiState.selectedIds.size} 个诗集，合并后原诗集将删除")
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("新诗集名称") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.mergeSelected(name.trim())
                    }
                    showMergeDialog = false
                }) { Text("合并") }
            },
            dismissButton = {
                TextButton(onClick = { showMergeDialog = false }) { Text("取消") }
            }
        )
    }

    if (showImportTargetDialog) {
        ImportTargetDialog(
            anthologies = uiState.anthologies,
            onSelectExisting = { anthologyId ->
                showImportTargetDialog = false
                val result = viewModel.processImport(
                    appContainer.fileImportService,
                    pendingFiles.toList(),
                    currentSplitMode,
                    anthologyId
                )
                importMessage = result.message
                pendingFiles.clear()
            },
            onCreateNew = { name ->
                showImportTargetDialog = false
                val created = viewModel.createAnthology(name)
                val result = viewModel.processImport(
                    appContainer.fileImportService,
                    pendingFiles.toList(),
                    currentSplitMode,
                    created.id
                )
                importMessage = result.message
                pendingFiles.clear()
            },
            onDismiss = {
                showImportTargetDialog = false
                pendingFiles.clear()
            }
        )
    }

    importMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { importMessage = null },
            title = { Text("提示", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { importMessage = null }) { Text("确定") }
            }
        )
    }

    showExportMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { showExportMessage = null },
            title = { Text("提示", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { showExportMessage = null }) { Text("确定") }
            }
        )
    }

    showDeleteDialog?.let { anthologyId ->
        val anthology = uiState.anthologies.find { it.id == anthologyId }
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除诗集", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary) },
            text = { Text("确认删除「${anthology?.name ?: "未知"}」？删除后无法恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAnthology(anthologyId)
                    showDeleteDialog = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun AnthologyRow(
    anthology: online.dicemeow.dev_randompoetryscreen.data.model.Anthology,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (inSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anthology.name,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
                Text(
                    text = "${anthology.entries.size} entries",
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onRename) {
                Icon(Icons.Default.Edit, contentDescription = "重命名", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp
        )
    }
}
