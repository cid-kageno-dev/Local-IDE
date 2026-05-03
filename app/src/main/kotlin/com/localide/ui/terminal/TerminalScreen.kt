package com.localide.ui.terminal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localide.ui.theme.*
import com.localide.viewmodel.LineType
import com.localide.viewmodel.TerminalLine
import com.localide.viewmodel.TerminalViewModel

@Composable
fun TerminalScreen(vm: TerminalViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    var input by remember { mutableStateOf("") }

    LaunchedEffect(state.lines.size) {
        if (state.lines.isNotEmpty()) {
            listState.animateScrollToItem(state.lines.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeBackground)
    ) {
        TerminalToolbar(
            isRunning = state.isRunning,
            workingDir = state.workingDir.path,
            onKill = vm::killProcess,
            onClear = vm::clear
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            state = listState
        ) {
            items(state.lines) { line ->
                TerminalLineText(line = line, fontSize = state.fontSize)
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    PromptText(dir = state.workingDir.name, fontSize = state.fontSize)
                    BasicTextField(
                        value = input,
                        onValueChange = {
                            input = it
                            vm.setInput(it)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        textStyle = TextStyle(
                            fontSize = state.fontSize.sp,
                            fontFamily = FontFamily.Monospace,
                            color = IdeOnBackground,
                            lineHeight = (state.fontSize * 1.5f).sp
                        ),
                        cursorBrush = SolidColor(IdeGreen),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Send
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSend = {
                                val cmd = input
                                input = ""
                                vm.executeCommand(cmd)
                            }
                        )
                    )
                }
            }
            item { Spacer(Modifier.height(60.dp)) }
        }

        TerminalInputBar(
            input = input,
            isRunning = state.isRunning,
            onInputChange = { input = it },
            onExecute = {
                val cmd = input
                input = ""
                vm.executeCommand(cmd)
            },
            onHistoryUp = vm::historyUp,
            onHistoryDown = vm::historyDown,
            onKill = vm::killProcess,
            fontSize = state.fontSize
        )
    }
}

@Composable
private fun TerminalToolbar(
    isRunning: Boolean,
    workingDir: String,
    onKill: () -> Unit,
    onClear: () -> Unit
) {
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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("TERMINAL", style = MaterialTheme.typography.titleSmall, color = IdeOnSurfaceVariant)
            if (isRunning) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = IdeGreen.copy(alpha = 0.2f)
                ) {
                    Text("● RUNNING", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.bodySmall, color = IdeGreen)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (isRunning) {
                IconButton(onClick = onKill, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Stop, "Kill", tint = IdeRed, modifier = Modifier.size(18.dp))
                }
            }
            IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Clear, "Clear", tint = IdeOnSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun TerminalLineText(line: TerminalLine, fontSize: Int) {
    val color = when (line.type) {
        LineType.COMMAND -> IdeAccentLight
        LineType.ERROR -> IdeRed
        LineType.INFO -> IdeYellow
        LineType.SUCCESS -> IdeGreen
        LineType.OUTPUT -> IdeOnBackground
    }
    Text(
        text = line.text,
        style = TextStyle(
            fontSize = fontSize.sp,
            fontFamily = FontFamily.Monospace,
            color = color,
            lineHeight = (fontSize * 1.5f).sp
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PromptText(dir: String, fontSize: Int) {
    Row {
        Text("~/$dir", style = TextStyle(fontSize = fontSize.sp, fontFamily = FontFamily.Monospace, color = IdeGreen))
        Text(" $ ", style = TextStyle(fontSize = fontSize.sp, fontFamily = FontFamily.Monospace, color = IdeAccent))
    }
}

@Composable
private fun TerminalInputBar(
    input: String,
    isRunning: Boolean,
    onInputChange: (String) -> Unit,
    onExecute: () -> Unit,
    onHistoryUp: () -> Unit,
    onHistoryDown: () -> Unit,
    onKill: () -> Unit,
    fontSize: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IdeSurface)
            .drawBehind {
                drawLine(IdeBorder, Offset(0f, 0f), Offset(size.width, 0f), 1f)
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f)
                .background(IdeSurfaceVariant, MaterialTheme.shapes.small)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("$", style = TextStyle(fontSize = fontSize.sp, fontFamily = FontFamily.Monospace, color = IdeGreen))
            BasicTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = fontSize.sp,
                    fontFamily = FontFamily.Monospace,
                    color = IdeOnBackground
                ),
                cursorBrush = SolidColor(IdeGreen),
                singleLine = true,
                decorationBox = { inner ->
                    Box {
                        if (input.isEmpty()) Text("Enter command...",
                            style = TextStyle(fontSize = fontSize.sp, fontFamily = FontFamily.Monospace, color = IdeOnSurfaceVariant))
                        inner()
                    }
                }
            )
        }
        IconButton(onClick = onHistoryUp, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.KeyboardArrowUp, "History Up", tint = IdeOnSurfaceVariant, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onHistoryDown, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.KeyboardArrowDown, "History Down", tint = IdeOnSurfaceVariant, modifier = Modifier.size(18.dp))
        }
        if (isRunning) {
            IconButton(onClick = onKill, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Cancel, "^C", tint = IdeRed, modifier = Modifier.size(18.dp))
            }
        } else {
            IconButton(
                onClick = onExecute,
                enabled = input.isNotBlank(),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, "Run", tint = if (input.isNotBlank()) IdeGreen else IdeOnSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}
