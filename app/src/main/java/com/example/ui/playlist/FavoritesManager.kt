package com.example.ui.playlist

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class FavoriteTrack(
    val gameName: String,
    val url: String,
    val title: String,
    val uploader: String,
    val duration: Long,
    val thumbnailUrl: String,
    val audioFile: String
)

object FavoritesManager {
    private const val PREFS_NAME = "sonic_music_favorites"
    private const val KEY_FAVORITES = "favorites_json"

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(Array<FavoriteTrack>::class.java)

    private val _favorites = MutableStateFlow<List<FavoriteTrack>>(emptyList())
    val favorites: StateFlow<List<FavoriteTrack>> = _favorites

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadFavorites()
        }
    }

    private fun loadFavorites() {
        val json = prefs?.getString(KEY_FAVORITES, null)
        if (json != null) {
            try {
                val array = adapter.fromJson(json)
                if (array != null) {
                    _favorites.value = array.toList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isFavorite(url: String): Boolean {
        return _favorites.value.any { it.url == url }
    }

    fun toggleFavorite(track: TrackState, gameName: String) {
        val current = _favorites.value.toMutableList()
        val index = current.indexOfFirst { it.url == track.url }
        if (index >= 0) {
            current.removeAt(index)
        } else {
            current.add(FavoriteTrack(
                gameName = gameName,
                url = track.url,
                title = track.title,
                uploader = track.uploader,
                duration = track.duration,
                thumbnailUrl = track.thumbnailUrl,
                audioFile = track.audioFile
            ))
        }
        _favorites.value = current
        saveFavorites(current)
    }

    private fun saveFavorites(list: List<FavoriteTrack>) {
        val json = adapter.toJson(list.toTypedArray())
        prefs?.edit()?.putString(KEY_FAVORITES, json)?.apply()
    }
}
