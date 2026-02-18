plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    id("kotlin-kapt")
    alias(libs.plugins.kotlin.compose)
}

android {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    buildFeatures {
        viewBinding = true
    }
    namespace = "com.practicum.playlistmaker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.practicum.playlistmaker"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
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
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.viewpager)
    implementation(libs.fragment.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.kotlinx.coroutines.android)


    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)
    kapt(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.glide)
    implementation(libs.retrofit2)
    implementation(libs.gson)
    implementation(libs.cgson)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.navigation.compose.v277)
    implementation(libs.androidx.lifecycle.viewmodel.compose.v270)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.coil.compose)
    implementation(libs.gson.v2101)

    implementation(libs.koin.android.v356)
    implementation(libs.koin.androidx.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)

}