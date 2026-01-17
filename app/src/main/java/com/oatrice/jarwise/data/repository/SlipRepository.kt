package com.oatrice.jarwise.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date

class SlipRepository(private val context: Context) {

    /**
     * Get recent images from the device's main gallery (Camera/Pictures).
     * Used for "Auto-Detect" workflow.
     */
    fun getRecentImages(limit: Int = 10): List<Uri> {
        val imageList = mutableListOf<Uri>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            var count = 0
            while (cursor.moveToNext() && count < limit) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(collection, id)
                imageList.add(contentUri)
                count++
            }
        }
        return imageList
    }

    /**
     * Get images from a specific folder (Tree Uri).
     * Used for "Import Folder" workflow.
     */
    fun getImagesFromFolder(folderUri: Uri): List<Uri> {
        val imageList = mutableListOf<Uri>()
        val directory = DocumentFile.fromTreeUri(context, folderUri)
        
        directory?.listFiles()?.forEach { file ->
            if (file.isFile && file.type?.startsWith("image/") == true) {
                imageList.add(file.uri)
            }
        }
        return imageList
    }

    /**
     * Observe for new images added to the MediaStore.
     * Emits the URI of the newly added image.
     */
    fun observeNewImages(): Flow<Uri> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                // When a change is detected, we query the latest image to confirm it's new
                // For simplicity, we just re-fetch the latest one here. 
                // In a real app, we might check timestamps more strictly.
                val latest = getRecentImages(1).firstOrNull()
                if (latest != null) {
                    trySend(latest)
                }
            }
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )

        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }
}
