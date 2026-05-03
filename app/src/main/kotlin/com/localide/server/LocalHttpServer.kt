package com.localide.server

import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

class LocalHttpServer(
    port: Int,
    private val rootDir: File,
    private val onRequest: ((method: String, uri: String, status: Int, ip: String) -> Unit)? = null
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri.trimStart('/')
        val file = File(rootDir, uri)
        val method = session.method.name
        val ip = session.remoteIpAddress ?: "unknown"

        return when {
            file.isDirectory -> {
                val indexFile = File(file, "index.html")
                if (indexFile.exists()) {
                    serveFile(indexFile, method, indexFile.absolutePath.removePrefix(rootDir.absolutePath), ip)
                } else {
                    serveDirectoryListing(file, uri, method, ip)
                }
            }
            file.exists() && file.isFile -> serveFile(file, method, uri, ip)
            else -> {
                onRequest?.invoke(method, "/$uri", 404, ip)
                newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    "text/html",
                    """<!DOCTYPE html><html><head><title>404 Not Found</title>
                    <style>body{font-family:monospace;background:#0d0d0d;color:#e8e8e8;
                    display:flex;align-items:center;justify-content:center;height:100vh;margin:0}
                    h1{color:#7c6af7}</style></head>
                    <body><div><h1>404</h1><p>/$uri not found</p></div></body></html>"""
                )
            }
        }
    }

    private fun serveFile(file: File, method: String, uri: String, ip: String): Response {
        val mimeType = getMimeTypeForFile(file.name)
        val status = 200
        onRequest?.invoke(method, "/$uri".replace("//", "/"), status, ip)
        return newChunkedResponse(
            Response.Status.OK,
            mimeType,
            FileInputStream(file)
        )
    }

    private fun serveDirectoryListing(dir: File, uri: String, method: String, ip: String): Response {
        val path = if (uri.isEmpty()) "/" else "/$uri"
        onRequest?.invoke(method, path, 200, ip)
        val files = dir.listFiles()?.sortedWith(
            compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }
        ) ?: emptyList()

        val rows = files.joinToString("\n") { f ->
            val href = if (uri.isEmpty()) f.name else "$uri/${f.name}"
            val icon = if (f.isDirectory) "📁" else getFileIcon(f.extension)
            val size = if (f.isFile) formatSize(f.length()) else "-"
            val modified = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(f.lastModified()))
            """<tr>
                <td>$icon <a href="/$href">${f.name}${if (f.isDirectory) "/" else ""}</a></td>
                <td>$size</td>
                <td>$modified</td>
            </tr>"""
        }

        val parentLink = if (uri.isNotEmpty()) {
            val parent = uri.substringBeforeLast("/", "")
            """<a href="/${parent}">← Parent Directory</a>"""
        } else ""

        val html = """<!DOCTYPE html>
<html>
<head>
  <title>LocalIDE Server — $path</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Courier New', monospace; background: #0d0d0d; color: #cdd3de;
           padding: 24px; min-height: 100vh; }
    h1 { color: #7c6af7; font-size: 1.2rem; margin-bottom: 4px; }
    .path { color: #888; font-size: 0.85rem; margin-bottom: 20px; }
    .parent { color: #4ec994; text-decoration: none; font-size: 0.9rem;
               display: block; margin-bottom: 16px; }
    .parent:hover { text-decoration: underline; }
    table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
    th { text-align: left; color: #888; font-weight: normal; padding: 6px 12px;
         border-bottom: 1px solid #2a2a2a; }
    td { padding: 8px 12px; border-bottom: 1px solid #1a1a1a; }
    a { color: #6eb1eb; text-decoration: none; }
    a:hover { text-decoration: underline; }
    tr:hover td { background: #1a1a1a; }
    .size { color: #888; }
    .date { color: #555; }
    .badge { background: #7c6af7; color: white; padding: 2px 8px;
             border-radius: 4px; font-size: 0.7rem; margin-left: 8px; }
  </style>
</head>
<body>
  <h1>⚡ LocalIDE <span class="badge">SERVER</span></h1>
  <div class="path">$path</div>
  $parentLink
  <table>
    <thead><tr><th>Name</th><th>Size</th><th>Modified</th></tr></thead>
    <tbody>$rows</tbody>
  </table>
</body>
</html>"""
        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
    }

    private fun getFileIcon(ext: String): String = when (ext.lowercase()) {
        "html", "htm" -> "🌐"
        "js", "ts" -> "📜"
        "css", "scss" -> "🎨"
        "json" -> "📋"
        "kt", "java" -> "☕"
        "py" -> "🐍"
        "md" -> "📝"
        "png", "jpg", "jpeg", "gif", "webp" -> "🖼️"
        "mp4", "mkv", "avi" -> "🎬"
        "mp3", "wav", "ogg" -> "🎵"
        "zip", "tar", "gz" -> "📦"
        "pdf" -> "📄"
        "sh" -> "⚙️"
        else -> "📄"
    }

    private fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }

    private fun getMimeTypeForFile(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            "ico" -> "image/x-icon"
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "mp3" -> "audio/mpeg"
            "ogg" -> "audio/ogg"
            "wav" -> "audio/wav"
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            "woff" -> "font/woff"
            "woff2" -> "font/woff2"
            "ttf" -> "font/ttf"
            "txt", "md", "kt", "java", "py", "sh", "yml", "yaml",
            "toml", "ini", "conf", "log" -> "text/plain"
            else -> "application/octet-stream"
        }
    }
}
