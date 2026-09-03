with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()
    
# Add startIndex param
content = content.replace(
    "fun PlayerScreen(navController: NavController? = null, gameName: String, viewModel: com.example.ui.player.LocalPlayerViewModel = viewModel()) {",
    "fun PlayerScreen(navController: NavController? = null, gameName: String, startIndex: Int = 0, viewModel: com.example.ui.player.LocalPlayerViewModel = viewModel()) {"
)

# Update the loadFolder call to pass startIndex (Wait, LocalPlayerViewModel doesn't take startIndex in loadFolder. Let's update LocalPlayerViewModel first).
with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
