package com.videorder.downloader.data.repositories

import android.content.Context
import com.videorder.downloader.data.database.dao.FileMetadataDao
import com.videorder.downloader.data.database.entities.FileMetadataEntity
import com.videorder.downloader.domain.models.FileItem
import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

interface FileRepository {
    fun getAllFiles(): Flow<List<FileItem>>
    fun getFilesByType(mediaType: MediaType): Flow<List<FileItem>>
    suspend fun syncLocalDirectory(customDir: String? = null)
    suspend fun renameFile(fileId: Long, newName: String): Boolean
    suspend fun deleteFile(fileId: Long): Boolean
    suspend fun getFileById(fileId: Long): FileItem?
}

class FileRepositoryImpl(
    private val context: Context,
    private val fileDao: FileMetadataDao
) : FileRepository {

    override fun getAllFiles(): Flow<List<FileItem>> {
        return fileDao.getAllFiles().map { list -> list.map { it.toDomain() } }
    }

    override fun getFilesByType(mediaType: MediaType): Flow<List<FileItem>> {
        return fileDao.getFilesByType(mediaType.name).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun syncLocalDirectory(customDir: String?) = withContext(Dispatchers.IO) {
        val targetDir = FileUtils.getTargetDownloadDirectory(context, customDir)
        if (!targetDir.exists()) return@withContext

        val diskFiles = targetDir.listFiles() ?: return@withContext
        for (file in diskFiles) {
            if (file.isFile && !file.name.startsWith(".")) {
                val existing = fileDao.getFileByPath(file.absolutePath)
                if (existing == null) {
                    val ext = file.extension
                    val mime = FileUtils.getMimeTypeFromExtension(ext)
                    val type = FileUtils.getMediaTypeFromMimeOrExt(mime, file.name)
                    fileDao.insertFile(
                        FileMetadataEntity(
                            filename = file.name,
                            localPath = file.absolutePath,
                            sizeBytes = file.length(),
                            mimeType = mime,
                            mediaType = type.name,
                            lastModified = file.lastModified()
                        )
                    )
                }
            }
        }
    }

    override suspend fun renameFile(fileId: Long, newName: String): Boolean = withContext(Dispatchers.IO) {
        val entity = fileDao.getFileById(fileId) ?: return@withContext false
        val oldFile = File(entity.localPath)
        if (!oldFile.exists()) return@withContext false

        val sanitizedNewName = FileUtils.sanitizeFilename(newName)
        val ext = oldFile.extension
        val finalName = if (sanitizedNewName.contains(".")) sanitizedNewName else "$sanitizedNewName.$ext"
        val newFile = File(oldFile.parentFile, finalName)

        val success = oldFile.renameTo(newFile)
        if (success) {
            fileDao.renameFile(fileId, finalName, newFile.absolutePath)
        }
        success
    }

    override suspend fun deleteFile(fileId: Long): Boolean = withContext(Dispatchers.IO) {
        val entity = fileDao.getFileById(fileId) ?: return@withContext false
        val file = File(entity.localPath)
        if (file.exists()) {
            file.delete()
        }
        fileDao.deleteFile(fileId)
        true
    }

    override suspend fun getFileById(fileId: Long): FileItem? {
        return fileDao.getFileById(fileId)?.toDomain()
    }
}
