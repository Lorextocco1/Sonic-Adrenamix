with open('app/src/main/java/com/example/ui/playlist/GamePlaylistViewModel.kt', 'r') as f:
    content = f.read()

target1 = """    val isExtractingInfo: StateFlow<Boolean> = combine(DownloadManager.isExtracting, currentGameName) { map, game ->
        map[game] ?: false
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)"""

replacement1 = """    val isExtractingInfo: StateFlow<Boolean> = combine(DownloadManager.isExtracting, currentGameName) { map, game ->
        map[game] ?: false
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage"""

content = content.replace(target1, replacement1)

target2 = """            } catch (e: Exception) {
                AppLogger.logError("Failed to extract playlist for $gameName", e)
            } finally {"""

replacement2 = """            } catch (e: Exception) {
                AppLogger.logError("Failed to extract playlist for $gameName", e)
                _errorMessage.value = "Errore: ${e.message}"
            } finally {"""

content = content.replace(target2, replacement2)

target3 = """    fun loadOrExtract(gameName: String, rawUrl: String) {"""

replacement3 = """    fun loadOrExtract(gameName: String, rawUrl: String) {
        _errorMessage.value = null"""

content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/ui/playlist/GamePlaylistViewModel.kt', 'w') as f:
    f.write(content)
print("Patched ViewModel with error message")
