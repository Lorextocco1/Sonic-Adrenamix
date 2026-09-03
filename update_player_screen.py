with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# First replace the whole PlayerScreen
import re
new_player_screen = """
@Composable
fun PlayerScreen(navController: NavController? = null, gameName: String, startIndex: Int = 0, viewModel: com.example.ui.player.LocalPlayerViewModel = viewModel()) {
  val tracks by viewModel.tracks.collectAsState()
  val currentIndex by viewModel.currentTrackIndex.collectAsState()
  val isPlaying by viewModel.isPlaying.collectAsState()
  val currentPosition by viewModel.currentPosition.collectAsState()

  LaunchedEffect(gameName) {
    viewModel.loadFolder("music/$gameName", startIndex)
  }

  SonicBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header with back button
      Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
          onClick = { navController?.popBackStack() },
          modifier = Modifier.align(Alignment.CenterStart)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
          )
        }
        Text(
          text = "MUSIC PLAYER",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = SonicGold,
          modifier = Modifier.align(Alignment.Center)
        )
      }
      
      Spacer(modifier = Modifier.height(32.dp))
      
      if (tracks.isEmpty()) {
          Text("Caricamento brano...", color = Color.White)
      } else {
          val currentTrack = tracks[currentIndex]
          
          Text(
              text = currentTrack.title,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 2,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              modifier = Modifier.padding(horizontal = 16.dp)
          )
          
          Spacer(modifier = Modifier.height(16.dp))
          
          // Artwork (Thumbnail via Coil)
          Box(
            modifier = Modifier
              .size(250.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.4f))
              .border(2.dp, SonicGold, CircleShape),
            contentAlignment = Alignment.Center
          ) {
              if (currentTrack.thumbnailUrl.isNotEmpty()) {
                  coil.compose.AsyncImage(
                      model = currentTrack.thumbnailUrl,
                      contentDescription = "Artwork",
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize()
                  )
              } else {
                  val fallbackGame = ALL_GAMES.find { it.name == gameName }
                  if (fallbackGame != null) {
                      Image(
                          painter = painterResource(id = fallbackGame.imageResId),
                          contentDescription = "Artwork",
                          modifier = Modifier.size(180.dp),
                          contentScale = ContentScale.Fit
                      )
                  }
              }
          }
          
          Spacer(modifier = Modifier.height(16.dp))
          
          Text(
              text = currentTrack.uploader,
              style = MaterialTheme.typography.titleMedium,
              color = Color.Gray,
              maxLines = 1,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          
          Spacer(modifier = Modifier.weight(1f))
          
          // Progress bar
          val progress = if (currentTrack.duration > 0) currentPosition.toFloat() / currentTrack.duration.toFloat() else 0f
          Slider(
              value = progress.coerceIn(0f, 1f),
              onValueChange = { newProgress ->
                  viewModel.seekTo((newProgress * currentTrack.duration).toLong())
              },
              colors = SliderDefaults.colors(
                  thumbColor = SonicGold,
                  activeTrackColor = SonicGold,
                  inactiveTrackColor = Color.Gray
              ),
              modifier = Modifier.fillMaxWidth()
          )
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
          ) {
              Text(formatTime(currentPosition), color = Color.White, style = MaterialTheme.typography.bodySmall)
              Text(formatTime(currentTrack.duration), color = Color.White, style = MaterialTheme.typography.bodySmall)
          }
          
          Spacer(modifier = Modifier.height(16.dp))
          
          // Controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = { viewModel.playPrevious() },
              modifier = Modifier.size(64.dp)
            ) {
              Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
              )
            }
            
            Button(
              onClick = { viewModel.togglePlayPause() },
              shape = CircleShape,
              colors = ButtonDefaults.buttonColors(containerColor = SonicGold),
              modifier = Modifier.size(80.dp),
              contentPadding = PaddingValues(0.dp)
            ) {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
              )
            }
            
            IconButton(
              onClick = { viewModel.playNext() },
              modifier = Modifier.size(64.dp)
            ) {
              Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
              )
            }
          }
          
          Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}
"""

start_idx = -1
end_idx = -1
lines = content.splitlines(True)
for i, line in enumerate(lines):
    if line.startswith("fun PlayerScreen("):
        start_idx = i
        break

if start_idx != -1:
    open_braces = 0
    found_first = False
    for i in range(start_idx, len(lines)):
        line = lines[i]
        open_braces += line.count('{')
        open_braces -= line.count('}')
        if open_braces > 0:
            found_first = True
        if found_first and open_braces == 0:
            end_idx = i
            break

    if end_idx != -1:
        new_lines = lines[:start_idx] + new_player_screen.splitlines(True) + lines[end_idx+1:]
        
        with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
            f.writelines(new_lines)
            
