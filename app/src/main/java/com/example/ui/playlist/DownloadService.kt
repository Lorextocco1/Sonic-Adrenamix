package com.example.ui.playlist

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.app.Notification
import java.util.concurrent.ConcurrentHashMap
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.util.AppLogger
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem



class DownloadService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val downloadMutex = kotlinx.coroutines.sync.Mutex()
    private var activeJobCount = 0
    private val completedTracks = mutableListOf<String>()
    private val activeDownloads = ConcurrentHashMap<String, Int>()

    companion object {
        const val CHANNEL_ID = "download_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val gameName = intent?.getStringExtra("gameName") ?: return START_NOT_STICKY
        val indices = intent.getIntArrayExtra("indices") ?: return START_NOT_STICKY

        activeJobCount++
        startForeground(NOTIFICATION_ID, buildNotification("In coda...", "Attesa download precedente", gameName))
        
        serviceScope.launch {
            downloadMutex.withLock {
                try {
                    startForeground(NOTIFICATION_ID, buildNotification("Preparazione download...", "", gameName))
                    handleDownload(gameName, indices)
                } finally {
                    activeJobCount--
                    if (activeJobCount == 0) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Download Playlist",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, text: String, gameName: String, progress: Int = 0): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val inboxStyle = NotificationCompat.InboxStyle()
        completedTracks.takeLast(4).forEach { trackTitle ->
            inboxStyle.addLine("✓ $trackTitle")
        }
        
        var totalProgress = 0
        var activeCount = 0
        // Limit to prevent notification content from becoming too large
        activeDownloads.entries.take(3).forEach { (downloadTitle, downloadProgress) ->
            inboxStyle.addLine("↓ $downloadTitle - $downloadProgress%")
            totalProgress += downloadProgress
            activeCount++
        }
        
        val averageProgress = if (activeCount > 0) totalProgress / activeCount else progress

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download - $gameName")
            .setContentText(if (activeCount > 0) "Scaricamento in corso..." else if (title.isNotEmpty()) title else "Attendere...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(100, averageProgress, activeCount == 0 && progress == 0)
            .setContentIntent(pendingIntent)
            .setStyle(inboxStyle)
            .build()
    }

    private fun updateNotification(gameName: String) {
        val notification = buildNotification("", "", gameName, 0)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }


        private suspend fun handleDownload(gameName: String, indices: IntArray) {
        val destFolder = File(filesDir, "music/$gameName")
        if (!destFolder.exists()) destFolder.mkdirs()

        try {
            val tracks = DownloadManager.tracks.value[gameName] ?: return
            
            for (index in indices) {
                val track = tracks.getOrNull(index) ?: continue
                
                if (track.isCompleted) {
                    if (!completedTracks.contains(track.title)) {
                        completedTracks.add(track.title)
                    }
                    continue
                }
                
                activeDownloads.clear() // since it's sequential
                activeDownloads[track.title] = 0
                updateNotification(gameName)
                downloadTrack(index, track.url, track.title, track.thumbnailUrl, destFolder, gameName)
                completedTracks.add(track.title)
                activeDownloads.remove(track.title)
                updateNotification(gameName)
            }
        } catch (e: Exception) {
            AppLogger.logError("Failed to download tracks for $gameName", e)
        }
    }

    private suspend fun downloadTrack(index: Int, url: String, cleanTitle: String, thumbnailUrl: String, destFolder: File, gameName: String) {
        val maxRetries = 3
        for (attempt in 1..maxRetries) {
            try {
                DownloadManager.updateTrack(gameName, index) { it.copy(isDownloading = true, downloadProgress = 0.05f) }
                
                val service = ServiceList.YouTube
                val extractor = service.getStreamExtractor(url)
                extractor.fetchPage()
                val audioStreams = extractor.audioStreams
                if (audioStreams.isNotEmpty()) {
                    val bestAudio = audioStreams.filter { it.format?.suffix == "m4a" }.maxByOrNull { it.averageBitrate } ?: audioStreams.maxByOrNull { it.averageBitrate } ?: audioStreams.first()
                    val audioUrl = bestAudio.content
                    val ext = bestAudio.format?.suffix ?: "m4a"
                    val safeTitle = cleanTitle.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                    val audioFile = File(destFolder, "${String.format("%02d", index + 1)}_$safeTitle.$ext")
                    val highResThumbnail = try {
                        extractor.thumbnails.maxByOrNull { it.height * it.width }?.url
                            ?: extractor.thumbnails.firstOrNull()?.url
                            ?: thumbnailUrl
                    } catch (e: Exception) {
                        thumbnailUrl
                    }
                    
                    downloadFileWithProgress(audioUrl, audioFile, index, gameName, cleanTitle)
                    
                    val metadataJson = JSONObject().apply {
                        put("title", cleanTitle)
                        put("uploader", extractor.uploaderName ?: "Unknown")
                        put("duration", extractor.length)
                        put("thumbnailUrl", highResThumbnail)
                        put("url", url)
                        put("audioFile", audioFile.name)
                        put("index", index)
                    }
                    val metadataFile = File(destFolder, "${String.format("%02d", index + 1)}_$safeTitle.json")
                    metadataFile.writeText(metadataJson.toString())
                    
                    DownloadManager.updateTrack(gameName, index) {
                        it.copy(
                            isDownloading = false,
                            isCompleted = true,
                            downloadProgress = 1f,
                            uploader = extractor.uploaderName ?: it.uploader,
                            duration = extractor.length,
                            thumbnailUrl = highResThumbnail,
                            audioFile = audioFile.name
                        )
                    }
                }
                break
            } catch (e: Exception) {
                AppLogger.logError("Failed to download track $url, attempt $attempt", e)
                if (attempt == maxRetries) {
                    DownloadManager.updateTrack(gameName, index) { it.copy(isDownloading = false, isCompleted = false) }
                } else {
                    delay(1000)
                }
            }
        }
    }

    private suspend fun downloadFileWithProgress(url: String, file: File, trackIndex: Int, gameName: String, cleanTitle: String) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder().connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS).readTimeout(60, java.util.concurrent.TimeUnit.SECONDS).build()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw java.io.IOException("Unexpected code $response")
                val body = response.body ?: throw java.io.IOException("Empty body")
                val totalBytes = body.contentLength()
                val fos = FileOutputStream(file)
                fos.use { out ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(1024 * 1024)
                        var bytesRead: Int
                        var downloadedBytes = 0L
                        var lastReportTime = 0L
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            out.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastReportTime > 500 || downloadedBytes == totalBytes) {
                                lastReportTime = currentTime
                                val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0.5f
                                DownloadManager.updateTrack(gameName, trackIndex) { it.copy(downloadProgress = progress) }
                                
                                val progressInt = (progress * 100).toInt()
                                if (activeDownloads[cleanTitle] != progressInt) {
                                    activeDownloads[cleanTitle] = progressInt
                                    updateNotification(gameName)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun readOfflineTracks(destFolder: File): Map<String, JSONObject> {
        val localFiles = destFolder.listFiles { f -> f.name.endsWith(".json") }
        val existingJsonMap = mutableMapOf<String, JSONObject>()
        localFiles?.forEach { file ->
            try {
                val json = JSONObject(file.readText())
                val audioFileName = json.optString("audioFile", "")
                if (File(destFolder, audioFileName).exists()) {
                    val trackUrl = json.optString("url", "")
                    val title = json.optString("title", "")
                    if (trackUrl.isNotEmpty()) {
                        existingJsonMap[trackUrl] = json
                    }
                    if (title.isNotEmpty()) {
                        existingJsonMap[title] = json
                    }
                }
            } catch (e: Exception) {
                AppLogger.logError("Error reading local json: ${file.name}", e)
            }
        }
        return existingJsonMap
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
