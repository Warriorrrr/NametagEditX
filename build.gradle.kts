plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("io.github.goooler.shadow") version "8.1.8"
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
    implementation(libs.hikaricp)
    compileOnly(libs.luckperms)
    compileOnly(libs.placeholderapi)

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

group = "com.nametagedit"
version = "4.5.23"
java.sourceCompatibility = JavaVersion.VERSION_21

tasks {
    assemble {
        dependsOn(reobfJar)
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

