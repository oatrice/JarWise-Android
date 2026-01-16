#!/bin/bash
# Use Android Studio's JDK (Java 21) which is compatible with our Gradle setup
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
echo "Using JDK from: $JAVA_HOME"
./gradlew "$@"
