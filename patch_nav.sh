sed -i '/composable("games_list") {/i \
    composable("favorites") {\n\
      com.example.ui.playlist.FavoritesScreen(navController = navController)\n\
    }\n\
' app/src/main/java/com/example/MainActivity.kt
