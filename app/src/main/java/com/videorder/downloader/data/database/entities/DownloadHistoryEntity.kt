package com.videorder.downloader.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.videorder.downloader.domain.models.MediaType

@Entity(tableName = "download_history")
data class DownloadHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: String,
    val url: String,
    val filename: String,
    val mimeType: String,
    val fileSize: Long,
    val quality: String?,
    val localPath: String,
    val completedAt: Long,
    val thumbnailUrl: String?,
    val mediaType: String = MediaType.VIDEO.name
)
