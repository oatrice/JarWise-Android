package com.oatrice.jarwise.data.backup

import java.io.File

interface CloudStorageService {
    suspend fun uploadBackup(file: File): Result<String> // Returns File ID
    suspend fun listBackups(): Result<List<BackupMetadata>>
    suspend fun downloadBackup(fileId: String, destFile: File): Result<Unit>
}

data class BackupMetadata(
    val id: String,
    val name: String,
    val createdTime: Long,
    val sizeBytes: Long
)
