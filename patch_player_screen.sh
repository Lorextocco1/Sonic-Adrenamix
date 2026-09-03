sed -i '/\/\/ Controls/i \
          Spacer(modifier = Modifier.height(16.dp))\n\
          val isFav = com.example.ui.playlist.FavoritesManager.isFavorite(currentTrack.url)\n\
          IconButton(onClick = {\n\
              val tState = com.example.ui.playlist.TrackState(\n\
                  title = currentTrack.title,\n\
                  uploader = currentTrack.uploader,\n\
                  duration = currentTrack.duration / 1000L,\n\
                  thumbnailUrl = currentTrack.thumbnailUrl,\n\
                  url = currentTrack.url,\n\
                  downloadProgress = 1f,\n\
                  isDownloading = false,\n\
                  isCompleted = true,\n\
                  audioFile = currentTrack.file.name\n\
              )\n\
              val gName = if (gameName == "Favorites") currentTrack.gameName else gameName\n\
              com.example.ui.playlist.FavoritesManager.toggleFavorite(tState, gName)\n\
          }) {\n\
              Icon(\n\
                  imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,\n\
                  contentDescription = "Preferito",\n\
                  tint = NeonOrange,\n\
                  modifier = Modifier.size(32.dp)\n\
              )\n\
          }' app/src/main/java/com/example/MainActivity.kt
