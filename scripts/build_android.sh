#!/bin/bash
# Use Android Studio's JDK (Java 21) which is compatible with our Gradle setup
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
echo "Using JDK from: $JAVA_HOME"
# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# Go to project root (one level up)
PROJECT_ROOT="$SCRIPT_DIR/.."

"$PROJECT_ROOT/gradlew" -p "$PROJECT_ROOT" "$@"
