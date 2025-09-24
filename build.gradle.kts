// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("org.jetbrains.compose") version "1.5.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.21" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.0" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    dependencies {
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.5.10")
        classpath("com.android.tools.build:gradle:8.1.2")  // Versión corregida del AGP
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    }
}
