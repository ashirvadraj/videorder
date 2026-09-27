package com.videorder.downloader.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.videorder.downloader.data.database.entities.TelegramSessionMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelegramSessionDao {
    @Query("SELECT * FROM telegram_sessions WHERE id = 1")
    fun getSession(): Flow<TelegramSessionMetadataEntity?>

    @Query("SELECT * FROM telegram_sessions WHERE id = 1")
    suspend fun getSessionSync(): TelegramSessionMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: TelegramSessionMetadataEntity)

    @Query("UPDATE telegram_sessions SET isLoggedIn = 0, phoneNumberMasked = null, userId = null, firstName = null, username = null WHERE id = 1")
    suspend fun clearSession()
}
