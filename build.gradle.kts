plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("com.gradleup.shadow") version "9.0.0-beta15"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    paperweight.paperDevBundle(libs.paper.get().version)
    implementation(libs.hikaricp) {
        exclude(group = "org.slf4j")
    }
    compileOnly(libs.luckperms)
    compileOnly(libs.placeholderapi)
}

group = "net.earthmc.nametagedit"
version = "4.6.2"
java.sourceCompatibility = JavaVersion.VERSION_21

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("1.21.4")
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

        expand("version" to project.version)
    }
}

