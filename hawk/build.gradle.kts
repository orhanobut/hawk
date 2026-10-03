import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
}
android {
    namespace = "com.orhanobut.hawk"
    compileSdk = libs.versions.compile.sdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.min.sdk.get().toInt()
        consumerProguardFiles("proguard-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    lint { warningsAsErrors = true }
}
dependencies {
    // Gson appears in a public constructor; stdlib is required by compiled Kotlin.
    // Keep Gson's Error Prone annotations: R8 resolves them in consumer builds.
    api(libs.gson)
    api(libs.kotlin.stdlib) {
        exclude(group = "org.jetbrains", module = "annotations")
    }
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.junit)
}
mavenPublishing {
    configure(AndroidSingleVariantLibrary(javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"), variant = "release"))
    coordinates(project.group.toString(), "hawk", project.version.toString())
    publishToMavenCentral()
    signAllPublications()
    pom {
        name.set("Hawk")
        description.set("Simple, pluggable key-value storage for Android")
        url.set("https://github.com/orhanobut/hawk")
        licenses { license {
            name.set("The Apache Software License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution.set("repo")
        } }
        developers { developer { id.set("nr4bt"); name.set("Orhan Obut") } }
        scm {
            url.set("https://github.com/orhanobut/hawk")
            connection.set("scm:git:https://github.com/orhanobut/hawk.git")
            developerConnection.set("scm:git:ssh://git@github.com/orhanobut/hawk.git")
        }
    }
}
publishing {
    repositories { maven { name = "verification"; url = layout.buildDirectory.dir("repository").get().asFile.toURI() } }
}
// Signing stays mandatory for remote releases; local verification needs no secrets.
tasks.withType<Sign>().configureEach {
    onlyIf {
        val localVerification = gradle.taskGraph.hasTask(":hawk:publishAllPublicationsToVerificationRepository")
        val remotePublication = gradle.taskGraph.allTasks.any { it.name.contains("MavenCentral") }
        !localVerification || remotePublication
    }
}
