pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.bouncycastle:bcprov-jdk18on:1.85")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "AniLibria"

include(
    ":app-mobile",
    ":data",
    ":shared-ktx",
    ":shared-android-ktx",
    ":searchbar",
    ":app-tv",
    ":shared-app",
    ":quill-di",
    ":media-mobile",
    ":taiwa",
    ":envoy"
)
