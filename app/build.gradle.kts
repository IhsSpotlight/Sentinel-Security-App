plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.compose.compiler)

}

android {
    namespace = "com.example.sentinel"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.sentinel"
        minSdk = 24
        targetSdk = 36
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "34.0.0"
}

dependencies {
    // Base Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Jetpack Compose dependencies
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui:1.9.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.4")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.activity)
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.2")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.2")

    // Retrofit for network requests
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Glide for image loading
    implementation(libs.glide)

    // Media3 ExoPlayer (for camera streaming)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.exoplayer.hls)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
