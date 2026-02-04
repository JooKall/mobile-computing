package com.example.project

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

//https://medium.com/@vontonnie/connecting-room-tables-using-foreign-keys-c19450361603
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("userId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val content: String
)