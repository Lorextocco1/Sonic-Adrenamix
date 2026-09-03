import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Remove the @Composable at 498
lines = content.splitlines()
if len(lines) > 497 and lines[497].strip() == "@Composable":
    del lines[497]

content = "\n".join(lines)

# Remove the download composable
content = re.sub(r'composable\("download"\)\s*\{\s*com\.example\.ui\.download\.DownloadScreen\(navController = navController\)\s*\}', '', content, flags=re.DOTALL)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

