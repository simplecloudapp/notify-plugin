enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

//TODO: Rename root project
rootProject.name = "notify-plugin"

//TODO: Rename sub projects
include("notify-shared", "notify-velocity", "notify-bungeecord")