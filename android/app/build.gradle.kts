// File build modul app (Kotlin DSL). Catatan sintaks untuk pembaca Java:
// - val x = ...          → variabel yang tidak bisa diubah (seperti final di Java)
// - blok { ... }         → konfigurasi bertingkat, mis. android { defaultConfig { ... } }
// - "${x}" di string     → menyisipkan nilai variabel x ke dalam teks
// Setiap kali file ini diubah, klik "Sync Now" di Android Studio.

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
}

// local.properties tidak di-commit; di sini kita baca nilai pribadi seperti GOOGLE_WEB_CLIENT_ID.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val googleWebClientId: String = localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")

android {
    namespace = "com.aris.templateapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.aris.templateapp"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // buildConfigField(tipe, nama, nilai) → menjadi konstanta BuildConfig.NAMA di kode Java.
        // Nilai String harus diberi tanda kutip di dalam teks, karena itu ada \"...\".
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")

        // Room menulis "peta" struktur database (JSON) ke folder app/schemas setiap versi database berubah.
        // File itu di-commit, sebagai riwayat untuk menulis migrasi saat tabel berubah nanti.
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    buildTypes {
        debug {
            // HP memanggil localhost; "adb reverse tcp:8080 tcp:8080" meneruskannya ke backend di laptop.
            buildConfigField("String", "API_BASE_URL", "\"http://localhost:8080/api/\"")
        }
        release {
            // Placeholder: diganti alamat server sungguhan saat app dirilis.
            buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/api/\"")
            optimization {
                enable = true
                packageScope = setOf("androidx.**")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        // ViewBinding: class Binding otomatis untuk setiap layout XML (pengganti findViewById).
        viewBinding = true
        // BuildConfig: class berisi API_BASE_URL dan GOOGLE_WEB_CLIENT_ID di atas.
        buildConfig = true
    }
}

dependencies {
    // implementation = dipakai kode app; annotationProcessor = pembuat kode saat compile (Hilt);
    // testImplementation = hanya untuk unit test di src/test.
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.viewpager2)

    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.gson)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    implementation(libs.androidx.browser)
    implementation(libs.lottie)
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    // Hanya di laptop yang path-nya mengandung kata "musl" (README → Troubleshooting): properti ini ditulis di
    // ~/.gradle/gradle.properties milik laptop itu, jadi laptop lain tidak terpengaruh.
    providers.gradleProperty("rakit.sqliteShimJar").orNull?.let { annotationProcessor(files(it)) }

    testImplementation(libs.junit)
    testImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.espresso.core)
}
