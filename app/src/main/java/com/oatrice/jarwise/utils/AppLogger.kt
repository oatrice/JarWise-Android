package com.oatrice.jarwise.utils

import android.content.Context

interface AppLogger {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

class AndroidAppLogger(val context: Context? = null) : AppLogger {
    private val logFileName = "jarwise_app.log"

    override fun d(tag: String, message: String) {
        android.util.Log.d(tag, message)
        writeToFile("DEBUG: $tag: $message")
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        android.util.Log.e(tag, message, throwable)
        writeToFile("ERROR: $tag: $message \n ${throwable?.stackTraceToString() ?: ""}")
    }

    private fun writeToFile(log: String) {
        context?.let { ctx ->
            try {
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                val logEntry = "$timestamp - $log\n"
                
                // Run on IO thread to avoid blocking UI? 
                // For simplicity in this logger helper, we might do it synchronously or via a simple thread if volume is low.
                // Given the requirement is just "save log", simple append is okay but better be safe.
                java.util.concurrent.Executors.newSingleThreadExecutor().execute {
                    try {
                        val file = java.io.File(ctx.filesDir, logFileName)
                        java.io.FileOutputStream(file, true).use { stream ->
                            stream.write(logEntry.toByteArray())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
