with open('app/src/main/java/com/example/ui/playlist/GamePlaylistScreen.kt', 'r') as f:
    content = f.read()

target = """    val tracks by viewModel.tracks.collectAsState()
    val isExtracting by viewModel.isExtractingInfo.collectAsState()
    val favorites by com.example.ui.playlist.FavoritesManager.favorites.collectAsState()"""

replacement = """    val tracks by viewModel.tracks.collectAsState()
    val isExtracting by viewModel.isExtractingInfo.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val favorites by com.example.ui.playlist.FavoritesManager.favorites.collectAsState()"""

content = content.replace(target, replacement)

target2 = """            } else if (tracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (url.isEmpty()) "Playlist non ancora configurata." else "Nessuna traccia trovata.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            } else {"""

replacement2 = """            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Errore sconosciuto",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else if (tracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (url.isEmpty()) "Playlist non ancora configurata." else "Nessuna traccia trovata.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            } else {"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/playlist/GamePlaylistScreen.kt', 'w') as f:
    f.write(content)
print("Patched GamePlaylistScreen with error message")
