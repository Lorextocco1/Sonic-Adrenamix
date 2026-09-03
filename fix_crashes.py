import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Fix GamesListScreen navigation
content = re.sub(
    r'navController\?\.navigate\("playlist/\$\{game\.name\}"\)',
    r'navController?.navigate("playlist/${java.net.URLEncoder.encode(game.name, "UTF-8")}")',
    content
)

# Fix PlayerScreen currentTrack crash
old_player_logic = """      if (tracks.isEmpty()) {
          Text("Caricamento brano...", color = Color.White)
      } else {
          val currentTrack = tracks[currentIndex]"""

new_player_logic = """      if (tracks.isEmpty()) {
          Text("Caricamento brano...", color = Color.White)
      } else {
          val currentTrack = tracks.getOrNull(currentIndex)
          if (currentTrack != null) {"""
content = content.replace(old_player_logic, new_player_logic)

# Add closing brace for the if (currentTrack != null) block
old_player_end = """          Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}"""
new_player_end = """          Spacer(modifier = Modifier.height(32.dp))
          }
      }
    }
  }
}"""
content = content.replace(old_player_end, new_player_end)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/playlist/GamePlaylistScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r'navController\?\.navigate\("player/\$gameName/\$index"\)',
    r'navController?.navigate("player/${java.net.URLEncoder.encode(gameName, "UTF-8")}/$index")',
    content
)

with open('app/src/main/java/com/example/ui/playlist/GamePlaylistScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/player/LocalPlayerViewModel.kt', 'r') as f:
    content = f.read()

old_seek = """    fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
        _currentPosition.value = positionMs
    }"""
new_seek = """    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
            _currentPosition.value = positionMs
        } catch(e: Exception) {}
    }"""
content = content.replace(old_seek, new_seek)

old_progress = """    private fun startProgressUpdate() {
        viewModelScope.launch(Dispatchers.Main) {
            while(true) {
                if (_isPlaying.value && mediaPlayer != null) {
                    _currentPosition.value = mediaPlayer!!.currentPosition.toLong()
                }
                delay(1000)
            }
        }
    }"""
new_progress = """    private fun startProgressUpdate() {
        viewModelScope.launch(Dispatchers.Main) {
            while(true) {
                try {
                    if (_isPlaying.value && mediaPlayer != null) {
                        _currentPosition.value = mediaPlayer?.currentPosition?.toLong() ?: 0L
                    }
                } catch(e: Exception) {}
                delay(1000)
            }
        }
    }"""
content = content.replace(old_progress, new_progress)

with open('app/src/main/java/com/example/ui/player/LocalPlayerViewModel.kt', 'w') as f:
    f.write(content)

