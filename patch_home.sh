sed -i '/.height(56.dp)/!b;n;n;n;n;n;n;a\
\n\
      Spacer(modifier = Modifier.height(16.dp))\n\
      Button(\n\
        onClick = { navController?.navigate("favorites") },\n\
        colors = ButtonDefaults.buttonColors(\n\
          containerColor = Color.Transparent,\n\
          contentColor = NeonOrange\n\
        ),\n\
        border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonOrange),\n\
        modifier = Modifier\n\
          .fillMaxWidth(0.6f)\n\
          .height(56.dp)\n\
      ) {\n\
        Icon(imageVector = Icons.Default.Favorite, contentDescription = null)\n\
        Spacer(modifier = Modifier.width(8.dp))\n\
        Text("Preferiti", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)\n\
      }' app/src/main/java/com/example/MainActivity.kt
