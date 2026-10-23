import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Public (client-safe) config only. Secrets (Razorpay secret, JWT secret, DB URL,
// Fast2SMS key, webhook secret) must live on the backend, never in the APK.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun prop(name: String, default: String = ""): String {
    val p = localProps.getProperty(name, "").trim()
    return if (p.isNotBlank()) p else default
}

android {
    namespace = "com.app.junkitout"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.app.junkitout"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "1.7"

        buildConfigField("String", "API_BASE_URL", "\"${prop("API_BASE_URL", "https://junkitout-android.onrender.com")}\"")
        buildConfigField("String", "RAZORPAY_KEY_ID", "\"${prop("RAZORPAY_KEY_ID", "rzp_live_Ti962RQ76Szodd")}\"")
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"${prop("GOOGLE_MAPS_API_KEY", "AIzaSyCqWuqhCwQ9klHHAyCA3sLClTvnRM6eNrc")}\"")
        manifestPlaceholders["MAPS_API_KEY"] = prop("GOOGLE_MAPS_API_KEY", "AIzaSyCqWuqhCwQ9klHHAyCA3sLClTvnRM6eNrc")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("com.razorpay:checkout:1.6.40")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.google.maps.android:maps-compose:4.4.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
