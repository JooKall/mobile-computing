package com.example.hw4sensorsandnotifications

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    /* If you want a column to have a different name, add the @ColumnInfo annotation to the field and set the name property.
        Example:
        @ColumnInfo(name = "first_name") val firstName: String?,
        @ColumnInfo(name = "last_name") val lastName: String?
    */
    val username: String = "Joonas",
    val imageUri: String? = null
)