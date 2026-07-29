package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatThreadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: ChatThreadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreads(threads: List<ChatThreadEntity>)

    @Query("SELECT * FROM chat_threads ORDER BY lastMessageTime DESC")
    suspend fun getAllThreads(): List<ChatThreadEntity>

    @Query("SELECT * FROM chat_threads WHERE id = :threadId")
    suspend fun getThreadById(threadId: String): ChatThreadEntity?

    @Query("UPDATE chat_threads SET lastMessageText = :text, lastMessageTime = :time WHERE id = :threadId")
    suspend fun updateLastMessage(threadId: String, text: String, time: Long)

    @Query("UPDATE chat_threads SET unreadCount = :count WHERE id = :threadId")
    suspend fun updateUnreadCount(threadId: String, count: Int)

    @Query("DELETE FROM chat_threads")
    suspend fun deleteAllThreads()
}
