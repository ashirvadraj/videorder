package com.videorder.downloader

import android.app.Application
import com.videorder.downloader.data.database.AppDatabase
import com.videorder.downloader.data.repositories.DownloadRepository
import com.videorder.downloader.data.repositories.DownloadRepositoryImpl
import com.videorder.downloader.data.repositories.FileRepository
import com.videorder.downloader.data.repositories.FileRepositoryImpl
import com.videorder.downloader.data.repositories.HistoryRepository
import com.videorder.downloader.data.repositories.HistoryRepositoryImpl
import com.videorder.downloader.data.repositories.SettingsRepository
import com.videorder.downloader.data.repositories.SettingsRepositoryImpl
import com.videorder.downloader.data.repositories.TelegramRepository
import com.videorder.downloader.data.repositories.TelegramRepositoryImpl
import com.videorder.downloader.downloader.network.NetworkMonitor
import com.videorder.downloader.downloader.notifications.DownloadNotificationManager
import com.videorder.downloader.downloader.queue.DownloadQueueManager
import com.videorder.downloader.downloader.services.DownloadForegroundService
import com.videorder.downloader.domain.usecases.DetectMediaUseCase
import com.videorder.downloader.domain.usecases.ManageDownloadUseCase
import com.videorder.downloader.domain.usecases.StartDownloadUseCase
import com.videorder.downloader.domain.usecases.TelegramAuthUseCase
import com.videorder.downloader.domain.usecases.TelegramBrowseUseCase
import com.videorder.downloader.security.SecureKeystoreManager
import com.videorder.downloader.security.TelegramSessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideorderApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var keystoreManager: SecureKeystoreManager
        private set

    lateinit var sessionStore: TelegramSessionStore
        private set

    lateinit var networkMonitor: NetworkMonitor
        private set

    lateinit var notificationManager: DownloadNotificationManager
        private set

    lateinit var queueManager: DownloadQueueManager
        private set

    lateinit var downloadRepository: DownloadRepository
        private set

    lateinit var historyRepository: HistoryRepository
        private set

    lateinit var fileRepository: FileRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var telegramRepository: TelegramRepository
        private set

    lateinit var detectMediaUseCase: DetectMediaUseCase
        private set

    lateinit var startDownloadUseCase: StartDownloadUseCase
        private set

    lateinit var manageDownloadUseCase: ManageDownloadUseCase
        private set

    lateinit var telegramAuthUseCase: TelegramAuthUseCase
        private set

    lateinit var telegramBrowseUseCase: TelegramBrowseUseCase
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(this)
        keystoreManager = SecureKeystoreManager(this)
        sessionStore = TelegramSessionStore(this, keystoreManager)
        networkMonitor = NetworkMonitor(this)
        notificationManager = DownloadNotificationManager(this)
        queueManager = DownloadQueueManager(this, database, networkMonitor, notificationManager)

        downloadRepository = DownloadRepositoryImpl(database.downloadTaskDao(), queueManager)
        historyRepository = HistoryRepositoryImpl(database.downloadHistoryDao())
        fileRepository = FileRepositoryImpl(this, database.fileMetadataDao())
        settingsRepository = SettingsRepositoryImpl(database.settingsDao())
        telegramRepository = TelegramRepositoryImpl(database.telegramSessionDao(), sessionStore)

        detectMediaUseCase = DetectMediaUseCase()
        startDownloadUseCase = StartDownloadUseCase(downloadRepository)
        manageDownloadUseCase = ManageDownloadUseCase(downloadRepository)
        telegramAuthUseCase = TelegramAuthUseCase(telegramRepository)
        telegramBrowseUseCase = TelegramBrowseUseCase(telegramRepository, downloadRepository)

        // Preload local downloaded files into database on startup
        CoroutineScope(Dispatchers.IO).launch {
            fileRepository.syncLocalDirectory()
        }
    }
}
