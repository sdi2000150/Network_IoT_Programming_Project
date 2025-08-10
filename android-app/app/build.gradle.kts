plugins {
    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.di_team.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.di_team.android"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.core.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.lifecycle.process)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    implementation(libs.material)
    implementation(libs.fastcsv)
    testImplementation(libs.junit)

    // Add Eclipse Paho MQTT dependencies
    implementation(libs.org.eclipse.paho.client.mqttv3)
}