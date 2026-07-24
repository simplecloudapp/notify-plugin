plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include("notify-shared", "notify-velocity", "notify-bungeecord")

rootProject.name = "notify-plugin"