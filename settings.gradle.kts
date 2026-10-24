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
        classpath("org.jdom:jdom2:2.0.6.1")
        classpath("io.netty:netty-handler:4.1.137.Final")
        classpath("io.netty:netty-codec-http:4.1.137.Final")
        classpath("org.bitbucket.b_c:jose4j:0.9.6")
        classpath("org.apache.commons:commons-lang3:3.18.0")
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
