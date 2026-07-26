import org.gradle.api.file.DuplicatesStrategy.EXCLUDE

plugins {
    kotlin("jvm") version "2.4.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.260.0")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    withType<Jar> {
        manifest {
            attributes["Main-Class"] = "aui.SpringBootLabsCdkApp"
        }

        duplicatesStrategy = EXCLUDE

        isZip64 = true

        from(sourceSets.main.get().output)

        dependsOn(configurations.runtimeClasspath)

        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }
}
