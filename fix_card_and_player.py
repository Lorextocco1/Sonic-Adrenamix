import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Update GameCard navigation
content = content.replace(
    'navController?.navigate("player/${game.name}")',
    'navController?.navigate("playlist/${game.name}")'
)

# Replace PlayerScreen definition
# First find where PlayerScreen starts
lines = content.splitlines(True)
for i, line in enumerate(lines):
    if "fun PlayerScreen(navController: NavController? = null, gameName: String," in line or "fun PlayerScreen(navController: NavController? = null, gameName: String) {" in line:
        start_idx = i
        break

# Wait, we already replaced PlayerScreen earlier. Let's see what it looks like now.
