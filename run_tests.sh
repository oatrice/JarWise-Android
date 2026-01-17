#!/bin/bash
# Script to run unit tests for SlipParser

echo "Running SlipParser tests..."
./build_android.sh app:testDebugUnitTest --tests "com.oatrice.jarwise.data.service.SlipParserTest"
