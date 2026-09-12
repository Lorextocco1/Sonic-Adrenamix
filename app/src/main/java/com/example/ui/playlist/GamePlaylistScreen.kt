package com.example.ui.playlist
import androidx.compose.material.icons.filled.Favorite

import androidx.compose.material.icons.filled.FavoriteBorder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.SonicBackground
import com.example.formatTime
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.SonicGold
import java.net.URLEncoder

@Composable
fun GamePlaylistScreen(
    navController: NavController? = null,
    gameName: String,
    url: String = "",
    viewModel: GamePlaylistViewModel = viewModel()
) {
    val tracks by viewModel.tracks.collectAsState()
    val isExtracting by viewModel.isExtractingInfo.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val favorites by com.example.ui.playlist.FavoritesManager.favorites.collectAsState()

    LaunchedEffect(gameName) {
        viewModel.loadOrExtract(gameName, url)
    }

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIndices by remember { mutableStateOf(setOf<Int>()) }

    val completedCount = tracks.count { it.isCompleted }
    val totalCount = tracks.size

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                IconButton(onClick = { navController?.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gameName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SonicGold
                    )
                    if (totalCount > 0) {
                        Text(
                            text = if (completedCount == totalCount) "Tutti i $totalCount brani scaricati" else "Scaricati $completedCount di $totalCount brani",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (completedCount == totalCount) Color(0xFF69F0AE) else Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            if (!isExtracting && tracks.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelectionMode) {
                        Button(
                            onClick = {
                                if (selectedIndices.isNotEmpty()) {
                                    viewModel.startDownload(gameName, selectedIndices.toIntArray())
                                }
                                isSelectionMode = false
                                selectedIndices = emptySet()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                        ) {
                            Text("Download (${selectedIndices.size})", color = Color.White)
                        }
                        
                        TextButton(onClick = { 
                            isSelectionMode = false
                            selectedIndices = emptySet()
                        }) {
                            Text("Annulla", color = Color.LightGray)
                        }
                    } else {
                        Button(
                            onClick = {
                                val allIndices = tracks.indices.toList().toIntArray()
                                viewModel.startDownload(gameName, allIndices)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SonicGold)
                        ) {
                            Text("Download All", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        
                        TextButton(onClick = { isSelectionMode = true }) {
                            Text("Seleziona brani", color = NeonOrange)
                        }
                    }
                }
            }

            if (isExtracting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = NeonOrange,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Analizzando la playlist...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            } else if (errorMessage != null) {
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
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(tracks) { index, track ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (track.isCompleted) 1.5.dp else 0.5.dp,
                                    color = if (track.isCompleted) SonicGold.copy(alpha = 0.6f) else Color.DarkGray,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = track.isCompleted || isSelectionMode) {
                                    if (isSelectionMode) {
                                        if (selectedIndices.contains(index)) {
                                            selectedIndices = selectedIndices - index
                                        } else {
                                            selectedIndices = selectedIndices + index
                                        }
                                    } else {
                                        navController?.navigate("player/${java.net.URLEncoder.encode(gameName, "UTF-8")}/$index")
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (track.isCompleted)
                                    Color(0xFF1E2640).copy(alpha = 0.85f)
                                else
                                    Color(0xFF141926).copy(alpha = 0.65f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isSelectionMode) {
                                        Checkbox(
                                            checked = selectedIndices.contains(index),
                                            onCheckedChange = { isChecked ->
                                                if (isChecked) {
                                                    selectedIndices = selectedIndices + index
                                                } else {
                                                    selectedIndices = selectedIndices - index
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = NeonOrange,
                                                uncheckedColor = Color.Gray
                                            ),
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                    
                                    // Thumbnail / Index avatar
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (track.thumbnailUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = track.thumbnailUrl,
                                                contentDescription = track.title,
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
                                            text = track.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (track.isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (track.isCompleted) Color.White else Color.LightGray,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = track.uploader,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            maxLines = 1
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (track.isCompleted) {
                                        val isFav = favorites.any { it.url == track.url }
                                        IconButton(onClick = { com.example.ui.playlist.FavoritesManager.toggleFavorite(track, gameName) }) {
                                            Icon(
                                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Preferito",
                                                tint = NeonOrange,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    if (track.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Riproduci",
                                            tint = SonicGold,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                if (track.isDownloading) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        LinearProgressIndicator(
                                            progress = { track.downloadProgress },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = NeonOrange,
                                            trackColor = Color.DarkGray
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${(track.downloadProgress * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonOrange
                                        )
                                    }
                                } else if (track.isCompleted) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Scaricato",
                                            tint = Color(0xFF69F0AE),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Scaricato • ${if (track.duration > 0) formatTime(track.duration * 1000L) else "Audio pronto"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF69F0AE)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "In attesa...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
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
