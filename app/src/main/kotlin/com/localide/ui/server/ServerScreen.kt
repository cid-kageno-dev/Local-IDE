package com.localide.ui.server

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localide.ui.theme.*
import com.localide.viewmodel.ServerLogEntry
import com.localide.viewmodel.ServerViewModel

@Composable
fun ServerScreen(vm: ServerViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(state.logs.size) {
        if (state.logs.isNotEmpty()) listState.animateScrollToItem(state.logs.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeBackground)
    ) {
        ServerToolbar(
            isRunning = state.isRunning,
            onStart = vm::startServer,
            onStop = vm::stopServer,
            onClearLogs = vm::clearLogs
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeSurface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (state.isRunning) IdeGreen else IdeOnSurfaceVariant,
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
                Text(
                    if (state.isRunning) "Server Running" else "Server Stopped",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (state.isRunning) IdeGreen else IdeOnSurfaceVariant
                )
            }

            // Server URL (when running)
            if (state.isRunning) {
                val url = "http://${state.localIp}:${state.port}"
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = IdeAccent.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, IdeAccent.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Local URL", style = MaterialTheme.typography.bodySmall, color = IdeOnSurfaceVariant)
                            Text(url, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), color = IdeAccentLight)
                        }
                        IconButton(onClick = { clipboard.setText(AnnotatedString(url)) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.ContentCopy, "Copy URL", tint = IdeAccent, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Config row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Port input
                Column(modifier = Modifier.weight(0.4f)) {
                    Text("Port", style = MaterialTheme.typography.bodySmall, color = IdeOnSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = state.portInput,
                        onValueChange = { vm.setPort(it) },
                        enabled = !state.isRunning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IdeSurfaceVariant, MaterialTheme.shapes.small)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        textStyle = TextStyle(
                            color = IdeOnBackground,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush = SolidColor(IdeAccent),
                        singleLine = true
                    )
                }

                // Serving dir
                Column(modifier = Modifier.weight(0.6f)) {
                    Text("Serving Directory", style = MaterialTheme.typography.bodySmall, color = IdeOnSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IdeSurfaceVariant, MaterialTheme.shapes.small)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            state.servingDir?.name ?: "Not set",
                            style = TextStyle(color = IdeOnBackground, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Stats row (when running)
            if (state.isRunning) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatChip("${state.requestCount}", "Requests", IdeAccent)
                    StatChip(state.localIp, "IP", IdeCyan)
                    StatChip("${state.port}", "Port", IdeGreen)
                }
            }
        }

        HorizontalDivider(color = IdeBorder)

        // Request Log Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeSurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("REQUEST LOG", style = MaterialTheme.typography.titleSmall, color = IdeOnSurfaceVariant)
            if (state.logs.isNotEmpty()) {
                Text("${state.logs.size} entries", style = MaterialTheme.typography.bodySmall, color = IdeOnSurfaceVariant)
            }
        }

        if (state.logs.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📡", fontSize = 32.sp)
                    Text(
                        if (state.isRunning) "Waiting for requests..." else "Start the server to see requests",
                        style = MaterialTheme.typography.bodySmall, color = IdeOnSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.logs) { log ->
                    LogEntryRow(log)
                }
            }
        }

        if (state.error != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IdeRed.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(state.error!!, style = MaterialTheme.typography.bodySmall, color = IdeRed, modifier = Modifier.weight(1f))
                IconButton(onClick = vm::clearError, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, "Dismiss", tint = IdeRed, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun ServerToolbar(
    isRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onClearLogs: () -> Unit
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
        Text("LOCAL SERVER", style = MaterialTheme.typography.titleSmall, color = IdeOnSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onClearLogs, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Clear, "Clear Logs", tint = IdeOnSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            if (!isRunning) {
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = IdeGreen),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Start", style = MaterialTheme.typography.titleSmall)
                }
            } else {
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(containerColor = IdeRed),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Filled.Stop, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Stop", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
private fun LogEntryRow(log: ServerLogEntry) {
    val statusColor = when {
        log.status >= 500 -> IdeRed
        log.status >= 400 -> IdeYellow
        log.status >= 300 -> IdeCyan
        else -> IdeGreen
    }
    val methodColor = when (log.method) {
        "GET" -> IdeGreen
        "POST" -> IdeAccent
        "PUT" -> IdeYellow
        "DELETE" -> IdeRed
        else -> IdeOnSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(IdeBorder, Offset(0f, size.height - 0.5f), Offset(size.width, size.height - 0.5f), 0.5f)
            }
            .padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(log.time, style = TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = IdeOnSurfaceVariant), modifier = Modifier.width(52.dp))
        Text(
            log.status.toString(),
            style = TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = statusColor),
            modifier = Modifier.width(28.dp)
        )
        Text(
            log.method,
            style = TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = methodColor),
            modifier = Modifier.width(36.dp)
        )
        Text(
            log.uri,
            style = TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = IdeOnBackground),
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(log.ip, style = TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = IdeOnSurfaceVariant))
    }
}

@Composable
private fun StatChip(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace), color = color)
            Text(label, style = MaterialTheme.typography.bodySmall, color = color.copy(alpha = 0.7f))
        }
    }
}
