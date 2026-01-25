package com.example.hw4sensorsandnotifications

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE username = :username")
    fun findUserWithName(username: String): User

    @Query("SELECT * FROM user LIMIT 1")
    fun getUser(): User

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertUsers(user: User)

    @Update
    fun updateUser(user: User)
}