package com.example.model

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ
}

enum class MessageType {
    TEXT, VOICE, IMAGE, LOCATION
}

data class User(
    val id: String,
    val name: String,
    val username: String,
    val avatarInitials: String,
    val avatarColorHex: Long = 0xFF10B981,
    val isOnline: Boolean = false,
    val lastSeen: String = "Online",
    val bio: String = "Menggunakan AuraChat"
)

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String = "10:45",
    val status: MessageStatus = MessageStatus.READ,
    val type: MessageType = MessageType.TEXT,
    val voiceDurationSeconds: Int = 0,
    val voiceProgress: Float = 0.0f,
    val imageUrl: String? = null,
    val reaction: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null
)

data class ChatThread(
    val id: String,
    val partner: User,
    val lastMessage: Message,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isGroup: Boolean = false,
    val groupName: String? = null,
    val isTyping: Boolean = false
)

data class UserStory(
    val id: String,
    val user: User,
    val hasUnseen: Boolean = true,
    val timeAgo: String = "Baru saja"
)

data class ContactItem(
    val user: User,
    val isFavorite: Boolean = false
)
