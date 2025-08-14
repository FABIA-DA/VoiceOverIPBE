plugins {
    id("java")
}

group = "at.htlleonding"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(group = "com.alphacephei", name = "vosk", version = "0.3.45")
}

tasks.test {
    useJUnitPlatform()
}