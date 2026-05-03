package com.localide.ui.filemanager

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localide.model.FileItem
import com.localide.ui.theme.*
import com.localide.viewmodel.EditorViewModel
import com.localide.viewmodel.FileManagerViewModel
import com.localide.viewmodel.SortBy
import java.io.File

@Composable
fun FileManagerScreen(
    fileManagerVm: FileManagerViewModel = viewModel(),
    editorVm: EditorViewModel = viewModel()
) {
    val state by fileManagerVm.state.collectAsState()
    var newFileName by remember { mutableStateOf("") }
    var newFolderName by remember { mutableStateOf("") }
    var renameText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeBackground)
    ) {
        FileManagerToolbar(
            vm = fileManagerVm,
            currentPath = state.currentDir.path
        )
        BreadcrumbBar(currentDir = state.currentDir, onNavigate = fileManagerVm::navigateTo)

        if (state.error != null) {
            ErrorBanner(error = state.error!!, onDismiss = fileManagerVm::clearError)
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IdeAccent, modifier = Modifier.size(32.dp))
            }
        } else if (state.items.isEmpty()) {
            EmptyDirectory()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.items, key = { it.path }) { item ->
                    FileItemRow(
                        item = item,
                        onItemClick = {
                            if (item.isDirectory) {
                                fileManagerVm.navigateTo(item.file)
                            } else {
                                editorVm.openFile(item.file)
                            }
                        },
                        onLongClick = {
                            fileManagerVm.setContextMenu(item.file)
                            if (!item.isDirectory) renameText = item.name
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // Context Menu
    if (state.contextMenuFile != null) {
        val target = state.contextMenuFile!!
        AlertDialog(
            onDismissRequest = { fileManagerVm.setContextMenu(null) },
            containerColor = IdeSurface,
            title = {
                Text(target.name, style = MaterialTheme.typography.titleMedium, color = IdeOnBackground,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!target.isDirectory) {
                        ContextAction("Open in Editor", Icons.Filled.Code) {
                            editorVm.openFile(target)
                            fileManagerVm.setContextMenu(null)
                        }
                    }
                    ContextAction("Rename", Icons.Filled.Edit) {
                        fileManagerVm.setContextMenu(null)
                        fileManagerVm.showRenameDialog(true)
                    }
                    ContextAction("Delete", Icons.Filled.Delete, color = IdeRed) {
                        fileManagerVm.setContextMenu(null)
                        fileManagerVm.showDeleteConfirm(true)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { fileManagerVm.setContextMenu(null) }) {
                    Text("Cancel", color = IdeOnSurfaceVariant)
                }
            }
        )
    }

    // New File Dialog
    if (state.showNewFileDialog) {
        InputDialog(
            title = "New File",
            placeholder = "filename.kt",
            value = newFileName,
            onValueChange = { newFileName = it },
            onConfirm = {
                fileManagerVm.createFile(newFileName.trim())
                newFileName = ""
                fileManagerVm.showNewFileDialog(false)
            },
            onDismiss = { fileManagerVm.showNewFileDialog(false); newFileName = "" }
        )
    }

    // New Folder Dialog
    if (state.showNewFolderDialog) {
        InputDialog(
            title = "New Folder",
            placeholder = "folder-name",
            value = newFolderName,
            onValueChange = { newFolderName = it },
            onConfirm = {
                fileManagerVm.createFolder(newFolderName.trim())
                newFolderName = ""
                fileManagerVm.showNewFolderDialog(false)
            },
            onDismiss = { fileManagerVm.showNewFolderDialog(false); newFolderName = "" }
        )
    }

    // Rename Dialog
    if (state.showRenameDialog) {
        InputDialog(
            title = "Rename",
            placeholder = "new-name",
            value = renameText,
            onValueChange = { renameText = it },
            onConfirm = {
                state.contextMenuFile?.let { fileManagerVm.renameFile(it, renameText.trim()) }
                fileManagerVm.showRenameDialog(false)
            },
            onDismiss = { fileManagerVm.showRenameDialog(false) }
        )
    }

    // Delete Confirm
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { fileManagerVm.showDeleteConfirm(false) },
            containerColor = IdeSurface,
            title = { Text("Delete", color = IdeRed) },
            text = { Text("Delete \"${state.contextMenuFile?.name}\"? This cannot be undone.", color = IdeOnSurface) },
            confirmButton = {
                TextButton(onClick = {
                    state.contextMenuFile?.let { fileManagerVm.deleteFile(it) }
                    fileManagerVm.showDeleteConfirm(false)
                }) { Text("Delete", color = IdeRed) }
            },
            dismissButton = {
                TextButton(onClick = { fileManagerVm.showDeleteConfirm(false) }) {
                    Text("Cancel", color = IdeOnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun FileManagerToolbar(vm: FileManagerViewModel, currentPath: String) {
    var showSortMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(IdeSurface)
            .drawBehind {
                drawLine(IdeBorder, Offset(0f, size.height), Offset(size.width, size.height), 1f)
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("FILES", style = MaterialTheme.typography.titleSmall, color = IdeOnSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(onClick = { vm.showNewFileDialog(true) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.NoteAdd, "New File", tint = IdeOnSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { vm.showNewFolderDialog(true) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.CreateNewFolder, "New Folder", tint = IdeOnSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = vm::navigateHome, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Home, "Home", tint = IdeOnSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = vm::refresh, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Refresh, "Refresh", tint = IdeOnSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            Box {
                IconButton(onClick = { showSortMenu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Sort, "Sort", tint = IdeOnSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }, containerColor = IdeSurface) {
                    Text("Sort by", style = MaterialTheme.typography.titleSmall, color = IdeOnSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                    SortBy.entries.forEach { sort ->
                        DropdownMenuItem(
                            text = { Text(sort.name.lowercase().replaceFirstChar { it.uppercase() }, color = IdeOnSurface) },
                            onClick = { vm.setSortBy(sort); showSortMenu = false }
                        )
                    }
                    HorizontalDivider(color = IdeBorder)
                    DropdownMenuItem(
                        text = { Text("Toggle Hidden Files", color = IdeOnSurface) },
                        onClick = { vm.toggleHidden(); showSortMenu = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun BreadcrumbBar(currentDir: File, onNavigate: (File) -> Unit) {
    val parts = buildList {
        var f: File? = currentDir
        while (f != null) { add(f); f = f.parentFile }
        reverse()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(IdeSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        parts.forEachIndexed { i, file ->
            if (i > 0) Text(" / ", style = MaterialTheme.typography.bodySmall, color = IdeOnSurfaceVariant)
            Text(
                text = if (file.name.isEmpty()) "/" else file.name,
                style = MaterialTheme.typography.bodySmall,
                color = if (file == currentDir) IdeAccent else IdeOnSurfaceVariant,
                modifier = Modifier.clickable { onNavigate(file) }
            )
        }
    }
}

@Composable
private fun FileItemRow(item: FileItem, onItemClick: () -> Unit, onLongClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onItemClick, onLongClick = onLongClick)
            .drawBehind {
                drawLine(IdeBorder, Offset(0f, size.height - 0.5f), Offset(size.width, size.height - 0.5f), 0.5f)
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = getFileIcon(item),
            fontSize = 18.sp
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.isDirectory) IdeCyan else IdeOnBackground,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            if (!item.isDirectory) {
                Text(
                    text = item.formattedSize(),
                    style = MaterialTheme.typography.bodySmall,
                    color = IdeOnSurfaceVariant
                )
            }
        }
        if (item.isDirectory) {
            Icon(Icons.Filled.ChevronRight, null, tint = IdeOnSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
}

fun getFileIcon(item: FileItem): String = when {
    item.isDirectory -> "📁"
    else -> when (item.extension) {
        "kt", "kts" -> "🟣"
        "java" -> "☕"
        "js", "jsx" -> "🟡"
        "ts", "tsx" -> "🔵"
        "py" -> "🐍"
        "html", "htm" -> "🌐"
        "css", "scss" -> "🎨"
        "json" -> "📋"
        "xml" -> "📐"
        "md" -> "📝"
        "sh", "bash" -> "⚙️"
        "png", "jpg", "jpeg", "gif", "webp" -> "🖼️"
        "mp4", "mkv" -> "🎬"
        "mp3", "wav" -> "🎵"
        "zip", "tar", "gz" -> "📦"
        "pdf" -> "📄"
        "txt" -> "📄"
        else -> "📄"
    }
}

@Composable
private fun ContextAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color = IdeOnSurface, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
private fun InputDialog(
    title: String, placeholder: String, value: String,
    onValueChange: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IdeSurface,
        title = { Text(title, color = IdeOnBackground) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = IdeOnSurfaceVariant) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IdeAccent,
                    unfocusedBorderColor = IdeBorder,
                    focusedTextColor = IdeOnBackground,
                    unfocusedTextColor = IdeOnBackground,
                    cursorColor = IdeAccent
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = value.isNotBlank()) {
                Text("Create", color = IdeAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = IdeOnSurfaceVariant) }
        }
    )
}

@Composable
private fun EmptyDirectory() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📂", fontSize = 40.sp)
            Text("Empty directory", style = MaterialTheme.typography.bodyMedium, color = IdeOnSurfaceVariant)
        }
    }
}

@Composable
private fun ErrorBanner(error: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IdeRed.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(error, style = MaterialTheme.typography.bodySmall, color = IdeRed, modifier = Modifier.weight(1f))
        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Filled.Close, "Dismiss", tint = IdeRed, modifier = Modifier.size(14.dp))
        }
    }
}
