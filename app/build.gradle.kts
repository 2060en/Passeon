plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("kotlin-kapt")
}

android {
    namespace = "com.ethy.passeon"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ethy.passeon"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation("androidx.compose.material:material-icons-extended-android:1.6.8")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")
    // ✨ Room 資料庫函式庫
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    // implementation("androidx.room:room-rxjava3:$room_version") // 如果需要 RxJava 支援
    // implementation("androidx.room:room-testing:$room_version") // 如果需要測試
    implementation("androidx.room:room-ktx:$room_version") // Kotlin Coroutines 支援 (必要)
    //ksp("androidx.room:room-compiler:$room_version") // ✨ 改用 ksp 而不是 annotationProcessor
    kapt("androidx.room:room-compiler:$room_version")
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}