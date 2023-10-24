plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("de.mannodermaus.android-junit5") version "1.9.3.0"
    kotlin("plugin.serialization") version "1.6.0"
}

android {
    namespace = "uvg.edu.gt.smartfridge"
    compileSdk = 33

    defaultConfig {
        applicationId = "uvg.edu.gt.smartfridge"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {
    val nav_version = "2.6.0"
    val ktor_version = "2.3.4"
    val version = "0.9.0"
    val lifecycle_version = "2.6.2"

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    // ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    // Library for Loading Async Image from URL
    implementation("io.coil-kt:coil-compose:2.4.0")
    // HTTP Client
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")
    // Json Serializer
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    // Navigation
    implementation("androidx.navigation:navigation-compose:$nav_version")
    // UI Libraries for display Calendars.
    implementation("androidx.compose.material:material:1.4.3")
    implementation ("io.github.vanpra.compose-material-dialogs:datetime:${version}")
    implementation ("io.github.vanpra.compose-material-dialogs:core:${version}")

    // Enabling coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    //testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.10.0")
    // (Optional) If you need "Parameterized Tests"
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")

    // (Optional) If you also have JUnit 4-based tests
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.0")
}