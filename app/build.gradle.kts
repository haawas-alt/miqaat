plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.usman.miqaat"
    // Google Play (from 31 Aug 2026): new apps and updates must target API 36.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.usman.miqaat"
        minSdk = 26
        targetSdk = 36
        // CI stamps the GitHub run number so every build is newer than the last
        val run = (System.getenv("GITHUB_RUN_NUMBER") ?: "0").toInt()
        versionCode = 100 + run
        versionName = "1.$run"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "REPO", "\"haawas-alt/miqaat\"")
        // Provenance: every build names the exact source commit it was built from (shown in About).
        buildConfigField("String", "GIT_SHA", "\"${System.getenv("GITHUB_SHA") ?: "local"}\"")
        buildConfigField("String", "BUILD_TAG", "\"${if (run > 0) "v1.$run" else "local"}\"")
    }

    // Two editions from one code base:
    //  • play   – for Google Play. No self-updater, no REQUEST_INSTALL_PACKAGES; Play delivers updates.
    //  • github – direct download from GitHub Releases. In-app updater that verifies the published SHA-256 before installing.
    // The in-app Language switch needs every language in the base APK: no per-language splits in the bundle.
    bundle { language { enableSplit = false } }

    flavorDimensions += "dist"
    productFlavors {
        create("play") { dimension = "dist"; buildConfigField("Boolean", "SELF_UPDATE", "false") }
        create("github") { dimension = "dist"; buildConfigField("Boolean", "SELF_UPDATE", "true") }
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
            // R8 without renaming: dead code and unused resources go, stack traces stay readable.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    testOptions { unitTests { isIncludeAndroidResources = true; isReturnDefaultValues = true } }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    lint {
        // Accessibility and correctness checks fail the build; the report is published by CI.
        warningsAsErrors = false
        abortOnError = true
        checkReleaseBuilds = true
        error += listOf("ContentDescription", "MissingPermission")
        warning += listOf("UnusedResources")
        disable += listOf("ObsoleteLintCustomCheck", "GradleDependency", "AndroidGradlePluginVersion", "OldTargetApi")
        xmlReport = true
        htmlReport = true
    }
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
    testImplementation("junit:junit:4.13.2")
    // Compose UI + screenshot tests run on the JVM (Robolectric native graphics), so CI needs no emulator.
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")
    // Instrumented screenshots and UI regression tests run on a real emulator in CI (the "emulator" job).
    androidTestImplementation(bom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
