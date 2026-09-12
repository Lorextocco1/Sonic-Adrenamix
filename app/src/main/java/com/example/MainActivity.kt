package com.example
import androidx.compose.material.icons.filled.Favorite

import androidx.compose.material.icons.filled.FavoriteBorder

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.SegaBlue
import com.example.ui.theme.SonicGold
import com.example.ui.theme.SonicGoldLight

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ColorFilter

import org.schabi.newpipe.extractor.NewPipe
import com.example.newpipe.DownloaderImpl

import com.example.util.AppLogger

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
    }
    super.onCreate(savedInstanceState)
    AppLogger.log("MainActivity onCreate - App Started")
    com.example.ui.playlist.FavoritesManager.init(this)
    try {
        NewPipe.init(DownloaderImpl.getInstance())
        AppLogger.log("NewPipe Extractor successfully initialized")
    } catch (e: Exception) {
        AppLogger.logError("Failed to initialize NewPipe Extractor", e)
        e.printStackTrace()
    }

    // Initialize Music Directory Tree
    try {
        val rootMusicDir = java.io.File(filesDir, "music")
        if (!rootMusicDir.exists()) {
            rootMusicDir.mkdirs()
            AppLogger.log("Created root music directory at ${rootMusicDir.absolutePath}")
        }
        
        ALL_GAMES.forEach { game ->
            val gameDir = java.io.File(rootMusicDir, game.name)
            if (!gameDir.exists()) {
                gameDir.mkdirs()
            }
        }
        AppLogger.log("Music directory tree successfully verified/created for ${ALL_GAMES.size} playlists.")
    } catch (e: Exception) {
        AppLogger.logError("Failed to create music directories", e)
    }
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        SonicBackground {
          Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
          ) { innerPadding ->
            AppNavigation(modifier = Modifier.padding(innerPadding))
          }
        }
      }
    }
  }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
  val navController = rememberNavController()
  NavHost(
    navController = navController, 
    startDestination = "home", 
    modifier = modifier,
    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) },
    popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) },
    popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) }
  ) {
    composable("home") {
      SonicHomeScreen(navController = navController)
    }
    
    
    composable("favorites") {

      com.example.ui.playlist.FavoritesScreen(navController = navController)

    }


    composable("games_list") {
      GamesListScreen(navController = navController)
    }

    composable("playlist/{gameName}") { backStackEntry ->
      val rawGameName = backStackEntry.arguments?.getString("gameName") ?: "Unknown"
      val gameName = java.net.URLDecoder.decode(rawGameName, "UTF-8")
      val url = when (gameName) {
          "Sonic the Hedgehog" -> "https://www.youtube.com/playlist?list=PLvNp0Boas720BYHiEHd-zM942KP_bCSZ4"
          "Sonic CD" -> "https://www.youtube.com/playlist?list=PLvNp0Boas7205K42TtDtdUCCrtkD-X0id"
          "Sonic the Hedgehog 2" -> "https://www.youtube.com/playlist?list=PLvNp0Boas721Im_ZBhvDEuCqzLsRvlKb3"
          "Sonic 3 & Knuckles" -> "https://www.youtube.com/playlist?list=PLvNp0Boas721ZhOX_U084B4Jrxdr8rJOj"
          "Knuckles' Chaotix" -> "https://www.youtube.com/playlist?list=PLvNp0Boas720u1qnpxmeBe8jARE-4Oghz"
          "Sonic Triple Trouble" -> "https://www.youtube.com/playlist?list=PLvNp0Boas723CXsLsUaRrgREn_miwCDvm"
          "Sonic 3D Blast" -> "https://www.youtube.com/playlist?list=PLvNp0Boas721tFGuK6G3GZEJqCXj_Q5fb"
          "Tails Adventure" -> "https://www.youtube.com/playlist?list=PLqzVNX9DhzKHoqUSD6RSK-p_t1BxevVKw"
          "Sonic R" -> "https://www.youtube.com/playlist?list=PLvNp0Boas722QWsRs25RbQhfdW3_DmRNB"
          "Sonic Advance" -> "https://www.youtube.com/playlist?list=PLvNp0Boas722w9ee2EQOxiA1UDzqHPTv_"
          "Sonic Advance 2" -> "https://www.youtube.com/playlist?list=PLvNp0Boas722cdUDI0BZPTVMNOP7knTE8"
          "Sonic Advance 3" -> "https://www.youtube.com/playlist?list=PLvNp0Boas720ZN-PS7Y7qB4fbKofBG_So"
          "Sonic Battle" -> "https://www.youtube.com/playlist?list=PLvNp0Boas720LRk7SZ0F0rRG-CVM0P0jc"
          "Sonic Adventure" -> "https://www.youtube.com/playlist?list=PLvNp0Boas722L8qX0VsBhaI8s0a9k4Umr"
          "Sonic Adventure 2 Battle" -> "https://www.youtube.com/playlist?list=PLvNp0Boas721TSB-gLzJt6vOmTdRsX47x"
          "Sonic Heroes" -> "https://www.youtube.com/playlist?list=PLvNp0Boas722QsWeHHrKSw5IfX-5tEX_0"
          "Shadow the Hedgehog" -> "https://www.youtube.com/playlist?list=PLvNp0Boas7217wlSWYj25AHEB-oYZ_Q-T"
          "Sonic Riders" -> "https://www.youtube.com/playlist?list=PLvNp0Boas721u-YU-P1whyTqVAskR2B9Q"
          "Sonic Riders Zero Gravity" -> "https://www.youtube.com/playlist?list=PLvNp0Boas72392cQ64bjpJhH7NRYHrKUq"
          "Sonic Free Riders" -> "https://www.youtube.com/playlist?list=PLvNp0Boas721fNWYsgdVTvmmf0-FQ_6QV"
          "Sonic 06" -> "https://www.youtube.com/playlist?list=PLvNp0Boas723k0aBhOnIBp1cYAp8S89i7"
          "Sonic Rush" -> "https://www.youtube.com/playlist?list=PLvNp0Boas721Cm9CWT9eaSq_JxA3f_NAr"
          "Sonic Rush Adventure" -> "https://www.youtube.com/playlist?list=PLvNp0Boas720tj1lWYIOohCHSPR13BICL"
          "Sonic Rivals" -> "https://www.youtube.com/playlist?list=PLnZc6WBQn4V_tlhZ457Pp0Vl5jw4yHiKF"
          "Sonic Rivals 2" -> "https://www.youtube.com/playlist?list=PLnZc6WBQn4V_p7iGtiaHxARgMEq1OwHZ7"
          "Sonic and the Secret Rings" -> "https://www.youtube.com/playlist?list=PLvNp0Boas723LUgVEMKg3TbvrivZgi4zi"
          "Sonic and the Black Knight" -> "https://www.youtube.com/playlist?list=PLvNp0Boas721Xb1rZMrxR_NHEKRpO5VNf"
          "Sonic Colors" -> "https://www.youtube.com/playlist?list=PLvNp0Boas721kdXyVrMyRCaDIRWSSQQgq"
          "Sonic Colors DS" -> "https://www.youtube.com/playlist?list=PLqzVNX9DhzKFywTVKUu72XNEzhB-L4s4Y"
          "Sonic Colors Ultimate" -> "https://www.youtube.com/playlist?list=PLvNp0Boas720R_z-lr1hiooDly03Q5Z6f"
          "Sonic Unleashed" -> "https://www.youtube.com/playlist?list=PLvNp0Boas723dWdYd0lLxuIOXkf3SCdxO"
          "Sonic Generations" -> "https://www.youtube.com/playlist?list=PL1A538347CCEAFE66"
          "Sonic Generations 3DS" -> "https://www.youtube.com/playlist?list=PL3FCDE9C63F13F2C5"
          "Sonic X Shadow Generations" -> "https://www.youtube.com/playlist?list=PLEh6IVB1iyiVIoGQAqDJlvDc-9FwbmsmA"
          "Sonic Lost World" -> "https://www.youtube.com/playlist?list=PLvNp0Boas722J0cH3q5Fp8N7F14eUW-BO"
          "Sonic Forces" -> "https://www.youtube.com/playlist?list=PLvNp0Boas722DbfR8d53LHrlicOuItJ-j"
          "Sonic Frontiers" -> "https://www.youtube.com/playlist?list=PLvNp0Boas720kf8hWiCDhdNqsuCYhkSKY"
          "Sonic Frontiers Final Horizon" -> "https://www.youtube.com/playlist?list=PLRmdnaAXIjrU2O99B5hpFdhaM5EEq36X8"
          "Sonic Mania" -> "https://www.youtube.com/playlist?list=PLvNp0Boas722ht2VkVNjVDtDvEf7k8noc"
          "Sonic the Hedgehog 4" -> "https://www.youtube.com/playlist?list=PLvNp0Boas722geGGIVi5QDJ9i75ngfId9"
          "Sonic Dash" -> "https://www.youtube.com/playlist?list=PLRRIJSofhX72ASunZC9vbaU_D6Sp_8_sQ"
          "Sonic Dash 2: Sonic Boom" -> "https://www.youtube.com/playlist?list=PLRRIJSofhX70JfCtm2AaaVkESqsHlNC-J"
          "Sonic Forces Speed Battle" -> "https://www.youtube.com/playlist?list=PLv9-ALgo60Tmjjs5vURtzsOxCLjD_CeMO"
          "Sonic Runners" -> "https://www.youtube.com/playlist?list=PLvNp0Boas722auG_u6BH2_yEl4eEZY4NV"
          "Sonic Dream Team" -> "https://www.youtube.com/playlist?list=PLacrXFZ0srRrGsEC-1MC74VQLamBz4WrJ"
          "Sonic & SEGA All-Stars Racing" -> "https://www.youtube.com/playlist?list=PLQBB4jGIqkAxg1WnXsAI2lbi-DdjIzl8J"
          "Sonic & All-Stars Racing Transformed" -> "https://www.youtube.com/playlist?list=PLvNp0Boas723XRrqBDcDlEavXl0TB30Da"
          "Team Sonic Racing" -> "https://www.youtube.com/playlist?list=PLvNp0Boas720gZrmw1smzrlIZCwL20JCu"
          "Sonic Racing CrossWorlds" -> "https://www.youtube.com/playlist?list=PLFZsqTq_ko81kYdjeo5WjgkOSrkbMuX2C"
          else -> ""
      }
      com.example.ui.playlist.GamePlaylistScreen(navController = navController, gameName = gameName, url = url)
    }
    composable("player/{gameName}/{index}") { backStackEntry ->
      val rawGameName = backStackEntry.arguments?.getString("gameName") ?: "Unknown"
      val gameName = java.net.URLDecoder.decode(rawGameName, "UTF-8")
      val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
      PlayerScreen(navController = navController, gameName = gameName, startIndex = index)
    }
  }
}

