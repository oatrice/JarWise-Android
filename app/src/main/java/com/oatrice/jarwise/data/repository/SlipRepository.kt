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



    data class ImageBucket(
        val id: String,
        val displayName: String,
        val coverUri: Uri,
        val count: Int
    )

    /**
     * Get unique folders (buckets) containing images.
     * Groups by BUCKET_ID to create an album list.
     */
    fun getImageBuckets(): List<ImageBucket> {
        val buckets = mutableMapOf<String, ImageBucket>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
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
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val bucketId = cursor.getString(bucketIdColumn) ?: continue
                val bucketName = cursor.getString(bucketNameColumn) ?: "Unknown"
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(collection, id)

                if (!buckets.containsKey(bucketId)) {
                    // First time seeing this bucket, so this is the most recent image (sort order)
                    buckets[bucketId] = ImageBucket(bucketId, bucketName, contentUri, 1)
                } else {
                    // Already seen, just increment count
                    val current = buckets[bucketId]!!
                    buckets[bucketId] = current.copy(count = current.count + 1)
                }
            }
        }
        return buckets.values.toList()
    }

    /**
     * Get images specifically from a bucket (folder).
     */
    fun getImagesInBucket(bucketId: String, limit: Int = 100): List<Uri> {
        val imageList = mutableListOf<Uri>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )
        val selection = "${MediaStore.Images.Media.BUCKET_ID} = ?"
        val selectionArgs = arrayOf(bucketId)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
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
