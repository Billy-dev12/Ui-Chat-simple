package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatDatabase
import com.example.data.toEntity
import com.example.data.toMessage
import com.example.model.ChatThread
import com.example.model.ContactItem
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.MessageType
import com.example.model.User
import com.example.network.ChatClient
import com.example.network.ConnectionState
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class Screen {
    object WelcomeName : Screen()
    object Connect : Screen()
    object ChatList : Screen()
    data class ChatDetail(val chatId: String) : Screen()
    object Contacts : Screen()
    object Settings : Screen()
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ChatDatabase.getDatabase(application)
    private val messageDao = db.messageDao()
    private val threadDao = db.chatThreadDao()
    private val chatClient = ChatClient()

    private val _currentUser = MutableStateFlow(
        User(
            id = "user_me",
            name = "Setyo Pratama",
            username = "@setyop",
            avatarInitials = "SP",
            avatarColorHex = 0xFF10B981,
            isOnline = true,
            bio = "Menggunakan AuraChat"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.WelcomeName)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.DARK_SLATE)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Semua")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _chatThreads = MutableStateFlow<List<ChatThread>>(emptyList())
    val chatThreads: StateFlow<List<ChatThread>> = _chatThreads.asStateFlow()

    private val _messagesMap = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messagesMap: StateFlow<Map<String, List<Message>>> = _messagesMap.asStateFlow()

    private val _onlineUsers = MutableStateFlow<List<String>>(emptyList())
    val onlineUsers: StateFlow<List<String>> = _onlineUsers.asStateFlow()

    private val _currentlyPlayingAudioId = MutableStateFlow<String?>(null)
    val currentlyPlayingAudioId: StateFlow<String?> = _currentlyPlayingAudioId.asStateFlow()

    private val _replyingToMessage = MutableStateFlow<Message?>(null)
    val replyingToMessage: StateFlow<Message?> = _replyingToMessage.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    private val _connectionMessage = MutableStateFlow<String?>(null)
    val connectionMessage: StateFlow<String?> = _connectionMessage.asStateFlow()

    private val _savedIp = MutableStateFlow<String?>(null)
    val savedIp: StateFlow<String?> = _savedIp.asStateFlow()

    private val _savedPort = MutableStateFlow("9090")
    val savedPort: StateFlow<String> = _savedPort.asStateFlow()

    private val colorPalette = listOf(
        0xFFEC4899L, 0xFF3B82F6L, 0xFFF59E0BL,
        0xFF10B981L, 0xFF8B5CF6L, 0xFF06B6D4L,
        0xFFEF4444L, 0xFF6366F1L
    )

    init {
        checkSavedUserAndSetupClient()
    }

    private fun checkSavedUserAndSetupClient() {
        val prefs = getApplication<Application>().getSharedPreferences("aurachat_prefs", Context.MODE_PRIVATE)
        val savedName = prefs.getString("user_name", null)
        val isFirstRun = prefs.getBoolean("is_first_run", true)
        val ip = prefs.getString("server_ip", null)
        val port = prefs.getString("server_port", "9090") ?: "9090"

        _savedIp.value = ip
        _savedPort.value = port

        setupChatClientCallbacks()

        if (isFirstRun || savedName.isNullOrBlank()) {
            _currentScreen.value = Screen.WelcomeName
        } else {
            val initials = getInitials(savedName)
            val handle = "@" + savedName.lowercase().replace("\\s+".toRegex(), "")
            _currentUser.value = _currentUser.value.copy(
                name = savedName,
                username = handle,
                avatarInitials = initials
            )

            if (!ip.isNullOrBlank()) {
                // Pre-navigate to ChatList and auto-connect to stored IP/Port
                _currentScreen.value = Screen.ChatList
                connectToServer(ip, port, savedName)
            } else {
                _currentScreen.value = Screen.Connect
            }
        }
    }

    private fun setupChatClientCallbacks() {
        chatClient.onConnected = {
            _connectionState.value = ConnectionState.CONNECTED
            _connectionMessage.value = "Terkoneksi ke server!"
            _connectionError.value = null
        }

        chatClient.onDisconnected = {
            if (_connectionState.value != ConnectionState.DISCONNECTED) {
                _connectionState.value = ConnectionState.DISCONNECTED
                _connectionMessage.value = "Koneksi terputus"
            }
        }

        chatClient.onError = { error ->
            _connectionError.value = error
            _connectionState.value = ConnectionState.ERROR
        }

        chatClient.onMessageReceived = { rawMessage ->
            parseServerMessage(rawMessage)
        }
    }

    fun saveAndSetUserName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return

        val initials = getInitials(trimmed)
        val handle = "@" + trimmed.lowercase().replace("\\s+".toRegex(), "")

        val updatedUser = _currentUser.value.copy(
            name = trimmed,
            username = handle,
            avatarInitials = initials
        )
        _currentUser.value = updatedUser

        val prefs = getApplication<Application>().getSharedPreferences("aurachat_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("user_name", trimmed)
            .putBoolean("is_first_run", false)
            .apply()

        if (_currentScreen.value is Screen.WelcomeName) {
            val ip = _savedIp.value
            if (!ip.isNullOrBlank()) {
                _currentScreen.value = Screen.ChatList
                connectToServer(ip, _savedPort.value, trimmed)
            } else {
                _currentScreen.value = Screen.Connect
            }
        }
    }

    fun connectToServer(ip: String, port: String, name: String) {
        val trimmedIp = ip.trim()
        val trimmedPort = port.trim()
        val trimmedName = name.trim()

        if (trimmedName.isNotBlank() && trimmedName != _currentUser.value.name) {
            val initials = getInitials(trimmedName)
            val handle = "@" + trimmedName.lowercase().replace("\\s+".toRegex(), "")
            _currentUser.value = _currentUser.value.copy(name = trimmedName, username = handle, avatarInitials = initials)

            val prefs = getApplication<Application>().getSharedPreferences("aurachat_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("user_name", trimmedName).apply()
        }

        _savedIp.value = trimmedIp
        _savedPort.value = trimmedPort

        val prefs = getApplication<Application>().getSharedPreferences("aurachat_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("server_ip", trimmedIp)
            .putString("server_port", trimmedPort)
            .apply()

        _connectionState.value = ConnectionState.CONNECTING
        _connectionError.value = null
        chatClient.connect(trimmedIp, trimmedPort.toIntOrNull() ?: 9090, _currentUser.value.name)
    }

    fun retryConnection() {
        val ip = _savedIp.value
        val port = _savedPort.value
        val name = _currentUser.value.name
        if (!ip.isNullOrBlank()) {
            connectToServer(ip, port, name)
        } else {
            _currentScreen.value = Screen.Connect
        }
    }

    fun disconnectFromServer() {
        chatClient.disconnect()
        _connectionState.value = ConnectionState.DISCONNECTED
        _onlineUsers.value = emptyList()
    }

    private fun parseServerMessage(raw: String) {
        val trimmed = raw.trim()
        when {
            // [Sistem] User yang online: Billy, Rian, Nadia
            trimmed.contains("User yang online:") -> {
                val namesText = trimmed.substringAfter("User yang online:").trim()
                if (namesText.isNotBlank()) {
                    val names = namesText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    _onlineUsers.value = names
                    loadOnlineUsersAsContacts(names)
                } else {
                    _onlineUsers.value = emptyList()
                    loadOnlineUsersAsContacts(emptyList())
                }
            }

            // [Sistem] Nama bergabung.
            trimmed.contains("bergabung") -> {
                val name = trimmed.substringAfter("[Sistem]").substringBefore("bergabung").trim()
                if (name.isNotBlank() && name !in _onlineUsers.value) {
                    _onlineUsers.value = _onlineUsers.value + name
                    loadOnlineUsersAsContacts(_onlineUsers.value)
                }
                // Minta update daftar online terbaru
                queryOnlineUsers()
            }

            // [Sistem] Nama keluar.
            trimmed.contains("keluar") -> {
                val name = trimmed.substringAfter("[Sistem]").substringBefore("keluar").trim()
                if (name.isNotBlank()) {
                    _onlineUsers.value = _onlineUsers.value.filter { it != name }
                    loadOnlineUsersAsContacts(_onlineUsers.value)
                }
                queryOnlineUsers()
            }

            // [Private dari Nama]: pesan
            trimmed.startsWith("[Private dari ") -> {
                val senderName = trimmed.substringAfter("[Private dari ").substringBefore("]")
                val text = trimmed.substringAfter("]: ").trim()
                handleIncomingMessage(senderName, text, isPrivate = true)
            }

            // [Nama]: pesan (broadcast)
            trimmed.startsWith("[") && trimmed.contains("]:") -> {
                val senderName = trimmed.substringAfter("[").substringBefore("]")
                if (senderName == "Sistem") {
                    // Pesan sistem lainnya
                    val systemMsg = trimmed.substringAfter("]: ").trim()
                    _connectionMessage.value = systemMsg
                    return
                }
                val text = trimmed.substringAfter("]: ").trim()
                handleIncomingMessage(senderName, text)
            }

            // [Sistem] Selamat datang, Nama!
            trimmed.startsWith("[Sistem]") && trimmed.contains("Selamat datang") -> {
                // Welcome message diterima, tidak perlu action tambahan
                // Karena server akan langsung kirim daftar online berikutnya
            }

            else -> {
                // Abaikan pesan lain
            }
        }
    }

    private fun handleIncomingMessage(senderName: String, text: String, isPrivate: Boolean = false) {
        val chatId = getChatIdForUser(senderName)
        val now = System.currentTimeMillis()
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))

        val message = Message(
            id = "msg_${now}_${senderName.hashCode()}",
            chatId = chatId,
            senderId = "user_${senderName.hashCode()}",
            text = text,
            timestamp = now,
            formattedTime = timeStr,
            status = MessageStatus.READ,
            type = MessageType.TEXT
        )

        _messagesMap.update { map ->
            val currentList = map[chatId] ?: emptyList()
            map + (chatId to (currentList + message))
        }

        val partner = User(
            id = "user_${senderName.hashCode()}",
            name = senderName,
            username = "@" + senderName.lowercase().replace("\\s+".toRegex(), ""),
            avatarInitials = getInitials(senderName),
            avatarColorHex = colorPalette[senderName.hashCode().and(0x7FFFFFFF) % colorPalette.size],
            isOnline = true
        )

        val lastMsg = message
        _chatThreads.update { threads ->
            val existing = threads.find { it.id == chatId }
            if (existing != null) {
                threads.map {
                    if (it.id == chatId) it.copy(
                        lastMessage = lastMsg,
                        unreadCount = it.unreadCount + 1
                    ) else it
                }
            } else {
                val newThread = ChatThread(
                    id = chatId,
                    partner = partner,
                    lastMessage = lastMsg,
                    unreadCount = 1
                )
                threads + newThread
            }
        }

        viewModelScope.launch {
            messageDao.insertMessage(message.toEntity(senderName, isPrivate))
            val existingThread = threadDao.getThreadById(chatId)
            if (existingThread != null) {
                threadDao.updateLastMessage(chatId, text, now)
                threadDao.updateUnreadCount(chatId, existingThread.unreadCount + 1)
            } else {
                threadDao.insertThread(
                    com.example.data.ChatThreadEntity(
                        id = chatId,
                        partnerName = senderName,
                        lastMessageText = text,
                        lastMessageTime = now,
                        unreadCount = 1
                    )
                )
            }
        }
    }

    private fun loadOnlineUsersAsContacts(names: List<String>) {
        val contacts = names.map { name ->
            val color = colorPalette[name.hashCode().and(0x7FFFFFFF) % colorPalette.size]
            ContactItem(
                user = User(
                    id = "user_${name.hashCode()}",
                    name = name,
                    username = "@" + name.lowercase().replace("\\s+".toRegex(), ""),
                    avatarInitials = getInitials(name),
                    avatarColorHex = color,
                    isOnline = true
                )
            )
        }
        _chatThreads.value.forEach { thread ->
            val updatedPartner = thread.partner.copy(isOnline = true)
            _chatThreads.update { threads ->
                threads.map { if (it.id == thread.id) it.copy(partner = updatedPartner) else it }
            }
        }
    }

    private fun getChatIdForUser(userName: String): String {
        return "chat_${userName.hashCode()}"
    }

    private fun getInitials(name: String): String {
        val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "U"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openChat(chatId: String) {
        _chatThreads.update { threads ->
            threads.map {
                if (it.id == chatId) it.copy(unreadCount = 0) else it
            }
        }
        _currentScreen.value = Screen.ChatDetail(chatId)

        viewModelScope.launch {
            val thread = threadDao.getThreadById(chatId)
            if (thread != null) {
                threadDao.updateUnreadCount(chatId, 0)
            }
        }
    }

    fun openChatWithUser(userName: String) {
        val chatId = getChatIdForUser(userName)
        val existing = _chatThreads.value.find { it.id == chatId }
        if (existing == null) {
            val color = colorPalette[userName.hashCode().and(0x7FFFFFFF) % colorPalette.size]
            val partner = User(
                id = "user_${userName.hashCode()}",
                name = userName,
                username = "@" + userName.lowercase().replace("\\s+".toRegex(), ""),
                avatarInitials = getInitials(userName),
                avatarColorHex = color,
                isOnline = true
            )
            val placeholderMsg = Message(
                id = "placeholder_${chatId}",
                chatId = chatId,
                senderId = "system",
                text = "Mulai percakapan dengan $userName",
                timestamp = System.currentTimeMillis(),
                formattedTime = "Baru saja",
                status = MessageStatus.READ
            )
            _chatThreads.update { threads ->
                threads + ChatThread(id = chatId, partner = partner, lastMessage = placeholderMsg)
            }
        }
        openChat(chatId)
    }

    fun togglePinChat(chatId: String) {
        _chatThreads.update { threads ->
            threads.map {
                if (it.id == chatId) it.copy(isPinned = !it.isPinned) else it
            }
        }
    }

    fun toggleMuteChat(chatId: String) {
        _chatThreads.update { threads ->
            threads.map {
                if (it.id == chatId) it.copy(isMuted = !it.isMuted) else it
            }
        }
    }

    fun setReplyingTo(message: Message?) {
        _replyingToMessage.value = message
    }

    fun sendMessage(chatId: String, text: String) {
        if (text.isBlank()) return

        val myUser = _currentUser.value
        val now = System.currentTimeMillis()
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))

        val newMsg = Message(
            id = "msg_${now}",
            chatId = chatId,
            senderId = myUser.id,
            text = text,
            timestamp = now,
            formattedTime = timeStr,
            status = MessageStatus.SENT,
            type = MessageType.TEXT,
            replyToText = _replyingToMessage.value?.text,
            replyToSender = _replyingToMessage.value?.let { if (it.senderId == myUser.id) "Anda" else "Teman" }
        )

        _replyingToMessage.value = null

        _messagesMap.update { map ->
            val currentList = map[chatId] ?: emptyList()
            map + (chatId to (currentList + newMsg))
        }

        _chatThreads.update { threads ->
            threads.map {
                if (it.id == chatId) it.copy(lastMessage = newMsg) else it
            }
        }

        val thread = _chatThreads.value.find { it.id == chatId }
        val partnerName = thread?.partner?.name
        val serverMessage = if (partnerName != null) {
            "@$partnerName $text"
        } else {
            text
        }
        chatClient.sendMessage(serverMessage)

        viewModelScope.launch {
            messageDao.insertMessage(newMsg.toEntity(myUser.name))
        }
    }

    fun queryOnlineUsers() {
        chatClient.sendMessage("/list")
    }

    fun sendVoiceNote(chatId: String, durationSec: Int) {
        val now = System.currentTimeMillis()
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))

        val voiceMsg = Message(
            id = "msg_voice_${now}",
            chatId = chatId,
            senderId = _currentUser.value.id,
            text = "",
            timestamp = now,
            formattedTime = timeStr,
            status = MessageStatus.SENT,
            type = MessageType.VOICE,
            voiceDurationSeconds = durationSec
        )

        _messagesMap.update { map ->
            val currentList = map[chatId] ?: emptyList()
            map + (chatId to (currentList + voiceMsg))
        }

        _chatThreads.update { threads ->
            threads.map {
                if (it.id == chatId) it.copy(lastMessage = voiceMsg) else it
            }
        }
    }

    fun addReactionToMessage(chatId: String, messageId: String, reactionEmoji: String) {
        _messagesMap.update { map ->
            val list = map[chatId] ?: return@update map
            val updated = list.map {
                if (it.id == messageId) {
                    val newReaction = if (it.reaction == reactionEmoji) null else reactionEmoji
                    it.copy(reaction = newReaction)
                } else it
            }
            map + (chatId to updated)
        }
    }

    fun toggleAudioPlayback(messageId: String) {
        _currentlyPlayingAudioId.value = if (_currentlyPlayingAudioId.value == messageId) null else messageId
    }

    override fun onCleared() {
        super.onCleared()
        chatClient.destroy()
    }
}
