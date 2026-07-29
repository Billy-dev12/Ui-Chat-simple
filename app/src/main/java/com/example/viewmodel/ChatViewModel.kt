package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ChatThread
import com.example.model.ContactItem
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.MessageType
import com.example.model.User
import com.example.model.UserStory
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class Screen {
    object WelcomeName : Screen()
    object ChatList : Screen()
    data class ChatDetail(val chatId: String) : Screen()
    object Contacts : Screen()
    object Settings : Screen()
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentUser = MutableStateFlow(
        User(
            id = "user_me",
            name = "Setyo Pratama",
            username = "@setyop",
            avatarInitials = "SP",
            avatarColorHex = 0xFF10B981,
            isOnline = true,
            bio = "Pikiran jernih, komunikasi sederhana ☕"
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

    private val _stories = MutableStateFlow<List<UserStory>>(emptyList())
    val stories: StateFlow<List<UserStory>> = _stories.asStateFlow()

    private val _contacts = MutableStateFlow<List<ContactItem>>(emptyList())
    val contacts: StateFlow<List<ContactItem>> = _contacts.asStateFlow()

    private val _replyingToMessage = MutableStateFlow<Message?>(null)
    val replyingToMessage: StateFlow<Message?> = _replyingToMessage.asStateFlow()

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _currentlyPlayingAudioId = MutableStateFlow<String?>(null)
    val currentlyPlayingAudioId: StateFlow<String?> = _currentlyPlayingAudioId.asStateFlow()

    init {
        checkSavedUserAndLoadData()
    }

    private fun checkSavedUserAndLoadData() {
        val prefs = getApplication<Application>().getSharedPreferences("aurachat_prefs", Context.MODE_PRIVATE)
        val savedName = prefs.getString("user_name", null)
        val isFirstRun = prefs.getBoolean("is_first_run", true)

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
            _currentScreen.value = Screen.ChatList
        }
        loadMockData()
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

        _stories.update { list ->
            list.map {
                if (it.id == "s_0") it.copy(user = updatedUser) else it
            }
        }

        if (_currentScreen.value is Screen.WelcomeName) {
            _currentScreen.value = Screen.ChatList
        }
    }

    private fun getInitials(name: String): String {
        val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "U"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }

    private fun loadMockData() {
        val me = _currentUser.value
        val userNadia = User("u_1", "Nadia Arisandi", "@nadia", "NA", 0xFFEC4899, true, "Online", "Desain UI/UX & Kopi Pagi 🎨")
        val userRian = User("u_2", "Rian Hidayat", "@rianh", "RH", 0xFF3B82F6, true, "Online", "Building cool Android apps 🚀")
        val userAulia = User("u_3", "Aulia Putri", "@aulia", "AP", 0xFFF59E0B, false, "Kemarin 21:15", "Membaca buku di akhir pekan 📚")
        val userDevGroup = User("u_4", "Grup Tech Design ID", "@techgroup", "TD", 0xFF10B981, true, "12 Anggota", "Komunitas UX Indonesia")
        val userDimas = User("u_5", "Dimas Anggara", "@dimas", "DA", 0xFF8B5CF6, false, "2 jam yang lalu", "Kopi & Koding ☕")
        val userSiti = User("u_6", "Siti Rahma", "@siti", "SR", 0xFF06B6D4, true, "Online", "Desainer Grafis")

        val storiesList = listOf(
            UserStory("s_0", me, hasUnseen = false, timeAgo = "Status Saya"),
            UserStory("s_1", userNadia, hasUnseen = true, timeAgo = "10m"),
            UserStory("s_2", userRian, hasUnseen = true, timeAgo = "35m"),
            UserStory("s_3", userSiti, hasUnseen = true, timeAgo = "1j"),
            UserStory("s_4", userDimas, hasUnseen = false, timeAgo = "3j")
        )

        val nadiaMessages = listOf(
            Message("m_1_1", "c_1", "u_1", "Halo ${me.name}! Gimana konsep UI dark mode untuk app chat ini? Pilihan warnanya nyaman di mata ga?", timestamp = 1700000000000, formattedTime = "09:30", status = MessageStatus.READ),
            Message("m_1_2", "c_1", me.id, "Bagus banget Nadia! Menggunakan background slate dark (#0B0F17) dengan aksen emerald (#10B981). Kontrasnya lembut dan ramah OLED.", timestamp = 1700000050000, formattedTime = "09:31", status = MessageStatus.READ),
            Message("m_1_3", "c_1", "u_1", "Dengar ini ya, ini preview audio penjelasan warna UI nya:", timestamp = 1700000100000, formattedTime = "09:32", status = MessageStatus.READ),
            Message("m_1_4", "c_1", "u_1", "", timestamp = 1700000120000, formattedTime = "09:33", status = MessageStatus.READ, type = MessageType.VOICE, voiceDurationSeconds = 14),
            Message("m_1_5", "c_1", me.id, "Keren banget! Transisi antar layernya juga terasa responsif dan elegan ✨", timestamp = 1700000200000, formattedTime = "09:35", status = MessageStatus.READ, reaction = "❤️")
        )

        val rianMessages = listOf(
            Message("m_2_1", "c_2", "u_2", "Mas, prototype Compose UI udah ready buat direview nih.", timestamp = 1700000100000, formattedTime = "10:12", status = MessageStatus.READ),
            Message("m_2_2", "c_2", me.id, "Siap Rian, performa animasi dan recomposition-nya smooth?", timestamp = 1700000150000, formattedTime = "10:14", status = MessageStatus.READ),
            Message("m_2_3", "c_2", "u_2", "Sangat mulus! Kita pakai Material 3 tokens & state hoisted dengan sangat rapi.", timestamp = 1700000200000, formattedTime = "10:15", status = MessageStatus.READ)
        )

        val auliaMessages = listOf(
            Message("m_3_1", "c_3", "u_3", "Besok jam 10 pagi meeting desain ya ${me.name}.", timestamp = 1700000000000, formattedTime = "Kemarin", status = MessageStatus.READ)
        )

        val groupMessages = listOf(
            Message("m_4_1", "c_4", "u_6", "Halo semuanya, selamat datang di perancangan UI minimalis AuraChat!", timestamp = 1700000000000, formattedTime = "08:15", status = MessageStatus.READ),
            Message("m_4_2", "c_4", "u_2", "Desain minimalis bikin mata engga cepat lelah pas pakai app malam hari 👍", timestamp = 1700000100000, formattedTime = "08:20", status = MessageStatus.READ)
        )

        val threads = listOf(
            ChatThread("c_1", userNadia, nadiaMessages.last(), unreadCount = 0, isPinned = true, isMuted = false),
            ChatThread("c_2", userRian, rianMessages.last(), unreadCount = 2, isPinned = true, isMuted = false),
            ChatThread("c_4", userDevGroup, groupMessages.last(), unreadCount = 1, isPinned = false, isMuted = false, isGroup = true, groupName = "Grup Tech Design ID"),
            ChatThread("c_3", userAulia, auliaMessages.last(), unreadCount = 0, isPinned = false, isMuted = true)
        )

        val contactsList = listOf(
            ContactItem(userNadia, isFavorite = true),
            ContactItem(userRian, isFavorite = true),
            ContactItem(userSiti, isFavorite = true),
            ContactItem(userAulia, isFavorite = false),
            ContactItem(userDimas, isFavorite = false)
        )

        _stories.value = storiesList
        _chatThreads.value = threads
        _messagesMap.value = mapOf(
            "c_1" to nadiaMessages,
            "c_2" to rianMessages,
            "c_3" to auliaMessages,
            "c_4" to groupMessages
        )
        _contacts.value = contactsList
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
        // Clear unread count for this thread
        _chatThreads.update { threads ->
            threads.map {
                if (it.id == chatId) it.copy(unreadCount = 0) else it
            }
        }
        _currentScreen.value = Screen.ChatDetail(chatId)
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
        val newMsg = Message(
            id = "msg_${System.currentTimeMillis()}",
            chatId = chatId,
            senderId = myUser.id,
            text = text,
            timestamp = System.currentTimeMillis(),
            formattedTime = "Baru saja",
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

        // Update thread last message
        _chatThreads.update { threads ->
            threads.map {
                if (it.id == chatId) it.copy(lastMessage = newMsg) else it
            }
        }

        // Simulate auto-reply after 1.5s for interactive demo
        viewModelScope.launch {
            delay(800)
            _chatThreads.update { threads ->
                threads.map { if (it.id == chatId) it.copy(isTyping = true) else it }
            }
            delay(1500)
            _chatThreads.update { threads ->
                threads.map { if (it.id == chatId) it.copy(isTyping = false) else it }
            }

            val replyTexts = listOf(
                "Pesan diterima dengan jelas 👍!",
                "Desain UI-nya keren dan responsif banget!",
                "Sangat nyaman di mata pas mode gelap 🌙",
                "Siap, terima kasih atas informasinya!"
            )
            val randomReply = replyTexts.random()

            val partnerReply = Message(
                id = "msg_reply_${System.currentTimeMillis()}",
                chatId = chatId,
                senderId = "u_1",
                text = randomReply,
                timestamp = System.currentTimeMillis(),
                formattedTime = "Baru saja",
                status = MessageStatus.READ,
                type = MessageType.TEXT
            )

            _messagesMap.update { map ->
                val currentList = map[chatId] ?: emptyList()
                map + (chatId to (currentList + partnerReply))
            }

            _chatThreads.update { threads ->
                threads.map {
                    if (it.id == chatId) it.copy(lastMessage = partnerReply) else it
                }
            }
        }
    }

    fun sendVoiceNote(chatId: String, durationSec: Int) {
        val voiceMsg = Message(
            id = "msg_voice_${System.currentTimeMillis()}",
            chatId = chatId,
            senderId = _currentUser.value.id,
            text = "",
            timestamp = System.currentTimeMillis(),
            formattedTime = "Baru saja",
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
}
