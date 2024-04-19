import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "1.9.22"
    id("signing")
    id("com.vanniktech.maven.publish") version "0.28.0"
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

signing {
    sign(publishing.publications)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(project.group.toString(), "lexerix", project.version.toString())

    pom {
        name.set("Lexerix")
        description.set("Kotlin API using Jetbrains Exposed for accessing the Lexware Financial Office database.")
        inceptionYear.set("2024")
        val repo = "Timberix/lexerix"
        url.set("https://github.com/$repo")
        licenses {
            license {
                name.set("MIT")
                url.set("https://github.com/$repo/blob/main/LICENSE")
            }
            license {
                name.set("jConnect License Agreement")
                url.set("https://github.com/$repo/blob/main/LICENSE_jconnect")
            }
        }
        developers {
            developer {
                id.set("kberix")
                name.set("kberix")
                url.set("https://github.com/kberix")
            }
        }
        scm {
            url.set("https://github.com/$repo")
            connection.set("scm:git:git://github.com/$repo.git")
            developerConnection.set("scm:git:ssh://git@github.com/$repo.git")
        }
        issueManagement {
            system.set("GitHub")
            url.set("https://github.com/$repo/issues")
        }
    }
}