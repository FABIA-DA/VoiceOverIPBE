plugins {
    id("java")
}

group = "at.htlleonding.fabia"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.ari4java:ari4java:+")
    implementation("ch.qos.logback:logback-classic:1.5.13")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}