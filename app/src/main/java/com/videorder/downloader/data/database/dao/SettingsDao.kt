package com.videorder.downloader.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.videorder.downloader.data.database.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsSync(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)

    @Query("UPDATE app_settings SET downloadLocation = :location WHERE id = 1")
    suspend fun updateDownloadLocation(location: String)

    @Query("UPDATE app_settings SET maxSimultaneousDownloads = :max WHERE id = 1")
    suspend fun updateMaxSimultaneous(max: Int)

    @Query("UPDATE app_settings SET wifiOnly = :wifiOnly WHERE id = 1")
    suspend fun updateWifiOnly(wifiOnly: Boolean)

    @Query("UPDATE app_settings SET themeMode = :theme WHERE id = 1")
    suspend fun updateTheme(theme: String)

    @Query("UPDATE app_settings SET hasCompletedOnboarding = 1 WHERE id = 1")
    suspend fun setOnboardingCompleted()
}
