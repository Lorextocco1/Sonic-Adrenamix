package com.example.ui.playlist

import kotlinx.coroutines.flow.MutableStateFlow

object DownloadManager {
    val tracks = MutableStateFlow<Map<String, List<TrackState>>>(emptyMap())
    val isExtracting = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    fun updateTracks(gameName: String, newTracks: List<TrackState>) {
        val currentMap = tracks.value.toMutableMap()
        currentMap[gameName] = newTracks
        tracks.value = currentMap
    }
    
    fun updateTrack(gameName: String, index: Int, update: (TrackState) -> TrackState) {
        val currentMap = tracks.value.toMutableMap()
        val list = currentMap[gameName]?.toMutableList() ?: return
        if (index in list.indices) {
            list[index] = update(list[index])
            currentMap[gameName] = list
            tracks.value = currentMap
        }
    }
    
    fun setExtracting(gameName: String, extracting: Boolean) {
        val currentMap = isExtracting.value.toMutableMap()
        currentMap[gameName] = extracting
        isExtracting.value = currentMap
    }
}
