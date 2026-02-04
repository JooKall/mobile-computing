package com.example.project

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MessageDao {
    @Query("SELECT * FROM MessageEntity WHERE userId = :userId")
    fun getMessagesForUser(userId: Int): List<MessageEntity>

    @Insert
    fun insertMessage(message: MessageEntity)
}