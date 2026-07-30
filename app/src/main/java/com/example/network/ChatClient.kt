package com.example.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.net.SocketTimeoutException

enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

class ChatClient {

    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    var onMessageReceived: ((String) -> Unit)? = null
    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun connect(host: String, port: Int, userName: String) {
        if (_connectionState.value == ConnectionState.CONNECTING) return

        _connectionState.value = ConnectionState.CONNECTING

        scope.launch {
            try {
                socket = Socket(host, port).apply {
                    soTimeout = 0
                }
                val inputStream = socket!!.getInputStream()
                writer = PrintWriter(socket!!.getOutputStream(), true)

                writer!!.println(userName)

                _connectionState.value = ConnectionState.CONNECTED
                onConnected?.invoke()

                val buffer = ByteArray(4096)
                val sb = StringBuilder()

                while (socket != null && !socket!!.isClosed) {
                    val bytesRead = try {
                        inputStream.read(buffer)
                    } catch (e: Exception) {
                        break
                    }
                    if (bytesRead <= 0) break

                    val chunk = String(buffer, 0, bytesRead, Charsets.UTF_8)
                    sb.append(chunk)

                    while (sb.contains("\n") || sb.contains("\r")) {
                        val newlineIdx = sb.indexOfAny(charArrayOf('\n', '\r'))
                        val line = sb.substring(0, newlineIdx).trim()
                        if (line.isNotEmpty()) {
                            onMessageReceived?.invoke(line)
                        }
                        var nextIdx = newlineIdx
                        while (nextIdx < sb.length && (sb[nextIdx] == '\n' || sb[nextIdx] == '\r')) {
                            nextIdx++
                        }
                        sb.delete(0, nextIdx)
                    }

                    if (sb.isNotEmpty()) {
                        val remaining = sb.toString().trim()
                        if (remaining.isNotEmpty()) {
                            onMessageReceived?.invoke(remaining)
                            sb.clear()
                        }
                    }
                }
            } catch (e: Exception) {
                if (_connectionState.value != ConnectionState.DISCONNECTED) {
                    _connectionState.value = ConnectionState.ERROR
                    onError?.invoke(e.message ?: "Koneksi gagal")
                }
            } finally {
                _connectionState.value = ConnectionState.DISCONNECTED
                onDisconnected?.invoke()
            }
        }
    }

    fun sendMessage(message: String) {
        if (_connectionState.value != ConnectionState.CONNECTED) return
        scope.launch {
            try {
                writer?.println(message)
            } catch (e: Exception) {
                onError?.invoke("Gagal mengirim pesan: ${e.message}")
            }
        }
    }

    fun disconnect() {
        try {
            socket?.close()
            reader = null
            writer = null
        } catch (_: Exception) {}
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun destroy() {
        disconnect()
        scope.cancel()
    }
}
