plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("com.gradleup.shadow") version "9.2.2"
    id("xyz.jpenilla.run-paper") version "3.0.1"
}

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    paperweight.paperDevBundle("${libs.versions.minecraft.get()}-R0.1-SNAPSHOT")
    implementation(libs.hikaricp) {
        exclude(group = "org.slf4j")
    }
    compileOnly(libs.luckperms)
    compileOnly(libs.placeholderapi)
}

java.sourceCompatibility = JavaVersion.VERSION_21

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion(project.findProperty("minecraftVersion") as? String ?: libs.versions.minecraft.get())
    }

    shadowJar {
        archiveClassifier.set("")

        relocate("com.zaxxer.hikari", "com.nametagedit.libs.hikari")
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()

        expand(
            "version" to project.version,
            "api_version" to libs.versions.minecraft.get()
        )
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            val releasesUrl = "https://repo.warriorrr.dev/releases"
            val snapshotsUrl = "https://repo.warriorrrr.dev/snapshots"
            url = uri(if (project.version.toString().endsWith("-SNAPSHOT")) snapshotsUrl else releasesUrl)

            name = "warrior"
            credentials(PasswordCredentials::class)
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components.getByName("java"))
        }
    }
}

