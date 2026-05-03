package com.localide.model

import java.io.File

data class FileItem(
    val file: File,
    val depth: Int = 0,
    val isExpanded: Boolean = false
) {
    val name: String get() = file.name
    val path: String get() = file.absolutePath
    val isDirectory: Boolean get() = file.isDirectory
    val extension: String get() = file.extension.lowercase()
    val size: Long get() = if (file.isFile) file.length() else 0L

    fun formattedSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }
}

data class OpenFile(
    val file: File,
    val content: String,
    val isModified: Boolean = false,
    val cursorPosition: Int = 0
) {
    val name: String get() = file.name
    val extension: String get() = file.extension.lowercase()
}
