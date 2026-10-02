plugins { alias(libs.plugins.android.application) }
android {
    namespace = "com.orhanobut.benchmark"
    compileSdk = libs.versions.compile.sdk.get().toInt()
    defaultConfig {
        applicationId = "com.orhanobut.benchmark"
        minSdk = libs.versions.min.sdk.get().toInt()
        targetSdk = libs.versions.compile.sdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    lint { warningsAsErrors = true }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
}
dependencies { implementation(project(":hawk")) }
