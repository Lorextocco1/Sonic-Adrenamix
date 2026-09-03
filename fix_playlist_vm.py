import re

with open('app/src/main/java/com/example/ui/playlist/GamePlaylistViewModel.kt', 'r') as f:
    content = f.read()

# Pass thumbnailUrl to downloadTrack
content = content.replace(
    "downloadTrack(index, item.url, initialTrackStates[index].title, destFolder)",
    "downloadTrack(index, item.url, initialTrackStates[index].title, initialTrackStates[index].thumbnailUrl, destFolder)"
)

content = content.replace(
    "private suspend fun downloadTrack(index: Int, url: String, cleanTitle: String, destFolder: File) {",
    "private suspend fun downloadTrack(index: Int, url: String, cleanTitle: String, thumbnailUrl: String, destFolder: File) {"
)

content = content.replace("extractor.thumbnailUrl", "thumbnailUrl")

with open('app/src/main/java/com/example/ui/playlist/GamePlaylistViewModel.kt', 'w') as f:
    f.write(content)
