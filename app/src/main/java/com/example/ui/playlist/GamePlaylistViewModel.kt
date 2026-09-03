package com.example.ui.playlist

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

data class TrackState(
    val title: String,
    val uploader: String,
    val duration: Long,
    val thumbnailUrl: String,
    val url: String,
    val downloadProgress: Float, // 0 to 1
    val isDownloading: Boolean,
    val isCompleted: Boolean,
    val audioFile: String = "",
    val index: Int = 0
)

class GamePlaylistViewModel(application: Application) : AndroidViewModel(application) {
    private val currentGameName = MutableStateFlow("")

    val tracks: StateFlow<List<TrackState>> = combine(DownloadManager.tracks, currentGameName) { map, game ->
        map[game] ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isExtractingInfo: StateFlow<Boolean> = combine(DownloadManager.isExtracting, currentGameName) { map, game ->
        map[game] ?: false
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun loadOrDownload(gameName: String, rawUrl: String) {
        currentGameName.value = gameName

        val destFolder = File(getApplication<Application>().filesDir, "music/$gameName")
        if (!destFolder.exists()) destFolder.mkdirs()

        viewModelScope.launch(Dispatchers.IO) {
            val localFiles = destFolder.listFiles { f -> f.name.endsWith(".json") }
            val existingJsonMap = mutableMapOf<String, JSONObject>()
            localFiles?.forEach { file ->
                try {
                    val json = JSONObject(file.readText())
                    val audioFileName = json.optString("audioFile", "")
                    if (File(destFolder, audioFileName).exists()) {
                        existingJsonMap[json.optString("url", "")] = json
                        existingJsonMap[json.optString("title", "")] = json
                    }
                } catch (e: Exception) {
                    AppLogger.logError("Error reading local json: ${file.name}", e)
                }
            }
            
            val offlineTracks = existingJsonMap.values.map { json ->
                TrackState(
                    title = json.optString("title", "Unknown"),
                    uploader = json.optString("uploader", "Unknown"),
                    duration = json.optLong("duration", 0L),
                    thumbnailUrl = json.optString("thumbnailUrl", ""),
                    url = json.optString("url", ""),
                    downloadProgress = 1f,
                    isDownloading = false,
                    isCompleted = true,
                    audioFile = json.optString("audioFile", ""),
                    index = json.optInt("index", 0)
                )
            }.sortedBy { it.index }.distinctBy { it.url }
            
            // Only update if we don't already have tracks from DownloadManager
            if (DownloadManager.tracks.value[gameName].isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    DownloadManager.updateTracks(gameName, offlineTracks)
                }
            }

            if (rawUrl.isEmpty()) return@launch

            DownloadManager.setExtracting(gameName, true)

            // Start Foreground Service to handle online extraction and download
            val intent = Intent(getApplication(), DownloadService::class.java).apply {
                putExtra("gameName", gameName)
                putExtra("url", rawUrl)
            }
            ContextCompat.startForegroundService(getApplication(), intent)
        }
    }
}
