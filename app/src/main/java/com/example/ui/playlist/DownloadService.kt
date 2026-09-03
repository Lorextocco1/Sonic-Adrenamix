package com.example.ui.playlist

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.MainActivity
import com.example.util.AppLogger
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.io.File
import java.io.FileOutputStream

class DownloadService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isDownloading = false
    private val completedTracks = mutableListOf<String>()
    
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
        val url = intent.getStringExtra("url") ?: return START_NOT_STICKY

        if (!isDownloading) {
            isDownloading = true
            startForeground(NOTIFICATION_ID, buildNotification("Preparazione download...", "", gameName))
            serviceScope.launch {
                try {
                    handleDownload(gameName, url)
                } finally {
                    isDownloading = false
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
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
        if (title.isNotEmpty() && progress > 0) {
            inboxStyle.addLine("↓ $title - $progress%")
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download - $gameName")
            .setContentText(if (title.isNotEmpty()) "$title ($progress%)" else text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(100, progress, progress == 0)
            .setContentIntent(pendingIntent)
            .setStyle(inboxStyle)
            .build()
    }

    private fun updateNotification(title: String, gameName: String, progress: Int) {
        val notification = buildNotification(title, "", gameName, progress)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private suspend fun handleDownload(gameName: String, rawUrl: String) {
        val destFolder = File(filesDir, "music/$gameName")
        if (!destFolder.exists()) destFolder.mkdirs()

        val url = rawUrl.replace("https://youtube.com/", "https://www.youtube.com/")
            .replace("https://m.youtube.com/", "https://www.youtube.com/")
            .replace("https://youtu.be/", "https://www.youtube.com/watch?v=")

        val existingJsonMap = readOfflineTracks(destFolder)
        
        try {
            DownloadManager.setExtracting(gameName, true)
            val service = ServiceList.YouTube
            val extractor = service.getPlaylistExtractor(url)
            extractor.fetchPage()
            val allItems = mutableListOf<StreamInfoItem>()
            allItems.addAll(extractor.initialPage.items)
            var currentPage = extractor.initialPage
            while (currentPage.hasNextPage()) {
                currentPage = extractor.getPage(currentPage.nextPage)
                allItems.addAll(currentPage.items)
            }

            val initialTrackStates = allItems.mapIndexed { index, item ->
                val rawName = item.name ?: "Track ${index + 1}"
                val cleanTitle = rawName
                    .replace(Regex("^\\s*\\d+[\\.\\-\\_\\s]+"), "")
                    .replace(Regex("\\[OST\\]", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("- Sonic the Hedgehog", RegexOption.IGNORE_CASE), "")
                    .trim()
                    .ifEmpty { rawName }
                val bestThumbnail = item.thumbnails.maxByOrNull { it.height * it.width }?.url
                    ?: item.thumbnails.firstOrNull()?.url
                    ?: ""
                val existing = existingJsonMap[item.url] ?: existingJsonMap[cleanTitle]
                val isAlreadyDownloaded = existing != null
                TrackState(
                    title = cleanTitle,
                    uploader = item.uploaderName ?: "Sega / DeoxysPrime",
                    duration = item.duration,
                    thumbnailUrl = bestThumbnail,
                    url = item.url,
                    downloadProgress = if (isAlreadyDownloaded) 1f else 0f,
                    isDownloading = false,
                    isCompleted = isAlreadyDownloaded,
                    index = index
                )
            }
            
            DownloadManager.updateTracks(gameName, initialTrackStates)
            DownloadManager.setExtracting(gameName, false)

            for ((index, item) in allItems.withIndex()) {
                val tracks = DownloadManager.tracks.value[gameName] ?: continue
                val track = tracks.getOrNull(index) ?: continue
                if (track.isCompleted) {
                    if (!completedTracks.contains(track.title)) {
                        completedTracks.add(track.title)
                    }
                    continue
                }
                downloadTrack(index, item.url, track.title, track.thumbnailUrl, destFolder, gameName)
                completedTracks.add(track.title)
                updateNotification("", gameName, 0)
            }
        } catch (e: Exception) {
            AppLogger.logError("Failed to extract playlist for $gameName", e)
            DownloadManager.setExtracting(gameName, false)
        }
    }

    private suspend fun downloadTrack(index: Int, url: String, cleanTitle: String, thumbnailUrl: String, destFolder: File, gameName: String) {
        try {
            DownloadManager.updateTrack(gameName, index) { it.copy(isDownloading = true, downloadProgress = 0.05f) }
            updateNotification(cleanTitle, gameName, 5)
            
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
        } catch (e: Exception) {
            AppLogger.logError("Failed to download track $url", e)
            DownloadManager.updateTrack(gameName, index) { it.copy(isDownloading = false, isCompleted = false) }
        }
    }

    private suspend fun downloadFileWithProgress(url: String, file: File, trackIndex: Int, gameName: String, cleanTitle: String) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use
                val body = response.body ?: return@use
                val totalBytes = body.contentLength()
                val fos = FileOutputStream(file)
                fos.use { out ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(8192)
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
                                updateNotification(cleanTitle, gameName, (progress * 100).toInt())
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
