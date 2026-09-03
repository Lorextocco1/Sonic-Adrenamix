import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Fix @Composable @Composable issue
content = content.replace("@Composable\n@Composable\nfun PlayerScreen", "@Composable\nfun PlayerScreen")

# Remove formatTime if it exists, then add it globally
content = re.sub(r'fun formatTime\([^)]*\)\s*(:\s*String)?\s*\{[^}]*\}', '', content)

format_time_code = """
fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
"""

if "fun formatTime" not in content:
    content += "\n" + format_time_code
    
# Add imports
imports_to_add = [
    "import androidx.compose.material3.Slider",
    "import androidx.compose.material3.SliderDefaults",
    "import androidx.compose.foundation.layout.Row",
    "import androidx.compose.foundation.layout.Arrangement",
    "import androidx.compose.foundation.layout.PaddingValues",
    "import androidx.compose.material.icons.filled.PlayArrow"
]

for imp in imports_to_add:
    if imp not in content:
        content = content.replace("import android.os.Bundle", f"{imp}\nimport android.os.Bundle")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
