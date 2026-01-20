#!/bin/bash

# CreditGuard Build Script
# Builds the Android app without Android Studio

set -e

echo "🛡️ CreditGuard Build Script"
echo "=========================="

# Check for ANDROID_HOME
if [ -z "$ANDROID_HOME" ]; then
    if [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
    elif [ -d "/usr/local/android-sdk" ]; then
        export ANDROID_HOME="/usr/local/android-sdk"
    else
        echo "❌ ANDROID_HOME not set. Please set it to your Android SDK path."
        echo "   Example: export ANDROID_HOME=\$HOME/Android/Sdk"
        exit 1
    fi
fi

echo "📱 Using Android SDK: $ANDROID_HOME"

# Download Gradle wrapper if not present
if [ ! -f "gradlew" ]; then
    echo "📥 Downloading Gradle wrapper..."
    gradle wrapper --gradle-version 8.2 2>/dev/null || {
        echo "Downloading gradle wrapper manually..."
        curl -sL https://services.gradle.org/distributions/gradle-8.2-bin.zip -o gradle.zip
        unzip -q gradle.zip
        ./gradle-8.2/bin/gradle wrapper
        rm -rf gradle-8.2 gradle.zip
    }
fi

chmod +x gradlew

echo "🔨 Building debug APK..."
./gradlew assembleDebug --no-daemon

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [ -f "$APK_PATH" ]; then
    echo ""
    echo "✅ Build successful!"
    echo "📦 APK location: $APK_PATH"
    echo ""
    echo "To install on connected device:"
    echo "  adb install $APK_PATH"
else
    echo "❌ Build failed. Check the error messages above."
    exit 1
fi
