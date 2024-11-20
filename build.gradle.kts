

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Note: Use a correct syntax for Kotlin DSL
        classpath("com.google.gms:google-services:4.3.14")
    }
}

plugins {

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.gms.google.services) apply false

}
