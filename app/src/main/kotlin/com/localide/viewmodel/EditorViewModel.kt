package com.localide.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localide.model.OpenFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class EditorState(
    val openFiles: List<OpenFile> = emptyList(),
    val activeFileIndex: Int = 0,
    val isLoading: Boolean = false,
    val saveStatus: SaveStatus = SaveStatus.Idle,
    val searchQuery: String = "",
    val showSearch: Boolean = false,
    val fontSize: Int = 14,
    val showLineNumbers: Boolean = true,
    val wordWrap: Boolean = false
)

enum class SaveStatus { Idle, Saving, Saved, Error }

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    val activeFile: OpenFile?
        get() = _state.value.openFiles.getOrNull(_state.value.activeFileIndex)

    fun openFile(file: File) {
        viewModelScope.launch {
            val existing = _state.value.openFiles.indexOfFirst { it.file.path == file.path }
            if (existing >= 0) {
                _state.value = _state.value.copy(activeFileIndex = existing)
                return@launch
            }
            _state.value = _state.value.copy(isLoading = true)
            val content = withContext(Dispatchers.IO) {
                try { file.readText() } catch (e: Exception) { "" }
            }
            val openFile = OpenFile(file = file, content = content)
            val newList = _state.value.openFiles + openFile
            _state.value = _state.value.copy(
                openFiles = newList,
                activeFileIndex = newList.size - 1,
                isLoading = false
            )
        }
    }

    fun updateContent(content: String) {
        val idx = _state.value.activeFileIndex
        val files = _state.value.openFiles.toMutableList()
        if (idx >= 0 && idx < files.size) {
            files[idx] = files[idx].copy(content = content, isModified = true)
            _state.value = _state.value.copy(openFiles = files)
        }
    }

    fun saveCurrentFile() {
        val file = activeFile ?: return
        val savedIdx = _state.value.activeFileIndex
        viewModelScope.launch {
            _state.value = _state.value.copy(saveStatus = SaveStatus.Saving)
            val success = withContext(Dispatchers.IO) {
                try {
                    file.file.writeText(file.content)
                    true
                } catch (e: Exception) { false }
            }
            val files = _state.value.openFiles.toMutableList()
            if (success && savedIdx >= 0 && savedIdx < files.size) {
                files[savedIdx] = files[savedIdx].copy(isModified = false)
            }
            _state.value = _state.value.copy(
                openFiles = files,
                saveStatus = if (success) SaveStatus.Saved else SaveStatus.Error
            )
            kotlinx.coroutines.delay(2000)
            _state.value = _state.value.copy(saveStatus = SaveStatus.Idle)
        }
    }

    fun closeFile(index: Int) {
        val files = _state.value.openFiles.toMutableList()
        if (index < 0 || index >= files.size) return
        files.removeAt(index)
        val newIdx = when {
            files.isEmpty() -> 0
            index >= files.size -> files.size - 1
            else -> index
        }
        _state.value = _state.value.copy(openFiles = files, activeFileIndex = newIdx)
    }

    fun switchToFile(index: Int) {
        _state.value = _state.value.copy(activeFileIndex = index)
    }

    fun createNewFile(directory: File, name: String) {
        viewModelScope.launch {
            val file = File(directory, name)
            val created = withContext(Dispatchers.IO) {
                try { file.createNewFile() } catch (e: Exception) { false }
            }
            if (created) openFile(file)
        }
    }

    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun toggleSearch() {
        _state.value = _state.value.copy(showSearch = !_state.value.showSearch, searchQuery = "")
    }

    fun setFontSize(size: Int) {
        _state.value = _state.value.copy(fontSize = size.coerceIn(10, 24))
    }

    fun toggleLineNumbers() {
        _state.value = _state.value.copy(showLineNumbers = !_state.value.showLineNumbers)
    }

    fun toggleWordWrap() {
        _state.value = _state.value.copy(wordWrap = !_state.value.wordWrap)
    }
}
