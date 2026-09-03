with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

import re
# Remove the DOWNLOAD button from SonicHomeScreen
content = re.sub(
    r'\s*Button\(\s*onClick = \{ navController\?\.navigate\("download"\) \},.*?text = "DOWNLOAD",.*?\}\n\s*\}',
    '',
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
