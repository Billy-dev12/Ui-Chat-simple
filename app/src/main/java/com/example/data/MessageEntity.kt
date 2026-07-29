package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.MessageType

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val formattedTime: String,
    val status: String,
    val type: String,
    val isPrivate: Boolean = false
)

fun MessageEntity.toMessage(): Message {
    return Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        formattedTime = formattedTime,
        status = MessageStatus.valueOf(status),
        type = MessageType.valueOf(type)
    )
}

fun Message.toEntity(senderName: String, isPrivate: Boolean = false): MessageEntity {
    return MessageEntity(
        id = id,
        chatId = chatId,
        senderId = senderId,
        senderName = senderName,
        text = text,
        timestamp = timestamp,
        formattedTime = formattedTime,
        status = status.name,
        type = type.name,
        isPrivate = isPrivate
    )
}
