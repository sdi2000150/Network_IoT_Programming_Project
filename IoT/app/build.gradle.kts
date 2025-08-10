plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.di_team.iot"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.di_team.iot"
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

    // JUnit Jupiter (JUnit 5) for testing
    testImplementation(libs.junit.jupiter)

    // Mockito core library
    testImplementation(libs.mockito.core)

    // Mockito Kotlin extensions
    testImplementation(libs.mockito.kotlin)

    // Optional: Mocking Android components (if needed)
    testImplementation(libs.mockito.android)

    androidTestImplementation(libs.espresso.core)

    // Add Eclipse Paho MQTT dependencies
    implementation(libs.org.eclipse.paho.client.mqttv3)
    implementation(libs.org.eclipse.paho.android.service)
}