plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    // 1. Wymagane dla Kotlin 2.0 + Compose:
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.projektrejestratorjazdy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.projektrejestratorjazdy"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 2. Włączenie obsługi Compose
    buildFeatures {
        compose = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // --- Podstawowe ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // --- Compose (Zaktualizowany BOM dla Kotlin 2.0) ---
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // --- KLUCZOWA POPRAWKA DLA BŁĘDU "THEME NOT FOUND" ---
    // Bez tej biblioteki plik themes.xml nie widzi Theme.Material3...
    implementation(libs.material)
    // Alternatywnie, jeśli libs.material nie zadziała:
    // implementation("com.google.android.material:material:1.12.0")

    // --- Navigation ---
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // --- Uprawnienia ---
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // --- CameraX ---
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // --- GPS ---
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // --- Room (Baza danych) ---
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
}