with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    lines = f.readlines()

new_routes = """
    composable("playlist/{gameName}") { backStackEntry ->
      val gameName = backStackEntry.arguments?.getString("gameName") ?: "Unknown"
      val url = if (gameName == "Sonic the Hedgehog") "https://youtube.com/playlist?list=PLvNp0Boas720BYHiEHd-zM942KP_bCSZ4" else ""
      com.example.ui.playlist.GamePlaylistScreen(navController = navController, gameName = gameName, url = url)
    }
    composable("player/{gameName}/{index}") { backStackEntry ->
      val gameName = backStackEntry.arguments?.getString("gameName") ?: "Unknown"
      val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
      PlayerScreen(navController = navController, gameName = gameName, startIndex = index)
    }
"""

for i, line in enumerate(lines):
    if "composable(\"player/{gameName}\") {" in line:
        start_idx = i
        break

# Find closing brace of that composable block
end_idx = start_idx
open_braces = 0
found_first = False
for i in range(start_idx, len(lines)):
    line = lines[i]
    open_braces += line.count('{')
    open_braces -= line.count('}')
    if open_braces > 0:
        found_first = True
    if found_first and open_braces == 0:
        end_idx = i
        break

lines = lines[:start_idx] + new_routes.splitlines(True) + lines[end_idx+1:]

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.writelines(lines)
