plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Enable Compose plugin for Kotlin 2.x toolchain
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.dezory.vivofpsunlocker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dezory.vivofpsunlocker"
        minSdk = 21
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 22
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(

                getDefaultProguardFile("proguard-android-optimize.txt"),

                "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    // Compose compiler version is managed by the Kotlin Compose plugin
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")

    // Jetpack Compose (Material 3)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.foundation.android)
    // Material icons for bottom navigation
    implementation("androidx.compose.material:material-icons-extended")
    // Optional: previews/testing
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
