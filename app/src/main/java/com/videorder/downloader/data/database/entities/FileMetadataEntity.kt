package com.videorder.downloader.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.videorder.downloader.domain.models.FileItem
import com.videorder.downloader.domain.models.MediaType

@Entity(tableName = "file_metadata")
data class FileMetadataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filename: String,
    val localPath: String,
    val sizeBytes: Long,
    val mimeType: String,
    val mediaType: String,
    val lastModified: Long,
    val durationMs: Long? = null,
    val resolution: String? = null,
    val thumbnailUri: String? = null
) {
    fun toDomain(): FileItem {
        val parsedType = try {
            MediaType.valueOf(mediaType)
        } catch (e: Exception) {
            MediaType.OTHER
        }
        return FileItem(
            id = id,
            filename = filename,
            localPath = localPath,
            sizeBytes = sizeBytes,
            mimeType = mimeType,
            mediaType = parsedType,
            lastModified = lastModified,
            durationMs = durationMs,
            resolution = resolution,
            thumbnailUri = thumbnailUri
        )
    }

    companion object {
        fun fromDomain(file: FileItem): FileMetadataEntity {
            return FileMetadataEntity(
                id = file.id,
                filename = file.filename,
                localPath = file.localPath,
                sizeBytes = file.sizeBytes,
                mimeType = file.mimeType,
                mediaType = file.mediaType.name,
                lastModified = file.lastModified,
                durationMs = file.durationMs,
                resolution = file.resolution,
                thumbnailUri = file.thumbnailUri
            )
        }
    }
}
