plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.gymway.core_ui"
    compileSdk = 36

    buildFeatures{
        compose=true
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform(libs.androidx.compose.bom)) // BOM برای هماهنگ‌سازی نسخه‌های Compose
    implementation(libs.androidx.ui)                    // Compose UI
    implementation(libs.androidx.ui.graphics)           // UI Graphics
    implementation(libs.androidx.ui.tooling.preview)    // Preview در IDE
    implementation(libs.androidx.material3)             // Material3 components
    debugImplementation(libs.androidx.ui.tooling)       // ابزار debug فقط در حالت debug
}