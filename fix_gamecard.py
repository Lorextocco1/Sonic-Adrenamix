import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Fix GameCard navigation
content = re.sub(
    r'navController\?\.navigate\("player/\$\{game\.name\}"\)',
    r'navController?.navigate("playlist/${java.net.URLEncoder.encode(game.name, "UTF-8")}")',
    content
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
