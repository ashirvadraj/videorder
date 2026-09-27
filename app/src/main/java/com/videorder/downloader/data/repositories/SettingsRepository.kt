package com.videorder.downloader.data.repositories

import com.videorder.downloader.data.database.dao.SettingsDao
import com.videorder.downloader.data.database.entities.SettingsEntity
import com.videorder.downloader.domain.models.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun getSettingsSync(): AppSettings
    suspend fun updateDownloadLocation(location: String)
    suspend fun updateMaxSimultaneous(max: Int)
    suspend fun updateWifiOnly(wifiOnly: Boolean)
    suspend fun updateTheme(theme: String)
    suspend fun setOnboardingCompleted()
    suspend fun saveSettings(settings: AppSettings)
}

class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override fun getSettings(): Flow<AppSettings> {
        return settingsDao.getSettings().map { it?.toDomain() ?: AppSettings(downloadLocation = "Downloads/Videorder") }
    }

    override suspend fun getSettingsSync(): AppSettings {
        return settingsDao.getSettingsSync()?.toDomain() ?: AppSettings(downloadLocation = "Downloads/Videorder")
    }

    override suspend fun updateDownloadLocation(location: String) {
        settingsDao.updateDownloadLocation(location)
    }

    override suspend fun updateMaxSimultaneous(max: Int) {
        settingsDao.updateMaxSimultaneous(max)
    }

    override suspend fun updateWifiOnly(wifiOnly: Boolean) {
        settingsDao.updateWifiOnly(wifiOnly)
    }

    override suspend fun updateTheme(theme: String) {
        settingsDao.updateTheme(theme)
    }

    override suspend fun setOnboardingCompleted() {
        settingsDao.setOnboardingCompleted()
    }

    override suspend fun saveSettings(settings: AppSettings) {
        settingsDao.saveSettings(SettingsEntity.fromDomain(settings))
    }
}
