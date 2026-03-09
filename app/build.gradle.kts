plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "de.robnice.philipstvcontrol"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "de.robnice.philipstvcontrol"
        minSdk = 30
        targetSdk = 36
        versionCode = 35
        versionName = "1.0.2"

    }

    val ksPath = System.getenv("ANDROID_KEYSTORE_PATH")

    signingConfigs {
        if (ksPath != null) {
            create("release") {
                storeFile = file(ksPath)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            if (ksPath != null) {
                signingConfig = signingConfigs.getByName("release")
            }

            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.core.splashscreen)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material.icons.extended)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.datastore.preferences)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.okhttp)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.tooling)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}