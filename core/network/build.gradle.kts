import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

val localProps =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) load(file.inputStream())
    }

fun localProp(key: String): String =
    localProps[key]?.toString()
        ?: System.getenv(key)
        ?: error("Missing required property: $key (set in local.properties or as env var)")

android {
    namespace = "com.rumir.network"
    compileSdk = libs.versions.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", "\"${localProp("BASE_URL")}\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {

    implementation(libs.androidx.paging.runtime)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.security.crypto)
}
