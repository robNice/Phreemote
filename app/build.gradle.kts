plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "de.robnice.philipstvcontrol.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.robnice.philipstvcontrol"
        minSdk = 26
        targetSdk = 36
        versionCode = 1010003001
        versionName = "1.3.1-mobile"
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
    implementation(project(":shared"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.activity.compose)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
