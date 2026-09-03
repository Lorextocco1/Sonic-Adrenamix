with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "enableEdgeToEdge()" in line:
        insert_idx = i
        break

code_to_insert = """
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
"""

new_lines = lines[:insert_idx] + code_to_insert.splitlines(True) + lines[insert_idx:]

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.writelines(new_lines)
