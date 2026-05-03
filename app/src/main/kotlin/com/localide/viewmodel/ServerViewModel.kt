package com.localide.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localide.server.LocalHttpServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface

data class ServerLogEntry(
    val time: String,
    val method: String,
    val uri: String,
    val status: Int,
    val ip: String
)

data class ServerState(
    val isRunning: Boolean = false,
    val port: Int = 8080,
    val servingDir: File? = null,
    val localIp: String = "",
    val logs: List<ServerLogEntry> = emptyList(),
    val error: String? = null,
    val requestCount: Int = 0,
    val portInput: String = "8080"
)

class ServerViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ServerState(
        servingDir = application.filesDir,
        localIp = getLocalIpAddress()
    ))
    val state: StateFlow<ServerState> = _state.asStateFlow()

    private var server: LocalHttpServer? = null

    fun startServer() {
        val dir = _state.value.servingDir ?: return
        val port = _state.value.port

        viewModelScope.launch {
            try {
                server = LocalHttpServer(
                    port = port,
                    rootDir = dir,
                    onRequest = { method, uri, status, ip ->
                        val time = java.text.SimpleDateFormat(
                            "HH:mm:ss", java.util.Locale.getDefault()
                        ).format(java.util.Date())
                        val entry = ServerLogEntry(time, method, uri, status, ip)
                        _state.value = _state.value.copy(
                            logs = (_state.value.logs + entry).takeLast(200),
                            requestCount = _state.value.requestCount + 1
                        )
                    }
                )
                server!!.start()
                _state.value = _state.value.copy(
                    isRunning = true,
                    error = null,
                    localIp = getLocalIpAddress()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRunning = false,
                    error = "Failed to start server: ${e.message}"
                )
            }
        }
    }

    fun stopServer() {
        server?.stop()
        server = null
        _state.value = _state.value.copy(isRunning = false)
    }

    fun setPort(portStr: String) {
        val port = portStr.toIntOrNull()
        _state.value = _state.value.copy(
            portInput = portStr,
            port = port ?: _state.value.port
        )
    }

    fun setServingDir(dir: File) {
        _state.value = _state.value.copy(servingDir = dir)
    }

    fun clearLogs() {
        _state.value = _state.value.copy(logs = emptyList(), requestCount = 0)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        stopServer()
    }

    private companion object {
        fun getLocalIpAddress(): String {
            return try {
                NetworkInterface.getNetworkInterfaces()?.toList()
                    ?.flatMap { it.inetAddresses.toList() }
                    ?.firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
                    ?.hostAddress ?: "127.0.0.1"
            } catch (e: Exception) {
                "127.0.0.1"
            }
        }
    }
}
