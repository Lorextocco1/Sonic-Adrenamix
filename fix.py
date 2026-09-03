with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    lines = f.readlines()

new_lines = lines[:323] + """
val ALL_GAMES = listOf(
    GameMedia("Sonic the Hedgehog", R.drawable.sonic_the_hedgehog),
    GameMedia("Sonic CD", R.drawable.sonic_cd),
    GameMedia("Sonic the Hedgehog 2", R.drawable.sonic_the_hedgehog_2),
    GameMedia("Sonic the Hedgehog 3", R.drawable.sonic_the_hedgehog_3),
    GameMedia("Sonic & Knuckles", R.drawable.sonic_and_knuckles),
    GameMedia("Knuckles' Chaotix", R.drawable.knuckles_chaotix),
    GameMedia("Sonic the Hedgehog 4 Episode I", R.drawable.sonic_the_hedgehog_4_episode_i),
    GameMedia("Sonic the Hedgehog 4 Episode II", R.drawable.sonic_the_hedgehog_4_episode_ii),
    GameMedia("Sonic Triple Trouble", R.drawable.sonic_triple_trouble),
    GameMedia("Sonic 3D Blast", R.drawable.sonic_3d_blast),
    GameMedia("Tails Adventure", R.drawable.tails_adventure),
    GameMedia("Sonic R", R.drawable.sonic_r),
    GameMedia("Sonic Mania", R.drawable.sonic_mania),
    GameMedia("Sonic Mania Plus", R.drawable.sonic_mania_plus),
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
    GameMedia("Sonic Frontiers", R.drawable.sonic_frontiers)
)

@Composable
fun GamesListScreen(navController: NavController? = null) {
""".splitlines(True) + lines[371:]

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.writelines(new_lines)
