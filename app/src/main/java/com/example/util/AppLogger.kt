package com.example.util

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private const val TAG = "SonicAppLogger"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun log(message: String) {
        writeLog("INFO", message)
    }

    fun logError(message: String, e: Throwable? = null) {
        val errorDetails = e?.let { "\n${it.stackTraceToString()}" } ?: ""
        writeLog("ERROR", "$message$errorDetails")
    }

    private fun writeLog(level: String, message: String) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            
            val logFile = File(downloadsDir, "SonicMusicHub_Log.txt")
            val timestamp = dateFormat.format(Date())
            val logEntry = "[$timestamp] [$level] $message\n"
            
            val writer = FileWriter(logFile, true)
            writer.append(logEntry)
            writer.close()
            
            Log.d(TAG, logEntry)
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to write log to file", ex)
        }
    }
}
