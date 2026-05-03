package com.localide.util

import java.io.File

object FileUtils {

    fun isTextFile(file: File): Boolean {
        val textExtensions = setOf(
            "kt", "kts", "java", "js", "jsx", "ts", "tsx", "py",
            "html", "htm", "css", "scss", "sass", "less",
            "xml", "json", "yaml", "yml", "toml", "ini", "conf",
            "sh", "bash", "zsh", "fish", "cmd", "bat",
            "md", "txt", "log", "csv", "sql",
            "c", "cpp", "h", "hpp", "cs", "go", "rb", "rs",
            "php", "swift", "dart", "r", "lua", "pl", "scala",
            "gradle", "properties", "gitignore", "dockerfile"
        )
        return file.extension.lowercase() in textExtensions
    }

    fun isBinaryFile(file: File): Boolean = !isTextFile(file)

    fun getLanguageFromExtension(ext: String): String = when (ext.lowercase()) {
        "kt", "kts" -> "Kotlin"
        "java" -> "Java"
        "js" -> "JavaScript"
        "jsx" -> "JSX"
        "ts" -> "TypeScript"
        "tsx" -> "TSX"
        "py" -> "Python"
        "html", "htm" -> "HTML"
        "css" -> "CSS"
        "scss", "sass" -> "SCSS"
        "json" -> "JSON"
        "xml" -> "XML"
        "yaml", "yml" -> "YAML"
        "sh", "bash", "zsh" -> "Shell"
        "md" -> "Markdown"
        "c" -> "C"
        "cpp", "hpp" -> "C++"
        "cs" -> "C#"
        "go" -> "Go"
        "rs" -> "Rust"
        "rb" -> "Ruby"
        "php" -> "PHP"
        "swift" -> "Swift"
        "dart" -> "Dart"
        "sql" -> "SQL"
        "gradle" -> "Gradle"
        else -> ext.uppercase().ifEmpty { "Plain Text" }
    }

    fun formatFileSize(bytes: Long): String = when {
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024L * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024))
    }

    fun countLines(content: String): Int = content.lines().size

    fun getIndent(line: String): Int {
        var count = 0
        for (ch in line) {
            when (ch) {
                ' ' -> count++
                '\t' -> count += 4
                else -> break
            }
        }
        return count
    }

    fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[/\\\\:*?\"<>|]"), "_").trim()
    }

    fun ensureUniqueFile(dir: File, name: String): File {
        var file = File(dir, name)
        if (!file.exists()) return file
        val baseName = name.substringBeforeLast('.')
        val ext = name.substringAfterLast('.', "")
        var counter = 1
        while (file.exists()) {
            file = File(dir, if (ext.isEmpty()) "${baseName}_$counter" else "${baseName}_$counter.$ext")
            counter++
        }
        return file
    }
}
