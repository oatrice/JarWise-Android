#!/bin/bash
# Prefer existing JAVA_HOME (e.g., Java 25); fall back to Android Studio's JDK.
if [ -z "$JAVA_HOME" ]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
fi
echo "Using JDK from: $JAVA_HOME"
# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# Go to project root (one level up)
PROJECT_ROOT="$SCRIPT_DIR/.."

"$PROJECT_ROOT/gradlew" -p "$PROJECT_ROOT" "$@"
