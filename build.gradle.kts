plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.gms) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

allprojects {
    configurations.all {
        resolutionStrategy {
            force("org.bouncycastle:bcprov-jdk18on:1.85")
            force("org.jdom:jdom2:2.0.6.1")
            force("io.netty:netty-handler:4.1.137.Final")
            force("io.netty:netty-codec-http:4.1.137.Final")
            force("org.bitbucket.b_c:jose4j:0.9.6")
            force("org.apache.commons:commons-lang3:3.18.0")
            force("com.google.guava:guava:32.0.0-android")
            eachDependency {
                if (requested.group == "io.netty") {
                    useVersion("4.1.137.Final")
                }
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions{
            freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
            freeCompilerArgs.add("-opt-in=kotlinx.coroutines.FlowPreview")
            freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi")
            freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        }
    }
}
