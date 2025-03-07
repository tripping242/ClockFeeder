object Config {

    @JvmStatic
    val minSdk = 26

    @JvmStatic
    val compileSdk = 34

    @JvmStatic
    val targetSdk = 34

    @JvmStatic
    val versionCode = 1

    @JvmStatic
    val versionName = "0.0.1"

    @JvmStatic
    val versionFull = "v$versionName-b$versionCode"

    @JvmStatic
    val appName = "ClockFeeder"

    @JvmStatic
    val applicationId = "com.codingblocks.clock"

    @JvmStatic
    val namespace = "com.codingblocks.clock"

    @JvmStatic
    val archiveName = "$appName-$versionFull"

    @JvmStatic
    val jvmVersion = JvmVersion.JVM_17

    const val mainSourceSet = "src/main/kotlin"

    const val testSourceSet = "src/test/kotlin"

    const val androidTestSourceSet = "src/androidTest/kotlin"

    const val lintWarningsAsErrors = false

    const val lintAbortOnError = false

    @JvmStatic
    val kotlinFreeCompilerArgs = listOf(
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-opt-in=kotlinx.coroutines.DelicateCoroutinesApi",
        "-opt-in=kotlinx.coroutines.FlowPreview",
        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
        "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"
    )
}
