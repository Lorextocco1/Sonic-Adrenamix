with open('app/src/main/java/com/example/ui/playlist/GamePlaylistViewModel.kt', 'r') as f:
    content = f.read()

imports = """import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.io.File"""

content = content.replace("import java.io.File", imports)

target = """    fun loadOrDownload(gameName: String, rawUrl: String) {
        currentGameName.value = gameName
        val destFolder = File(getApplication<Application>().filesDir, "music/$gameName")
        if (!destFolder.exists()) destFolder.mkdirs()

        viewModelScope.launch(Dispatchers.IO) {
            val localFiles = destFolder.listFiles { f -> f.name.endsWith(".json") }
            val existingJsonMap = mutableMapOf<String, JSONObject>()
            localFiles?.forEach { file ->
                try {
                    val json = JSONObject(file.readText())
                    val audioFileName = json.optString("audioFile", "")
                    if (File(destFolder, audioFileName).exists()) {
                        existingJsonMap[json.optString("url", "")] = json
                        existingJsonMap[json.optString("title", "")] = json
                    }
                } catch (e: Exception) {
                    AppLogger.logError("Error reading local json: ${file.name}", e)
                }
            }

            val offlineTracks = existingJsonMap.values.map { json ->
                TrackState(
                    title = json.optString("title", "Unknown"),
                    uploader = json.optString("uploader", "Unknown"),
                    duration = json.optLong("duration", 0L),
                    thumbnailUrl = json.optString("thumbnailUrl", ""),
                    url = json.optString("url", ""),
                    downloadProgress = 1f,
                    isDownloading = false,
                    isCompleted = true,
                    audioFile = json.optString("audioFile", ""),
                    index = json.optInt("index", 0)
                )
            }.sortedBy { it.index }.distinctBy { it.url }

            // Only update if we don't already have tracks from DownloadManager
            if (DownloadManager.tracks.value[gameName].isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    DownloadManager.updateTracks(gameName, offlineTracks)
                }
            }

            if (rawUrl.isEmpty()) return@launch

            val currentTracks = DownloadManager.tracks.value[gameName]
            val isAlreadyExtracting = DownloadManager.isExtracting.value[gameName] == true
            
            if (!currentTracks.isNullOrEmpty() || isAlreadyExtracting) {
                // If we already have tracks in memory or it's currently extracting/queued, skip starting the service again
                return@launch
            }

            DownloadManager.setExtracting(gameName, true)

            val intent = Intent(getApplication(), DownloadService::class.java).apply {
                putExtra("gameName", gameName)
                putExtra("url", rawUrl)
            }
            ContextCompat.startForegroundService(getApplication(), intent)
        }
    }"""

replacement = """    fun loadOrExtract(gameName: String, rawUrl: String) {
        currentGameName.value = gameName
        val destFolder = File(getApplication<Application>().filesDir, "music/$gameName")
        if (!destFolder.exists()) destFolder.mkdirs()

        viewModelScope.launch(Dispatchers.IO) {
            val localFiles = destFolder.listFiles { f -> f.name.endsWith(".json") }
            val existingJsonMap = mutableMapOf<String, JSONObject>()
            localFiles?.forEach { file ->
                try {
                    val json = JSONObject(file.readText())
                    val audioFileName = json.optString("audioFile", "")
                    if (File(destFolder, audioFileName).exists()) {
                        existingJsonMap[json.optString("url", "")] = json
                        existingJsonMap[json.optString("title", "")] = json
                    }
                } catch (e: Exception) {
                    AppLogger.logError("Error reading local json: ${file.name}", e)
                }
            }

            val offlineTracks = existingJsonMap.values.map { json ->
                TrackState(
                    title = json.optString("title", "Unknown"),
                    uploader = json.optString("uploader", "Unknown"),
                    duration = json.optLong("duration", 0L),
                    thumbnailUrl = json.optString("thumbnailUrl", ""),
                    url = json.optString("url", ""),
                    downloadProgress = 1f,
                    isDownloading = false,
                    isCompleted = true,
                    audioFile = json.optString("audioFile", ""),
                    index = json.optInt("index", 0)
                )
            }.sortedBy { it.index }.distinctBy { it.url }

            val currentTracks = DownloadManager.tracks.value[gameName]
            if (currentTracks.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    DownloadManager.updateTracks(gameName, offlineTracks)
                }
            }

            if (rawUrl.isEmpty()) return@launch

            val isAlreadyExtracting = DownloadManager.isExtracting.value[gameName] == true
            if (!currentTracks.isNullOrEmpty() || isAlreadyExtracting) {
                return@launch
            }

            DownloadManager.setExtracting(gameName, true)

            try {
                val url = rawUrl.replace("https://youtube.com/", "https://www.youtube.com/")
                    .replace("https://m.youtube.com/", "https://www.youtube.com/")
                    .replace("https://youtu.be/", "https://www.youtube.com/watch?v=")

                val service = ServiceList.YouTube
                val extractor = service.getPlaylistExtractor(url)
                extractor.fetchPage()

                val allItems = mutableListOf<StreamInfoItem>()
                allItems.addAll(extractor.initialPage.items)

                var currentPage = extractor.initialPage
                while (currentPage.hasNextPage()) {
                    currentPage = extractor.getPage(currentPage.nextPage)
                    allItems.addAll(currentPage.items)
                }

                val initialTrackStates = allItems.mapIndexed { index, item ->
                    val rawName = item.name ?: "Track ${index + 1}"
                    val cleanTitle = rawName
                        .replace(Regex("^\\\\s*\\\\d+[\\\\.\\\\-\\\\_\\\\s]+"), "")
                        .replace(Regex("\\\\[OST\\\\]", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("- Sonic the Hedgehog", RegexOption.IGNORE_CASE), "")
                        .trim()
                        .ifEmpty { rawName }

                    val bestThumbnail = item.thumbnails.maxByOrNull { it.height * it.width }?.url
                        ?: item.thumbnails.firstOrNull()?.url
                        ?: ""

                    val existing = existingJsonMap[item.url] ?: existingJsonMap[cleanTitle]
                    val isAlreadyDownloaded = existing != null

                    TrackState(
                        title = cleanTitle,
                        uploader = item.uploaderName ?: "Sega / DeoxysPrime",
                        duration = item.duration,
                        thumbnailUrl = bestThumbnail,
                        url = item.url,
                        downloadProgress = if (isAlreadyDownloaded) 1f else 0f,
                        isDownloading = false,
                        isCompleted = isAlreadyDownloaded,
                        audioFile = existing?.optString("audioFile", "") ?: "",
                        index = index
                    )
                }

                withContext(Dispatchers.Main) {
                    DownloadManager.updateTracks(gameName, initialTrackStates)
                }
            } catch (e: Exception) {
                AppLogger.logError("Failed to extract playlist for $gameName", e)
            } finally {
                DownloadManager.setExtracting(gameName, false)
            }
        }
    }

    fun startDownload(gameName: String, indices: IntArray) {
        val intent = Intent(getApplication(), DownloadService::class.java).apply {
            putExtra("gameName", gameName)
            putExtra("indices", indices)
        }
        ContextCompat.startForegroundService(getApplication(), intent)
    }"""

with open('app/src/main/java/com/example/ui/playlist/GamePlaylistViewModel.kt', 'w') as f:
    f.write(content.replace(target, replacement))
print("Patched VM")
