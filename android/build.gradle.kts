// File build tingkat project (berlaku untuk semua modul). Ditulis dalam Kotlin DSL:
// - alias(libs.plugins.x) mengambil plugin dari gradle/libs.versions.toml
// - "apply false" artinya plugin hanya disiapkan di sini; dipakai sungguhan di app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.hilt) apply false
}
