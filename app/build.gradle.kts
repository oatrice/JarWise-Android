import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.legacy.kapt)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.oatrice.jarwise"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.oatrice.jarwise"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.9.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.testLogging {
                    events("passed", "skipped", "failed", "standardOut", "standardError")
                    showStandardStreams = true
                }
            }
        }
    }

    sourceSets {
        val roomSchemas = file("$projectDir/schemas").path
        getByName("test") {
            assets.directories.add(roomSchemas)
        }
        getByName("androidTest") {
            assets.directories.add(roomSchemas)
        }
        getByName("release") {
            assets.directories.add(roomSchemas)
        }
    }
}

// Kotlin compiler options moved to the new DSL.
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.datastore.preferences)
    
    // Icons
    implementation(libs.androidx.material.icons.extended)
    
    // Coil for Image Loading
    implementation(libs.coil.compose)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.accompanist.permissions)
    
    // DocumentFile
    implementation(libs.androidx.documentfile)

    // ML Kit (Issue #31)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.mlkit.barcode.scanning)

    testImplementation(libs.junit)

    testImplementation(libs.androidx.room.testing)
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation(libs.androidx.core.ktx)
    testImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    // Koin
    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")

    // Google Auth & Drive
    implementation(libs.play.services.auth)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)
    implementation(libs.google.auth.library.oauth2.http)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.google.http.client.android)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
}

kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}
