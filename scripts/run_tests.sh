#!/bin/bash
# Script to run unit tests for SlipParser

echo "Running SlipParser tests..."
# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

"$SCRIPT_DIR/build_android.sh" app:testDebugUnitTest --tests "com.oatrice.jarwise.data.service.SlipParserTest"
