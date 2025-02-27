plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.jmailen.kotlinter)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "${Config.applicationId}.core"
    compileSdk = Config.compileSdk

    defaultConfig {
        minSdk = Config.minSdk
    }

    sourceSets {
        named("main") { java { srcDirs(Config.mainSourceSet) } }
        named("test") { java { srcDirs(Config.testSourceSet) } }
        named("androidTest") { java { srcDirs(Config.androidTestSourceSet) } }
    }

    compileOptions {
        sourceCompatibility = Config.jvmVersion.sourceCompatibility
        targetCompatibility = Config.jvmVersion.targetCompatibility
    }

    kotlinOptions {
        jvmTarget = Config.jvmVersion.jvmTarget
        freeCompilerArgs += Config.kotlinFreeCompilerArgs
    }

    lint {
        warningsAsErrors = Config.lintWarningsAsErrors
        abortOnError = Config.lintAbortOnError
    }
}

dependencies {
    api(libs.jakewharton.timber)
    api(libs.koin.core)
    api(libs.kotlinx.collections.immutable)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler) {
        exclude(group = "com.google.auto", module = "auto-common")
    }

    implementation(libs.jakewharton.retrofit2.kotlinx.serialization.converter)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.square.okhttp.core)
    implementation(libs.square.okhttp.interceptor)
    implementation(libs.square.retrofit.core)
    implementation(libs.square.retrofit.converter.scalars)
    implementation(libs.io.github.rburgst)
    implementation(libs.work.runtime.ktx)
    implementation("io.insert-koin:koin-androidx-workmanager:3.5.0")

    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mock)
}
