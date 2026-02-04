package com.oatrice.jarwise.data.repository

import android.content.Context
import android.net.Uri
import com.oatrice.jarwise.data.api.MigrationApi
import com.oatrice.jarwise.data.api.model.MigrationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class MigrationRepository(
    private val api: MigrationApi,
    private val context: Context
) {

    suspend fun uploadMigrationFiles(mmbakUri: Uri, xlsUri: Uri): Result<MigrationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mmbakFile = getFileFromUri(context, mmbakUri, "backup.mmbak")
                val xlsFile = getFileFromUri(context, xlsUri, "backup.xls")

                if (mmbakFile == null || xlsFile == null) {
                    return@withContext Result.failure(Exception("Failed to read files"))
                }

                val mmbakRequestBody = mmbakFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                val xlsRequestBody = xlsFile.asRequestBody("application/vnd.ms-excel".toMediaTypeOrNull())

                val mmbakPart = MultipartBody.Part.createFormData("mmbak_file", mmbakFile.name, mmbakRequestBody)
                val xlsPart = MultipartBody.Part.createFormData("xls_file", xlsFile.name, xlsRequestBody)

                val response = api.uploadMigrationFiles(mmbakPart, xlsPart)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Upload failed: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
