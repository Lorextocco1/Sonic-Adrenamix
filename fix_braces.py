with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""      }
  }
}

data class GameMedia""",
"""      }
    }
  }
}

data class GameMedia"""
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
