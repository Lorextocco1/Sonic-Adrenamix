import os
files_to_remove = [
    'app/src/main/java/com/example/ui/player/LocalPlayerScreen.kt',
    'app/src/main/java/com/example/ui/download/DownloadScreen.kt',
    'app/src/main/java/com/example/ui/download/DownloadViewModel.kt'
]
for f in files_to_remove:
    if os.path.exists(f):
        os.remove(f)

# Also remove references in MainActivity.kt
with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

import re
# Remove the old "local_player" route
content = re.sub(r'composable\("local_player/\{folderName\}"\) \{.*?\}', '', content, flags=re.DOTALL)
# Remove the old DownloadScreen import just in case
content = re.sub(r'import com.example.ui.download.DownloadScreen\n', '', content)
content = re.sub(r'import com.example.ui.player.LocalPlayerScreen\n', '', content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
