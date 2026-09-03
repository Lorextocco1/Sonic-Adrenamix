with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "fun PlayerScreen(navController: NavController? = null, gameName: String) {" in line:
        start_idx = i
    if "import androidx.lifecycle.viewmodel.compose.viewModel" in line:
        has_import = True
        
import_str = "import androidx.lifecycle.viewmodel.compose.viewModel\nimport androidx.compose.runtime.LaunchedEffect\nimport androidx.compose.runtime.collectAsState\nimport androidx.compose.material.icons.filled.Pause\nimport androidx.compose.material.icons.filled.SkipNext\nimport androidx.compose.material.icons.filled.SkipPrevious\n"

# I'll just write a whole new PlayerScreen string
new_player_screen = """
@Composable
fun PlayerScreen(navController: NavController? = null, gameName: String, viewModel: com.example.ui.player.LocalPlayerViewModel = viewModel()) {
  val currentSong by viewModel.currentSongName.collectAsState()
  val isPlaying by viewModel.isPlaying.collectAsState()

  LaunchedEffect(gameName) {
      viewModel.loadFolder("music/$gameName")
  }

  Box(modifier = Modifier.fillMaxSize()) {
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
      
      Spacer(modifier = Modifier.height(48.dp))
      
      // Artwork
      val gameMedia = ALL_GAMES.find { it.name == gameName }
      Box(
        modifier = Modifier
          .size(250.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.4f))
          .border(2.dp, SonicGold, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        if (gameMedia != null) {
            Image(
                painter = painterResource(id = gameMedia.imageResId),
                contentDescription = "Artwork",
                modifier = Modifier.size(180.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Play Placeholder",
              tint = SonicGold.copy(alpha = 0.5f),
              modifier = Modifier.size(80.dp)
            )
        }
      }
      
      Spacer(modifier = Modifier.height(32.dp))
      
      Text(
        text = currentSong,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
      
      Text(
        text = gameName,
        style = MaterialTheme.typography.titleMedium,
        color = Color.LightGray,
        modifier = Modifier.padding(top = 8.dp)
      )
      
      Spacer(modifier = Modifier.weight(1f))
      
      // Playback Controls
      androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { viewModel.previous() },
          modifier = Modifier.size(64.dp)
        ) {
          Icon(
            imageVector = Icons.Default.SkipPrevious,
            contentDescription = "Previous",
            tint = Color.White,
            modifier = Modifier.size(48.dp)
          )
        }
        
        FloatingActionButton(
          onClick = { viewModel.togglePlayPause() },
          containerColor = NeonOrange,
          contentColor = Color.White,
          modifier = Modifier.size(80.dp),
          shape = CircleShape
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = "Play/Pause",
            modifier = Modifier.size(48.dp)
          )
        }
        
        IconButton(
          onClick = { viewModel.next() },
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
"""

# replace the old PlayerScreen from start_idx to EOF (assuming it's the last function in file)
# actually let's just find the closing brace of PlayerScreen.
# Wait, it's safer to just split string.
lines_before = lines[:start_idx-1]  # -1 for @Composable

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    # write imports first line
    f.write(import_str)
    f.writelines(lines_before)
    f.write(new_player_screen)
