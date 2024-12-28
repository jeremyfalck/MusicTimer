plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    android.buildFeatures.buildConfig = true
    namespace = "com.jfalck.musictimer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jfalck.musictimer"
        minSdk = 26
        targetSdk = 34
        versionCode = 5
        versionName = "0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "ADMOB_MAIN_BANNER_ID",
                "\"ca-app-pub-3940256099942544/9214589741\""
            )
        }
        release {
            buildConfigField(
                "String",
                "ADMOB_MAIN_BANNER_ID",
                "\"ca-app-pub-2297732968203269/8358496774\""
            )
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
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(project(":musictimer-common"))

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui.graphics)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.play.services.ads)

    implementation(libs.glance.appwidget)
    implementation(libs.glance.material)
    implementation(libs.glance.material3)
}