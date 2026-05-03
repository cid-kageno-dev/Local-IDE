package com.localide.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class TerminalLine(
    val text: String,
    val type: LineType = LineType.OUTPUT
)

enum class LineType { COMMAND, OUTPUT, ERROR, INFO, SUCCESS }

data class TerminalState(
    val lines: List<TerminalLine> = listOf(
        TerminalLine("LocalIDE Terminal v1.0", LineType.INFO),
        TerminalLine("Type 'help' for available commands", LineType.INFO),
        TerminalLine("", LineType.OUTPUT)
    ),
    val currentInput: String = "",
    val workingDir: File = File("/data/data/com.localide/files"),
    val isRunning: Boolean = false,
    val history: List<String> = emptyList(),
    val historyIndex: Int = -1,
    val fontSize: Int = 13
)

class TerminalViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(TerminalState(
        workingDir = application.filesDir
    ))
    val state: StateFlow<TerminalState> = _state.asStateFlow()

    private var currentProcess: Process? = null
    private var processJob: Job? = null

    fun setInput(input: String) {
        _state.value = _state.value.copy(currentInput = input, historyIndex = -1)
    }

    fun historyUp() {
        val history = _state.value.history
        if (history.isEmpty()) return
        val newIdx = (_state.value.historyIndex + 1).coerceAtMost(history.size - 1)
        _state.value = _state.value.copy(
            currentInput = history[history.size - 1 - newIdx],
            historyIndex = newIdx
        )
    }

    fun historyDown() {
        val idx = _state.value.historyIndex
        if (idx <= 0) {
            _state.value = _state.value.copy(currentInput = "", historyIndex = -1)
            return
        }
        val history = _state.value.history
        val newIdx = idx - 1
        _state.value = _state.value.copy(
            currentInput = history[history.size - 1 - newIdx],
            historyIndex = newIdx
        )
    }

    fun executeCommand(command: String) {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) return

        val history = (_state.value.history + trimmed).takeLast(100)
        appendLine(TerminalLine("${_state.value.workingDir.path} $ $trimmed", LineType.COMMAND))
        _state.value = _state.value.copy(
            currentInput = "",
            history = history,
            historyIndex = -1
        )

        when {
            trimmed == "clear" || trimmed == "cls" -> {
                _state.value = _state.value.copy(lines = emptyList())
                return
            }
            trimmed == "help" -> {
                showHelp()
                return
            }
            trimmed.startsWith("cd ") -> {
                changeDir(trimmed.removePrefix("cd ").trim())
                return
            }
            trimmed == "cd" -> {
                _state.value = _state.value.copy(
                    workingDir = File("/data/data/com.localide/files")
                )
                return
            }
            trimmed == "pwd" -> {
                appendLine(TerminalLine(_state.value.workingDir.absolutePath, LineType.OUTPUT))
                return
            }
            trimmed == "exit" || trimmed == "quit" -> {
                appendLine(TerminalLine("Session terminated.", LineType.INFO))
                return
            }
        }

        runShellCommand(trimmed)
    }

    private fun changeDir(path: String) {
        val newDir = when {
            path.startsWith("/") -> File(path)
            path == ".." -> _state.value.workingDir.parentFile ?: _state.value.workingDir
            path == "~" -> File("/data/data/com.localide/files")
            else -> File(_state.value.workingDir, path)
        }
        when {
            !newDir.exists() -> appendLine(TerminalLine("cd: $path: No such file or directory", LineType.ERROR))
            !newDir.isDirectory -> appendLine(TerminalLine("cd: $path: Not a directory", LineType.ERROR))
            else -> _state.value = _state.value.copy(workingDir = newDir)
        }
    }

    private fun runShellCommand(command: String) {
        if (_state.value.isRunning) {
            appendLine(TerminalLine("A command is already running. Use Ctrl+C to stop.", LineType.ERROR))
            return
        }
        _state.value = _state.value.copy(isRunning = true)
        processJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val process = ProcessBuilder("sh", "-c", command)
                    .directory(_state.value.workingDir)
                    .redirectErrorStream(false)
                    .start()
                currentProcess = process

                val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
                val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

                val stdoutJob = launch {
                    stdoutReader.lineSequence().forEach { line ->
                        appendLine(TerminalLine(line, LineType.OUTPUT))
                    }
                }
                val stderrJob = launch {
                    stderrReader.lineSequence().forEach { line ->
                        appendLine(TerminalLine(line, LineType.ERROR))
                    }
                }

                stdoutJob.join()
                stderrJob.join()
                val exitCode = process.waitFor()

                withContext(Dispatchers.Main) {
                    if (exitCode != 0) {
                        appendLine(TerminalLine("Process exited with code $exitCode", LineType.INFO))
                    }
                    _state.value = _state.value.copy(isRunning = false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    appendLine(TerminalLine("Error: ${e.message}", LineType.ERROR))
                    _state.value = _state.value.copy(isRunning = false)
                }
            }
        }
    }

    fun killProcess() {
        currentProcess?.destroyForcibly()
        processJob?.cancel()
        _state.value = _state.value.copy(isRunning = false)
        appendLine(TerminalLine("^C", LineType.INFO))
    }

    fun clear() {
        _state.value = _state.value.copy(lines = emptyList())
    }

    private fun showHelp() {
        val help = listOf(
            "Available commands:",
            "  cd <dir>       Change directory",
            "  pwd            Print working directory",
            "  ls             List files",
            "  cat <file>     Print file contents",
            "  echo <text>    Print text",
            "  mkdir <name>   Create directory",
            "  rm <file>      Remove file",
            "  cp <src> <dst> Copy file",
            "  mv <src> <dst> Move/rename file",
            "  touch <file>   Create empty file",
            "  chmod <mode>   Change permissions",
            "  grep <pat>     Search in files",
            "  find <dir>     Find files",
            "  ps             List processes",
            "  kill <pid>     Kill process",
            "  clear/cls      Clear terminal",
            "  exit/quit      Exit terminal",
            "  help           Show this help"
        )
        help.forEach { appendLine(TerminalLine(it, LineType.INFO)) }
    }

    private fun appendLine(line: TerminalLine) {
        val current = _state.value.lines
        _state.value = _state.value.copy(lines = (current + line).takeLast(2000))
    }
}
