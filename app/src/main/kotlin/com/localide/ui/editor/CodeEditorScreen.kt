package com.localide.ui.editor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localide.model.OpenFile
import com.localide.ui.theme.*
import com.localide.util.SyntaxHighlighter
import com.localide.viewmodel.EditorViewModel
import com.localide.viewmodel.SaveStatus

@Composable
fun CodeEditorScreen(vm: EditorViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeBackground)
    ) {
        EditorToolbar(
            vm = vm,
            saveStatus = state.saveStatus,
            showSearch = state.showSearch,
            fontSize = state.fontSize,
            showLineNumbers = state.showLineNumbers,
            wordWrap = state.wordWrap
        )

        if (state.openFiles.isEmpty()) {
            EditorWelcome()
        } else {
            FileTabs(
                files = state.openFiles,
                activeIndex = state.activeFileIndex,
                onSelect = vm::switchToFile,
                onClose = vm::closeFile
            )
            if (state.showSearch) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = vm::setSearchQuery,
                    onClose = vm::toggleSearch
                )
            }
            EditorBody(
                file = state.openFiles[state.activeFileIndex],
                fontSize = state.fontSize,
                showLineNumbers = state.showLineNumbers,
                wordWrap = state.wordWrap,
                searchQuery = state.searchQuery,
                onContentChange = vm::updateContent
            )
        }
    }
}

