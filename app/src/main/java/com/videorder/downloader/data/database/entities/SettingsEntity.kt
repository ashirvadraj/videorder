package com.videorder.downloader.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.videorder.downloader.domain.models.AppSettings

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val downloadLocation: String = "Downloads/Videorder",
    val maxSimultaneousDownloads: Int = 3,
    val wifiOnly: Boolean = false,
    val mobileDataAllowed: Boolean = true,
    val autoResume: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val themeMode: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
    val hasCompletedOnboarding: Boolean = false
) {
    fun toDomain(): AppSettings {
        return AppSettings(
            downloadLocation = downloadLocation,
            maxSimultaneousDownloads = maxSimultaneousDownloads,
            wifiOnly = wifiOnly,
            mobileDataAllowed = mobileDataAllowed,
            autoResume = autoResume,
            notificationsEnabled = notificationsEnabled,
            themeMode = themeMode,
            hasCompletedOnboarding = hasCompletedOnboarding
        )
    }

    companion object {
        fun fromDomain(settings: AppSettings): SettingsEntity {
            return SettingsEntity(
                id = 1,
                downloadLocation = settings.downloadLocation,
                maxSimultaneousDownloads = settings.maxSimultaneousDownloads,
                wifiOnly = settings.wifiOnly,
                mobileDataAllowed = settings.mobileDataAllowed,
                autoResume = settings.autoResume,
                notificationsEnabled = settings.notificationsEnabled,
                themeMode = settings.themeMode,
                hasCompletedOnboarding = settings.hasCompletedOnboarding
            )
        }
    }
}
