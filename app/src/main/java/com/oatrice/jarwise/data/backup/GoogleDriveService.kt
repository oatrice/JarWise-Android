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
import java.util.Collections

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

    override suspend fun uploadBackup(file: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService() ?: return@withContext Result.failure(Exception("User not signed in"))
            
            val fileMetadata = com.google.api.services.drive.model.File()
            fileMetadata.name = "jarwise_backup.db"
            // fileMetadata.parents = listOf("appDataFolder") // If using appDataFolder

            val mediaContent = FileContent("application/x-sqlite3", file)
            
            // TODO: check if file exists and update instead of create new one every time?
            // For MVP: Simple create
            
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
                .setQ("name = 'jarwise_backup.db' and trashed = false")
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
