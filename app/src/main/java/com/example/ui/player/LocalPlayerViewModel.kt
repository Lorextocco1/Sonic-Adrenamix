package com.example.ui.player

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.example.util.AppLogger
import com.example.ui.playlist.FavoritesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

data class TrackInfo(
    val title: String,
    val uploader: String,
    val duration: Long,
    val thumbnailUrl: String,
    val file: File,
    val index: Int = 0,
    val gameName: String = "",
    val url: String = ""
)

class LocalPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _tracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    val tracks: StateFlow<List<TrackInfo>> = _tracks

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    init {
        initializeController()
        startProgressUpdate()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                mediaController = controllerFuture?.get()
                mediaController?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        super.onMediaItemTransition(mediaItem, reason)
                        mediaItem?.localConfiguration?.uri?.let { uri ->
                            val path = uri.path
                            val index = _tracks.value.indexOfFirst { it.file.absolutePath == path }
                            if (index >= 0) {
                                _currentTrackIndex.value = index
                            }
                        }
                    }
                })
            },
            ContextCompat.getMainExecutor(getApplication())
        )
    }

    fun loadFolder(folderPath: String, startIndex: Int = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            val loadedTracks = if (folderPath == "music/Favorites") {
                FavoritesManager.favorites.value.mapIndexedNotNull { i, fav ->
                    val rootMusicDir = File(getApplication<Application>().filesDir, "music/${fav.gameName}")
                    val audioFile = File(rootMusicDir, fav.audioFile)
                    if (audioFile.exists()) {
                        TrackInfo(
                            title = fav.title,
                            uploader = fav.uploader,
                            duration = fav.duration,
                            thumbnailUrl = fav.thumbnailUrl,
                            file = audioFile,
                            index = i,
                            gameName = fav.gameName,
                            url = fav.url
                        )
                    } else null
                }
            } else {
                val rootMusicDir = File(getApplication<Application>().filesDir, folderPath)
                if (rootMusicDir.exists() && rootMusicDir.isDirectory) {
                    val files = rootMusicDir.listFiles { f -> f.name.endsWith(".json") }
                    files?.mapNotNull { file ->
                        try {
                            val json = JSONObject(file.readText())
                            val audioFileName = json.optString("audioFile", "")
                            val audioFile = File(rootMusicDir, audioFileName)
                            if (audioFile.exists()) {
                                TrackInfo(
                                    title = json.optString("title", "Unknown"),
                                    uploader = json.optString("uploader", "Unknown"),
                                    duration = json.optLong("duration", 0L) * 1000L,
                                    thumbnailUrl = json.optString("thumbnailUrl", ""),
                                    file = audioFile,
                                    index = json.optInt("index", 0),
                                    gameName = folderPath.substringAfter("music/"),
                                    url = json.optString("url", "")
                                )
                            } else null
                        } catch (e: Exception) { null }
                    }?.sortedWith(compareBy({ it.index }, { it.title })) ?: emptyList()
                } else {
                    AppLogger.log("Directory non trovata: $folderPath")
                    emptyList()
                }
            }

            withContext(Dispatchers.Main) {
                _tracks.value = loadedTracks
                if (loadedTracks.isNotEmpty()) {
                    val trackToPlay = loadedTracks.indexOfFirst { it.index == startIndex }.takeIf { it >= 0 }
                        ?: (if (startIndex in loadedTracks.indices) startIndex else 0)
                    
                    if (mediaController == null) {
                        viewModelScope.launch {
                            while (mediaController == null) {
                                delay(100)
                            }
                            setupPlaylistAndPlay(trackToPlay)
                        }
                    } else {
                        setupPlaylistAndPlay(trackToPlay)
                    }
                }
            }
        }
    }

    private fun setupPlaylistAndPlay(startIndex: Int) {
        val controller = mediaController ?: return
        val mediaItems = _tracks.value.map { track ->
            MediaItem.Builder()
                .setUri(Uri.fromFile(track.file))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.uploader)
                        .setArtworkUri(Uri.parse(track.thumbnailUrl))
                        .build()
                )
                .build()
        }
        controller.setMediaItems(mediaItems, startIndex, 0)
        controller.prepare()
        controller.repeatMode = Player.REPEAT_MODE_ALL
        controller.play()
        _currentTrackIndex.value = startIndex
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }
    
    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun playNext() {
        if (mediaController?.hasNextMediaItem() == true) {
            mediaController?.seekToNextMediaItem()
        } else {
            setupPlaylistAndPlay(0)
        }
    }

    fun playPrevious() {
        if (mediaController?.hasPreviousMediaItem() == true) {
            mediaController?.seekToPreviousMediaItem()
        } else {
            val last = _tracks.value.size - 1
            if (last >= 0) setupPlaylistAndPlay(last)
        }
    }
    
    private fun startProgressUpdate() {
        viewModelScope.launch(Dispatchers.Main) {
            while(true) {
                try {
                    if (mediaController?.isPlaying == true) {
                        _currentPosition.value = mediaController?.currentPosition ?: 0L
                    }
                } catch(e: Exception) {}
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
