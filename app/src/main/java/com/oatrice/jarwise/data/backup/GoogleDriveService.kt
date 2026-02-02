package com.oatrice.jarwise.data.backup

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.oatrice.jarwise.data.auth.AuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

class GoogleDriveService(
    private val context: Context,
    private val authService: AuthService
) : CloudStorageService {

    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = AndroidHttp.newCompatibleTransport()

    private fun getDriveService(): Drive? {
        val currentUser = authService.currentUser.value ?: return null
        
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singletonList(DriveScopes.DRIVE_FILE) // And DRIVE_APPDATA if needed
        )
        credential.selectedAccountName = currentUser.email

        return Drive.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("JarWise")
            .build()
    }

    private fun getOrCreateBackupFolder(service: Drive): String {
        val folderName = "JarWise backup"
        
        // 1. Check if folder exists
        val result = service.files().list()
            .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '$folderName' and trashed = false")
            .setSpaces("drive")
            .setFields("files(id)")
            .execute()

        if (result.files.isNotEmpty()) {
            return result.files[0].id
        }

        // 2. Create folder if not exists
        val folderMetadata = com.google.api.services.drive.model.File()
        folderMetadata.name = folderName
        folderMetadata.mimeType = "application/vnd.google-apps.folder"

        val folder = service.files().create(folderMetadata)
            .setFields("id")
            .execute()
            
        return folder.id
    }

    override suspend fun uploadBackup(file: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService() ?: return@withContext Result.failure(Exception("User not signed in"))
            
            // Get or create parent folder
            val folderId = getOrCreateBackupFolder(service)

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileMetadata = com.google.api.services.drive.model.File()
            fileMetadata.name = "jarwise_backup_$timestamp.db"
            fileMetadata.parents = listOf(folderId)

            val mediaContent = FileContent("application/x-sqlite3", file)
            
            val uploadedFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
                
            Result.success(uploadedFile.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun listBackups(): Result<List<BackupMetadata>> = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService() ?: return@withContext Result.failure(Exception("User not signed in"))
            
            val result = service.files().list()
                .setQ("name contains 'jarwise_backup_' and name contains '.db' and trashed = false")
                .setSpaces("drive") // or "appDataFolder"
                .setFields("files(id, name, createdTime, size)")
                .execute()
                
            val backups = result.files.map { 
                BackupMetadata(
                    id = it.id,
                    name = it.name,
                    createdTime = it.createdTime?.value ?: 0L,
                    sizeBytes = it.getSize() ?: 0L
                )
            }
            Result.success(backups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadBackup(fileId: String, destFile: File): Result<Unit> = withContext(Dispatchers.IO) {
         try {
            val service = getDriveService() ?: return@withContext Result.failure(Exception("User not signed in"))
            
            val outputStream = destFile.outputStream()
            service.files().get(fileId)
                .executeMediaAndDownloadTo(outputStream)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
