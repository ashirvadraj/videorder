package com.videorder.downloader.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.videorder.downloader.data.database.dao.DownloadHistoryDao
import com.videorder.downloader.data.database.dao.DownloadTaskDao
import com.videorder.downloader.data.database.dao.FileMetadataDao
import com.videorder.downloader.data.database.dao.SettingsDao
import com.videorder.downloader.data.database.dao.TelegramSessionDao
import com.videorder.downloader.data.database.entities.DownloadHistoryEntity
import com.videorder.downloader.data.database.entities.DownloadTaskEntity
import com.videorder.downloader.data.database.entities.FileMetadataEntity
import com.videorder.downloader.data.database.entities.SettingsEntity
import com.videorder.downloader.data.database.entities.TelegramSessionMetadataEntity

@Database(
    entities = [
        DownloadTaskEntity::class,
        DownloadHistoryEntity::class,
        FileMetadataEntity::class,
        TelegramSessionMetadataEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadTaskDao(): DownloadTaskDao
    abstract fun downloadHistoryDao(): DownloadHistoryDao
    abstract fun fileMetadataDao(): FileMetadataDao
    abstract fun telegramSessionDao(): TelegramSessionDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "videorder_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
