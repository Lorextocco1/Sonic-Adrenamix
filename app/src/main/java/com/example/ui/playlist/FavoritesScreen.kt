package com.example.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.formatTime
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.SonicGold

@Composable
fun FavoritesScreen(navController: NavController? = null) {
    val favorites by FavoritesManager.favorites.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header with back button
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = { navController?.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color.White
                    )
                }
                Text(
                    text = "PREFERITI",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SonicGold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nessun brano nei preferiti.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(favorites) { index, favorite ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = SonicGold.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    navController?.navigate("player/Favorites/$index")
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E2640).copy(alpha = 0.85f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (favorite.thumbnailUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = favorite.thumbnailUrl,
                                                contentDescription = favorite.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = favorite.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = favorite.uploader,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            maxLines = 1
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(onClick = {
                                        // create a mock trackstate to toggle
                                        val track = TrackState(
                                            title = favorite.title,
                                            uploader = favorite.uploader,
                                            duration = favorite.duration,
                                            thumbnailUrl = favorite.thumbnailUrl,
                                            url = favorite.url,
                                            downloadProgress = 1f,
                                            isDownloading = false,
                                            isCompleted = true
                                        )
                                        FavoritesManager.toggleFavorite(track, favorite.gameName)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Rimuovi",
                                            tint = NeonOrange,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
