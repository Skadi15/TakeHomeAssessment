plugins {
    // Apply the war plugin to add support for building webapps in Java.
    war
    id("org.gretty") version "4.1.5"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:5.13.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.13.0")

    // This dependency is used by the application.
    implementation(libs.guava)

    providedCompile("jakarta.enterprise:jakarta.enterprise.cdi-api:4.1.0")
    providedCompile("jakarta.servlet:jakarta.servlet-api:6.1.0")

    providedCompile("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}


