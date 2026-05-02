plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // GANTI kapt menjadi ksp agar kompatibel dengan Kotlin 2.0
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.kelompok3.cinematch"
    // Gunakan SDK 35 agar lebih stabil dengan library yang ada
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kelompok3.cinematch"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    // 1. Navigation
    implementation(libs.androidx.navigation.compose)

    // 2. Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)

    // 3. Networking (Retrofit)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // 4. Image Loader (Coil)
    implementation(libs.coil.compose)

    // 5. Room (Database Lokal) - FIX MENGGUNAKAN KSP
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    // Kuncinya ada di sini: ganti kapt(...) menjadi ksp(...)
    ksp(libs.room.compiler)

    // 6. Default Compose Libraries (Menggunakan referensi libs.versions.toml)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}