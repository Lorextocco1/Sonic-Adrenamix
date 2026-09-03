data class GameMedia(val name: String, val imageResId: Int)

val ALL_GAMES = listOf(
    GameMedia("Sonic the Hedgehog", R.drawable.sonic_the_hedgehog),
    GameMedia("Sonic CD", R.drawable.sonic_cd),
    GameMedia("Sonic the Hedgehog 2", R.drawable.sonic_the_hedgehog_2),
    GameMedia("Sonic 3 & Knuckles", R.drawable.sonic_3_and_knuckles),
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
    GameMedia("Sonic & SEGA All-Stars Racing", R.drawable.sonic_and_sega_all_stars_racing),
    GameMedia("Sonic & All-Stars Racing Transformed", R.drawable.sonic_and_all_stars_racing_transformed),
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
