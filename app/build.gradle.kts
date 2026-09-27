plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.usman.miqaat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.usman.miqaat"
        minSdk = 26
        targetSdk = 34
        // CI stamps the GitHub run number so every build is newer than the last
        val run = (System.getenv("GITHUB_RUN_NUMBER") ?: "0").toInt()
        versionCode = 100 + run
        versionName = "1.$run"
        buildConfigField("String", "REPO", "\"haawas-alt/miqaat\"")
    }

    // The signing key never lives in the repository. CI decodes it from the KEYSTORE_BASE64 secret
    // into keystore/miqaat.jks (git-ignored) and passes the password via KEYSTORE_PASSWORD.
    signingConfigs {
        create("release") {
            val ks = rootProject.file("keystore/miqaat.jks")
            val pw = System.getenv("KEYSTORE_PASSWORD")
            if (ks.exists() && pw != null) {
                storeFile = ks
                storePassword = pw
                keyAlias = "miqaat"
                keyPassword = pw
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    val bom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(bom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.media:media:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    // High-precision prayer time calculation (Meeus algorithms), pure Java, no deps
    implementation("com.batoulapps.adhan:adhan:1.2.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
