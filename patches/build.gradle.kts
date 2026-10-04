plugins {
    kotlin("plugin.serialization") version "2.4.10"
}

group = "morningentree.morphe"

patches {
    about {
        name = "Morning Entree Patches"
        description = "Various patches for use with Morphe"
        source = "git@github.com:Entree3k/Morning-Entree-Patches.git"
        author = "Morning Entree"
        contact = "na"
        website = "https://morphe.software"
        license = "GPLv3"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
        // jelf 0.12.0 (used by the Pairip native patch) requires a JVM 17 runtime,
        // so the patches module must target 17 (default was 11).
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }

    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath: Configuration by configurations.creating

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)

    // Pairip de-virtualization (shared/misc/pairip): ELF parsing, JSON maps, native loader.
    implementation(libs.jelf)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.native.lib.loader) {
        // bundled version clashes with newer runtime dependency
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}