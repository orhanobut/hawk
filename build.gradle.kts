buildscript {
    // Upgrade AGP's built-in compiler without applying a second Kotlin Android plugin.
    dependencies { classpath(libs.kotlin.gradle.plugin) }
}
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.dokka) apply false
}
allprojects {
    group = providers.gradleProperty("GROUP").get()
    version = providers.gradleProperty("VERSION_NAME").get()
}
