plugins {
    id("java")
    id("java-library")
    id("io.papermc.paperweight.userdev") version "1.3.8"
    id("xyz.jpenilla.run-paper") version "1.0.6" // mojang mappings
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
group = "net.nonswag.tnl.mapping.v1_19_R2"
version = "1.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.thenextlvl.net/repository/maven-public/")
}

dependencies {
    implementation("org.projectlombok:lombok:1.18.24")
    implementation("net.nonswag.tnl:listener:6.3.2")
    paperDevBundle("1.19.3-R0.1-SNAPSHOT")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}