@Composable
private fun EditorToolbar(
    vm: EditorViewModel,
    saveStatus: SaveStatus,
    showSearch: Boolean,
    fontSize: Int,
    showLineNumbers: Boolean,
    wordWrap: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }
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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("EDITOR", style = MaterialTheme.typography.titleSmall, color = IdeOnSurfaceVariant)
            when (saveStatus) {
                SaveStatus.Saving -> Text("• saving", style = MaterialTheme.typography.bodySmall, color = IdeYellow)
                SaveStatus.Saved -> Text("• saved", style = MaterialTheme.typography.bodySmall, color = IdeGreen)
                SaveStatus.Error -> Text("• error", style = MaterialTheme.typography.bodySmall, color = IdeRed)
                else -> {}
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(onClick = vm::toggleSearch, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Search, "Search", tint = if (showSearch) IdeAccent else IdeOnSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = vm::saveCurrentFile, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Save, "Save", tint = IdeOnSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.MoreVert, "More", tint = IdeOnSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = IdeSurface
                ) {
                    DropdownMenuItem(
                        text = { Text("Font Size: $fontSize", color = IdeOnSurface) },
                        onClick = {},
                        leadingIcon = { Icon(Icons.Filled.TextFields, null, tint = IdeOnSurfaceVariant) }
                    )
                    DropdownMenuItem(
                        text = { Text("  −", color = IdeOnSurface) },
                        onClick = { vm.setFontSize(fontSize - 1); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("  +", color = IdeOnSurface) },
                        onClick = { vm.setFontSize(fontSize + 1); showMenu = false }
                    )
                    HorizontalDivider(color = IdeBorder)
                    DropdownMenuItem(
                        text = { Text(if (showLineNumbers) "Hide Line Numbers" else "Show Line Numbers", color = IdeOnSurface) },
                        onClick = { vm.toggleLineNumbers(); showMenu = false },
                        leadingIcon = { Icon(Icons.Filled.FormatListNumbered, null, tint = IdeOnSurfaceVariant) }
                    )
                    DropdownMenuItem(
                        text = { Text(if (wordWrap) "Disable Word Wrap" else "Enable Word Wrap", color = IdeOnSurface) },
                        onClick = { vm.toggleWordWrap(); showMenu = false },
                        leadingIcon = { Icon(Icons.Filled.WrapText, null, tint = IdeOnSurfaceVariant) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FileTabs(
    files: List<OpenFile>,
    activeIndex: Int,
    onSelect: (Int) -> Unit,
    onClose: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(IdeSurface)
            .drawBehind {
                drawLine(IdeBorder, Offset(0f, size.height), Offset(size.width, size.height), 1f)
            },
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        itemsIndexed(files) { index, file ->
            val isActive = index == activeIndex
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable { onSelect(index) }
                    .background(if (isActive) IdeBackground else Color.Transparent)
                    .drawBehind {
                        if (isActive) drawLine(
                            IdeAccent, Offset(0f, size.height - 2f),
                            Offset(size.width, size.height - 2f), 2f
                        )
                    }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = file.name + if (file.isModified) " ●" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) IdeOnBackground else IdeOnSurfaceVariant,
                    maxLines = 1
                )
                if (isActive) {
                    Icon(
                        Icons.Filled.Close, "Close",
                        tint = IdeOnSurfaceVariant,
                        modifier = Modifier.size(12.dp).clickable { onClose(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IdeSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Filled.Search, null, tint = IdeOnSurfaceVariant, modifier = Modifier.size(16.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(color = IdeOnBackground, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
            cursorBrush = SolidColor(IdeAccent),
            singleLine = true,
            decorationBox = { inner ->
                Box {
                    if (query.isEmpty()) Text("Search in file...", style = MaterialTheme.typography.bodySmall, color = IdeOnSurfaceVariant)
                    inner()
                }
            }
        )
        IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Filled.Close, "Close", tint = IdeOnSurfaceVariant, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun EditorBody(
    file: OpenFile,
    fontSize: Int,
    showLineNumbers: Boolean,
    wordWrap: Boolean,
    searchQuery: String,
    onContentChange: (String) -> Unit
) {
    var textFieldValue by remember(file.file.path) {
        mutableStateOf(TextFieldValue(file.content))
    }
    val highlightedText = remember(textFieldValue.text, file.extension) {
        SyntaxHighlighter.highlight(textFieldValue.text, file.extension)
    }
    val lines = remember(textFieldValue.text) { textFieldValue.text.lines() }
    val scrollState = rememberScrollState()
    val lineHeight = with(LocalDensity.current) { (fontSize * 1.5f).sp.toPx() }

    LaunchedEffect(file.file.path) {
        textFieldValue = TextFieldValue(file.content)
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (showLineNumbers) {
            Column(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .background(IdeSurface)
                    .verticalScroll(scrollState)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                lines.forEachIndexed { index, _ ->
                    Text(
                        text = "${index + 1}",
                        style = TextStyle(
                            fontSize = fontSize.sp,
                            fontFamily = FontFamily.Monospace,
                            color = IdeOnSurfaceVariant,
                            lineHeight = (fontSize * 1.5f).sp
                        ),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            BasicTextField(
                value = textFieldValue.copy(
                    annotatedString = applySearch(highlightedText, searchQuery)
                ),
                onValueChange = { newVal ->
                    textFieldValue = newVal
                    onContentChange(newVal.text)
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = fontSize.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SyntaxDefault,
                    lineHeight = (fontSize * 1.5f).sp
                ),
                cursorBrush = SolidColor(IdeAccent)
            )
        }
    }
}

private fun applySearch(annotated: AnnotatedString, query: String): AnnotatedString {
    if (query.isBlank()) return annotated
    return androidx.compose.ui.text.buildAnnotatedString {
        append(annotated)
        val text = annotated.text.lowercase()
        val q = query.lowercase()
        var start = 0
        while (true) {
            val idx = text.indexOf(q, start)
            if (idx < 0) break
            addStyle(
                SpanStyle(background = Color(0xFFFFFF00).copy(alpha = 0.3f), color = Color.White),
                idx, idx + q.length
            )
            start = idx + q.length
        }
    }
}

@Composable
private fun EditorWelcome() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("⚡", fontSize = 48.sp)
            Text("LocalIDE", style = MaterialTheme.typography.titleMedium, color = IdeAccent)
            Text("Open a file from the Files tab to start editing", style = MaterialTheme.typography.bodySmall, color = IdeOnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                FeatureChip("Syntax Highlighting")
                FeatureChip("Multi-Tab")
                FeatureChip("Search")
            }
        }
    }
}

@Composable
private fun FeatureChip(label: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = IdeAccent.copy(alpha = 0.15f)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall, color = IdeAccent)
    }
}
