package com.videorder.downloader.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.videorder.downloader.data.database.entities.FileMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileMetadataDao {
    @Query("SELECT * FROM file_metadata ORDER BY lastModified DESC")
    fun getAllFiles(): Flow<List<FileMetadataEntity>>

    @Query("SELECT * FROM file_metadata WHERE mediaType = :mediaType ORDER BY lastModified DESC")
    fun getFilesByType(mediaType: String): Flow<List<FileMetadataEntity>>

    @Query("SELECT * FROM file_metadata WHERE id = :id")
    suspend fun getFileById(id: Long): FileMetadataEntity?

    @Query("SELECT * FROM file_metadata WHERE localPath = :path LIMIT 1")
    suspend fun getFileByPath(path: String): FileMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileMetadataEntity): Long

    @Update
    suspend fun updateFile(file: FileMetadataEntity)

    @Query("UPDATE file_metadata SET filename = :newFilename, localPath = :newPath WHERE id = :id")
    suspend fun renameFile(id: Long, newFilename: String, newPath: String)

    @Query("DELETE FROM file_metadata WHERE id = :id")
    suspend fun deleteFile(id: Long)

    @Query("DELETE FROM file_metadata WHERE localPath = :path")
    suspend fun deleteFileByPath(path: String)

    @Query("DELETE FROM file_metadata")
    suspend fun clearAll()
}
