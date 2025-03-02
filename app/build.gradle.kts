plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android") version "1.9.0"
}

android {
    namespace = "com.san_pedrito.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.san_pedrito.myapplication"
        minSdk = 28
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
        jvmTarget = "11" // Añadir opciones de Kotlin
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0") // Añadir dependencia de Kotlin
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}