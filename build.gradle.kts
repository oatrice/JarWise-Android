// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Override AGP's built-in Kotlin to use Kotlin 2.3.0 (Java 25 support).
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.services) apply false
}
