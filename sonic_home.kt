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


      Spacer(modifier = Modifier.height(16.dp))

      Button(

        onClick = { navController?.navigate("favorites") },

        colors = ButtonDefaults.buttonColors(

          containerColor = Color.Transparent,

          contentColor = NeonOrange

        ),

        border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonOrange),

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
}
fun SonicHomeScreenPreview() {
  MyApplicationTheme { SonicHomeScreen() }
}
