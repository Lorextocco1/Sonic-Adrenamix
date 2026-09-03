sed -i '/if (track.isCompleted) {/i \
                                    val isFav = com.example.ui.playlist.FavoritesManager.isFavorite(track.url)\n\
                                    IconButton(onClick = { com.example.ui.playlist.FavoritesManager.toggleFavorite(track, gameName) }) {\n\
                                        Icon(\n\
                                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,\n\
                                            contentDescription = "Preferito",\n\
                                            tint = NeonOrange,\n\
                                            modifier = Modifier.size(28.dp)\n\
                                        )\n\
                                    }\n\
                                    Spacer(modifier = Modifier.width(4.dp))' app/src/main/java/com/example/ui/playlist/GamePlaylistScreen.kt
