import org.gradle.internal.impldep.org.bouncycastle.oer.its.etsi102941.Url
import java.net.URL

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    //firebase
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {

        google()
        mavenCentral()

    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.10.1") // Use latest stable version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0") // Match your Kotlin version
        // Add other classpath dependencies here
    }
}

