// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

/*
 * Release signing, if it has been set up.
 *
 * A properly signed build installs cleanly over the previous one. Nothing here is
 * committed: the values come from the environment, which on GitHub means four repository
 * secrets. When they are absent the release build falls back to Android's debug key, so a
 * usable APK is still produced -- it just cannot be installed over a copy signed with a
 * different key, which is why the README says to uninstall first if Android objects.
 */
val keystorePath: String? = System.getenv("DECLUTTER_KEYSTORE_PATH")
val keystorePassword: String? = System.getenv("DECLUTTER_KEYSTORE_PASSWORD")
val keyAlias: String? = System.getenv("DECLUTTER_KEY_ALIAS")
val keyPassword: String? = System.getenv("DECLUTTER_KEY_PASSWORD")
val hasReleaseKey = !keystorePath.isNullOrBlank() &&
    !keystorePassword.isNullOrBlank() &&
    !keyAlias.isNullOrBlank() &&
    !keyPassword.isNullOrBlank()

android {
    namespace = "dev.gru953.declutter"
    compileSdk = 37
    compileSdkMinor = 2

    defaultConfig {
        applicationId = "dev.gru953.declutter"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseKey) {
            create("release") {
                storeFile = file(keystorePath!!)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
                enableV1Signing = false
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            signingConfig = if (hasReleaseKey) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    buildFeatures {
        compose = true
    }

    androidResources {
        // The interface is UK English only, so no other locale's resources are packaged.
        localeFilters += listOf("en")
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "/META-INF/DEPENDENCIES",
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        warningsAsErrors = false
        abortOnError = true
        disable += setOf(
            // The interface is English only, on purpose.
            "MissingTranslation",
            // compileSdk is 37 because the Compose libraries require it; targetSdk stays at
            // 36 so the app does not silently adopt Android 17's behaviour changes on a
            // phone running Android 16. That gap is the decision, not an oversight.
            "OldTargetApi",
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Shizuku gives the app ADB-level privileges without root, when the user starts it.
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    // Android 9+ blocks reflection onto non-SDK interfaces; this lifts that for our own calls.
    implementation(libs.hiddenapibypass)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
