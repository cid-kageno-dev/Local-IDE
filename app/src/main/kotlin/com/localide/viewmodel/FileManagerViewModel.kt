package com.localide.viewmodel

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localide.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class FileManagerState(
    val currentDir: File = Environment.getExternalStorageDirectory()
        ?: File("/sdcard"),
    val items: List<FileItem> = emptyList(),
    val selectedFile: File? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showHidden: Boolean = false,
    val sortBy: SortBy = SortBy.NAME,
    val history: List<File> = emptyList(),
    val showNewFileDialog: Boolean = false,
    val showNewFolderDialog: Boolean = false,
    val showRenameDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val contextMenuFile: File? = null
)

enum class SortBy { NAME, SIZE, DATE, TYPE }

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(FileManagerState())
    val state: StateFlow<FileManagerState> = _state.asStateFlow()

    private val homeDir: File = application.filesDir

    init {
        navigateTo(homeDir)
    }

    fun navigateTo(dir: File) {
        if (!dir.exists() || !dir.isDirectory) return
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                loadDirectory(dir)
            }
            val history = if (dir == _state.value.currentDir) _state.value.history
            else _state.value.history + _state.value.currentDir
            _state.value = _state.value.copy(
                currentDir = dir,
                items = items,
                isLoading = false,
                history = history
            )
        }
    }

    fun navigateUp() {
        val history = _state.value.history
        if (history.isNotEmpty()) {
            val prev = history.last()
            _state.value = _state.value.copy(history = history.dropLast(1))
            navigateTo(prev)
        } else {
            _state.value.currentDir.parentFile?.let { navigateTo(it) }
        }
    }

    fun navigateHome() = navigateTo(homeDir)

    fun refresh() = navigateTo(_state.value.currentDir)

    fun createFile(name: String): Boolean {
        val file = File(_state.value.currentDir, name)
        return try {
            file.createNewFile()
            refresh()
            true
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to create file: ${e.message}")
            false
        }
    }

    fun createFolder(name: String): Boolean {
        val dir = File(_state.value.currentDir, name)
        return try {
            dir.mkdir()
            refresh()
            true
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to create folder: ${e.message}")
            false
        }
    }

    fun deleteFile(file: File): Boolean {
        return try {
            if (file.isDirectory) file.deleteRecursively() else file.delete()
            refresh()
            true
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to delete: ${e.message}")
            false
        }
    }

    fun renameFile(file: File, newName: String): Boolean {
        val newFile = File(file.parent, newName)
        return try {
            file.renameTo(newFile)
            refresh()
            true
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to rename: ${e.message}")
            false
        }
    }

    fun toggleHidden() {
        _state.value = _state.value.copy(showHidden = !_state.value.showHidden)
        refresh()
    }

    fun setSortBy(sort: SortBy) {
        _state.value = _state.value.copy(sortBy = sort)
        refresh()
    }

    fun setContextMenu(file: File?) {
        _state.value = _state.value.copy(contextMenuFile = file)
    }

    fun showNewFileDialog(show: Boolean) {
        _state.value = _state.value.copy(showNewFileDialog = show)
    }

    fun showNewFolderDialog(show: Boolean) {
        _state.value = _state.value.copy(showNewFolderDialog = show)
    }

    fun showRenameDialog(show: Boolean) {
        _state.value = _state.value.copy(showRenameDialog = show)
    }

    fun showDeleteConfirm(show: Boolean) {
        _state.value = _state.value.copy(showDeleteConfirm = show)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun loadDirectory(dir: File): List<FileItem> {
        val files = dir.listFiles() ?: return emptyList()
        val showHidden = _state.value.showHidden
        val sortBy = _state.value.sortBy
        return files
            .filter { showHidden || !it.name.startsWith(".") }
            .sortedWith(compareBy<File> { !it.isDirectory }.then(
                when (sortBy) {
                    SortBy.NAME -> compareBy { it.name.lowercase() }
                    SortBy.SIZE -> compareBy { it.length() }
                    SortBy.DATE -> compareByDescending { it.lastModified() }
                    SortBy.TYPE -> compareBy { it.extension }
                }
            ))
            .map { FileItem(it) }
    }
}
