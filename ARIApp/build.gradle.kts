plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "at.htlleonding.fabia"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("at.htlleonding.fabia.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.ari4java:ari4java:+")
    implementation("ch.qos.logback:logback-classic:1.5.13")
    implementation("io.projectreactor.netty:reactor-netty:1.1.15")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.apache.httpcomponents:httpmime:4.5.14")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "at.htlleonding.fabia.Main"
    }
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        manifest {
            attributes["Main-Class"] = "at.htlleonding.fabia.Main"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}