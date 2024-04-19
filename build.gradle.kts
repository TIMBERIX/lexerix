plugins {
    kotlin("jvm") version "1.9.22"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "toys.timberix"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://maven.icm.edu.pl/artifactory/repo/")
}

val exposedVersion = "0.49.0"

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")

    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("com.zaxxer:HikariCP:5.1.0")

    implementation("org.slf4j:slf4j-nop:2.0.13")

    //implementation("net.sourceforge.jtds:jtds:1.3.1") // doesn't work :(
    // jConnect Driver for Sybase
    implementation(files("libs/jconn2.jar"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)

}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "toys.timberix.MainKt"
    }
}

// Configure the shadowJar task
tasks.shadowJar {
    // Configure manifest settings if needed
    manifest {
        attributes["Main-Class"] = "com.example.MainClassName"
    }
}