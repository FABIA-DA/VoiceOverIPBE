import org.gradle.kotlin.dsl.annotationProcessor
import org.gradle.kotlin.dsl.compileOnly

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

val jacksonVersion = "2.17.2"
val lombokVersion = "1.18.40"

dependencies {
    implementation("org.projectlombok:lombok:$lombokVersion")
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    implementation("io.github.ari4java:ari4java:0.17.0")
    implementation("ch.qos.logback:logback-classic:1.5.13")
    implementation("io.projectreactor.netty:reactor-netty:1.1.15")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda:$jacksonVersion")
    implementation("joda-time:joda-time:2.14.0")
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
        manifest {
            attributes["Main-Class"] = "at.htlleonding.fabia.Main"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}