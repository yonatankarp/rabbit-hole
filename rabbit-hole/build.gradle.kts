import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("jacoco")
    id("maven-publish")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.diffplug.spotless")
}

tasks {
    getByName<Jar>("jar") {
        enabled = true
        archiveBaseName.set("")
    }

    getByName<BootJar>("bootJar") {
        enabled = false
    }

    build {
        finalizedBy(spotlessApply)
    }

    withType<Test> {
        useJUnitPlatform()
        finalizedBy(spotlessApply)
        finalizedBy(jacocoTestReport)
    }
}

dependencies {
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8")
    implementation(group = "org.springframework.boot", name = "spring-boot-starter-amqp")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation(group = "io.mockk", name = "mockk", version = "1.13.3")

}

allOpen {
    annotation("org.springframework.context.annotation.Configuration")
}
