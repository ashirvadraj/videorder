package com.videorder.downloader.domain.models

data class AppSettings(
    val downloadLocation: String,
    val maxSimultaneousDownloads: Int = 3,
    val wifiOnly: Boolean = false,
    val mobileDataAllowed: Boolean = true,
    val autoResume: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val themeMode: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
    val hasCompletedOnboarding: Boolean = false
)