@Composable
fun SonicBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
  val infiniteTransition = rememberInfiniteTransition(label = "Background Animations")
  
  // Twinkling phases for stars (irregular intervals)
  val twinkle1 by infiniteTransition.animateFloat(
      initialValue = 0.2f, targetValue = 1f,
      animationSpec = infiniteRepeatable(animation = tween(1200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
      label = "Twinkle 1"
  )
  val twinkle2 by infiniteTransition.animateFloat(
      initialValue = 0.3f, targetValue = 1f,
      animationSpec = infiniteRepeatable(animation = tween(1800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
      label = "Twinkle 2"
  )
  val twinkle3 by infiniteTransition.animateFloat(
      initialValue = 0.1f, targetValue = 1f,
      animationSpec = infiniteRepeatable(animation = tween(2500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
      label = "Twinkle 3"
  )
  
  // Ring entry/exit animation (moving diagonally in and out of the corners)
  val ringOffsetAnim by infiniteTransition.animateFloat(
      initialValue = 20f, targetValue = 50f,
      animationSpec = infiniteRepeatable(animation = tween(6000, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
      label = "Ring Entry Exit"
  )
  
  // Golden Mist pulsing alpha
  val mistAlpha by infiniteTransition.animateFloat(
      initialValue = 0.02f, targetValue = 0.06f,
      animationSpec = infiniteRepeatable(animation = tween(8000, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
      label = "Mist Alpha"
  )
  
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(SegaBlue) // The classic Sonic Blue
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
       val canvasWidth = size.width
       val canvasHeight = size.height
       
       // Golden Nebula
       drawCircle(
           brush = Brush.radialGradient(colors = listOf(Color(0xFFFFB300).copy(alpha = 0.15f), Color.Transparent)),
           radius = canvasWidth * 0.7f,
           center = Offset(canvasWidth * 0.4f, canvasHeight * 0.4f)
       )
       
       // Draw Twinkling Stars
       val random = kotlin.random.Random(888)
       for (i in 0..120) {
           val x = random.nextFloat() * canvasWidth
           val y = random.nextFloat() * canvasHeight
           val sizeBase = random.nextFloat() * 3f + 1f
           
           val colorOption = random.nextInt(3)
           val color = when (colorOption) {
               0 -> Color.White
               1 -> Color(0xFFFFD54F) // Light Gold
               else -> Color(0xFF81D4FA) // Light Blue
           }
           
           val twinkleMultiplier = when (i % 3) {
               0 -> twinkle1
               1 -> twinkle2
               else -> twinkle3
           }
           
           drawCircle(
               color = color.copy(alpha = (random.nextFloat() * 0.5f + 0.5f) * twinkleMultiplier),
               radius = sizeBase,
               center = Offset(x, y)
           )
       }
        // Full sky golden mist (Hypnotic Wash)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFB300).copy(alpha = mistAlpha),
                    Color(0xFFFFD54F).copy(alpha = mistAlpha * 1.5f),
                    Color(0xFFFFB300).copy(alpha = mistAlpha * 0.8f)
                )
            ),
            size = size
        )

       
       // Animated Ring 1 (Top Left, protruding and moving in/out)
       val ringRadius = 150.dp.toPx()
       val animatedOffset = ringOffsetAnim.dp.toPx()
       
       drawCircle(
         color = SonicGold,
         radius = ringRadius,
         center = Offset(animatedOffset, animatedOffset),
         style = Stroke(width = 30.dp.toPx())
       )
       drawCircle(
         color = SonicGoldLight,
         radius = ringRadius,
         center = Offset(animatedOffset, animatedOffset),
         style = Stroke(width = 10.dp.toPx())
       )
       
       // Animated Ring 2 (Bottom Right, protruding and moving in/out)
       drawCircle(
         color = SonicGold,
         radius = ringRadius,
         center = Offset(canvasWidth - animatedOffset, canvasHeight - animatedOffset),
         style = Stroke(width = 30.dp.toPx())
       )
       drawCircle(
         color = SonicGoldLight,
         radius = ringRadius,
         center = Offset(canvasWidth - animatedOffset, canvasHeight - animatedOffset),
         style = Stroke(width = 10.dp.toPx())
       )
    }
    
    content()
  }
}

@Composable
fun SonicHomeScreen(navController: NavController? = null) {
  Box(modifier = Modifier.fillMaxSize()) {
    // Center Logo & Button
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .align(Alignment.Center)
        .offset(y = (-30).dp) // Move slightly up to center both items
    ) {
      Image(
        painter = painterResource(id = R.drawable.sonic_home),
        contentDescription = "Sonic Music Logo",
        modifier = Modifier
          .size(406.dp), // Increased size by 25% from 325.dp
        contentScale = ContentScale.Fit
      )

      Button(
        onClick = { navController?.navigate("games_list") },
        colors = ButtonDefaults.buttonColors(
          containerColor = NeonOrange,
          contentColor = Color.White
        ),
        modifier = Modifier
          .padding(top = 16.dp)
          .fillMaxWidth(0.6f)
          .height(56.dp)
      ) {
        Text(
          text = "INIZIA",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = { navController?.navigate("favorites") },
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF81D4FA), // Azzurrino (Light Blue)
          contentColor = Color.Black
        ),
        modifier = Modifier
          .fillMaxWidth(0.6f)
          .height(56.dp)
      ) {
        Icon(imageVector = Icons.Default.Favorite, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Preferiti", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      }
    }
  }
}

data class GameMedia(val name: String, val imageResId: Int)

val ALL_GAMES = listOf(
    GameMedia("Sonic the Hedgehog", R.drawable.sonic_the_hedgehog),
    GameMedia("Sonic CD", R.drawable.sonic_cd),
    GameMedia("Sonic the Hedgehog 2", R.drawable.sonic_the_hedgehog_2),
    GameMedia("Sonic 3 & Knuckles", R.drawable.sonic_and_knuckles),
    GameMedia("Knuckles' Chaotix", R.drawable.knuckles_chaotix),
    GameMedia("Sonic Triple Trouble", R.drawable.sonic_triple_trouble),
    GameMedia("Sonic 3D Blast", R.drawable.sonic_3d_blast),
    GameMedia("Tails Adventure", R.drawable.tails_adventure),
    GameMedia("Sonic R", R.drawable.sonic_r),
    GameMedia("Sonic Advance", R.drawable.sonic_advance),
    GameMedia("Sonic Advance 2", R.drawable.sonic_advance_2),
    GameMedia("Sonic Advance 3", R.drawable.sonic_advance_3),
    GameMedia("Sonic Battle", R.drawable.sonic_battle),
    GameMedia("Sonic Adventure", R.drawable.sonic_adventure),
    GameMedia("Sonic Adventure 2 Battle", R.drawable.sonic_adventure_2_battle),
    GameMedia("Sonic Heroes", R.drawable.sonic_heroes),
    GameMedia("Shadow the Hedgehog", R.drawable.shadow_the_hedgehog),
    GameMedia("Sonic Riders", R.drawable.sonic_riders),
    GameMedia("Sonic Riders Zero Gravity", R.drawable.sonic_riders_zero_gravity),
    GameMedia("Sonic Free Riders", R.drawable.sonic_free_riders),
    GameMedia("Sonic 06", R.drawable.sonic_06),
    GameMedia("Sonic Rush", R.drawable.sonic_rush),
    GameMedia("Sonic Rush Adventure", R.drawable.sonic_rush_adventure),
    GameMedia("Sonic Rivals", R.drawable.sonic_rivals),
    GameMedia("Sonic Rivals 2", R.drawable.sonic_rivals_2),
    GameMedia("Sonic and the Secret Rings", R.drawable.sonic_and_the_secret_rings),
    GameMedia("Sonic and the Black Knight", R.drawable.sonic_and_the_black_knight),
    GameMedia("Sonic Colors", R.drawable.sonic_colors),
    GameMedia("Sonic Colors DS", R.drawable.sonic_colors_ds),
    GameMedia("Sonic Colors Ultimate", R.drawable.sonic_colors_ultimate),
    GameMedia("Sonic Unleashed", R.drawable.sonic_unleashed),
    GameMedia("Sonic Generations", R.drawable.sonic_generations),
    GameMedia("Sonic Generations 3DS", R.drawable.sonic_generations_3ds),
    GameMedia("Sonic X Shadow Generations", R.drawable.sonic_x_shadow_generations),
    GameMedia("Sonic Lost World", R.drawable.sonic_lost_world),
    GameMedia("Sonic Forces", R.drawable.sonic_forces),
    GameMedia("Sonic Frontiers", R.drawable.sonic_frontiers),
    GameMedia("Sonic Frontiers Final Horizon", R.drawable.sonic_frontiers_final_horizon),
    GameMedia("Sonic Dash", R.drawable.sonic_dash),
    GameMedia("Sonic Dash 2: Sonic Boom", R.drawable.sonic_dash_2_sonic_boom),
    GameMedia("Sonic Forces Speed Battle", R.drawable.sonic_forces_speed_battle),
    GameMedia("Sonic Runners", R.drawable.sonic_runners),
    GameMedia("Sonic Dream Team", R.drawable.sonic_dream_team),
    GameMedia("Sonic & SEGA All-Stars Racing", R.drawable.sonic_e_sega_all_stars_racing),
    GameMedia("Sonic & All-Stars Racing Transformed", R.drawable.sonic_all_stars_racing_transformed),
    GameMedia("Team Sonic Racing", R.drawable.team_sonic_racing),
    GameMedia("Sonic Racing CrossWorlds", R.drawable.sonic_racing_crossworlds)
)

@Composable
fun GamesListScreen(navController: NavController? = null) {
  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      items(ALL_GAMES) { game ->
        GameMediaCard(game = game, navController = navController)
      }
    }
  }
}

@Composable
fun GameMediaCard(game: GameMedia, navController: NavController?) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "cardScale")
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(260.dp)
      .graphicsLayer {
          scaleX = scale
          scaleY = scale
      }
      .clickable(
          interactionSource = interactionSource,
          indication = androidx.compose.foundation.LocalIndication.current
      ) { 
           navController?.navigate("playlist/${java.net.URLEncoder.encode(game.name, "UTF-8")}") 
       },
    contentAlignment = Alignment.Center
  ) {
    // Glow contour layer 1 (Outer soft glow)
    Image(
      painter = painterResource(id = game.imageResId),
      contentDescription = null,
      contentScale = ContentScale.Fit,
      colorFilter = ColorFilter.tint(SonicGold),
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .blur(16.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
    )
    // Glow contour layer 2 (Inner strong glow)
    Image(
      painter = painterResource(id = game.imageResId),
      contentDescription = null,
      contentScale = ContentScale.Fit,
      colorFilter = ColorFilter.tint(SonicGold),
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .blur(6.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
    )
    // Main Image
    Image(
      painter = painterResource(id = game.imageResId),
      contentDescription = game.name,
      contentScale = ContentScale.Fit,
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    )
  }
}

@Preview(showBackground = true)
@Composable
fun SonicHomeScreenPreview() {
  MyApplicationTheme { SonicHomeScreen() }
}


@Composable

fun PlayerScreen(navController: NavController? = null, gameName: String, startIndex: Int = 0, viewModel: com.example.ui.player.LocalPlayerViewModel = viewModel()) {
  val tracks by viewModel.tracks.collectAsState()
  val currentIndex by viewModel.currentTrackIndex.collectAsState()
  val isPlaying by viewModel.isPlaying.collectAsState()
  val currentPosition by viewModel.currentPosition.collectAsState()
  val favorites by com.example.ui.playlist.FavoritesManager.favorites.collectAsState()

  LaunchedEffect(gameName) {
    viewModel.loadFolder("music/$gameName", startIndex)
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
      
      Spacer(modifier = Modifier.height(32.dp))
      
      if (tracks.isEmpty()) {
          Text("Caricamento brano...", color = Color.White)
      } else {
          val currentTrack = tracks.getOrNull(currentIndex)
          if (currentTrack != null) {
          
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
          
          Spacer(modifier = Modifier.height(16.dp))

          val isFav = favorites.any { it.url == currentTrack.url }

          IconButton(onClick = {

              val tState = com.example.ui.playlist.TrackState(

                  title = currentTrack.title,

                  uploader = currentTrack.uploader,

                  duration = currentTrack.duration / 1000L,

                  thumbnailUrl = currentTrack.thumbnailUrl,

                  url = currentTrack.url,

                  downloadProgress = 1f,

                  isDownloading = false,

                  isCompleted = true,

                  audioFile = currentTrack.file.name

              )

              val gName = if (gameName == "Favorites") currentTrack.gameName else gameName

              com.example.ui.playlist.FavoritesManager.toggleFavorite(tState, gName)

          }) {

              Icon(

                  imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,

                  contentDescription = "Preferito",

                  tint = NeonOrange,

                  modifier = Modifier.size(32.dp)

              )

          }
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
}


fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
