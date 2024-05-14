plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.jfalck.musictimer_common"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    api(platform(libs.androidx.compose.bom))

    api(libs.androidx.core.ktx)

    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.datastore.core)
    implementation(libs.datastore.preferences)

    api(libs.androidx.activity.compose)
    api(libs.androidx.ui)
    api(libs.androidx.core.splashscreen)

    api(libs.play.services.wearable)

    androidTestApi(libs.androidx.ui.test.junit4)
    androidTestApi(platform(libs.androidx.compose.bom))

    api(libs.androidx.ui.tooling)
    api(libs.androidx.ui.tooling.preview)
    debugApi(libs.androidx.ui.test.manifest)
    api(libs.androidx.material3)

    api(libs.koin.android)
    api(libs.androidx.lifecycle.livedata)
}