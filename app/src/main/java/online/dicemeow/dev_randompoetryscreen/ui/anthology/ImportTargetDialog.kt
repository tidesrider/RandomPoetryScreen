package online.dicemeow.dev_randompoetryscreen.ui.anthology

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.dicemeow.dev_randompoetryscreen.data.model.Anthology
import online.dicemeow.dev_randompoetryscreen.ui.theme.TerminalGreen
import online.dicemeow.dev_randompoetryscreen.ui.theme.TerminalGreenDim

@Composable
fun ImportTargetDialog(
    anthologies: List<Anthology>,
    onSelectExisting: (String) -> Unit,
    onCreateNew: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    if (showCreateNew) {
        AlertDialog(
            onDismissRequest = { showCreateNew = false },
            title = { Text("新建诗集", fontFamily = FontFamily.Monospace, color = TerminalGreen) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("诗集名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        onCreateNew(newName.trim())
                        newName = ""
                    }
                }) { Text("创建并导入") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateNew = false }) { Text("取消") }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("存储到哪个诗集", fontFamily = FontFamily.Monospace, color = TerminalGreen) },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCreateNew = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+ 新建一个诗集",
                        fontFamily = FontFamily.Monospace,
                        color = TerminalGreen,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.size(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    anthologies.forEach { anthology ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectExisting(anthology.id) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = anthology.name,
                                    fontFamily = FontFamily.Monospace,
                                    color = TerminalGreen,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${anthology.entries.size} entries",
                                    fontFamily = FontFamily.Monospace,
                                    color = TerminalGreenDim,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
