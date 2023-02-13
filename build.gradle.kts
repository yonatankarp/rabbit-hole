plugins {
    id("jacoco")
    id("rabbit-hole.java-conventions")
    id("rabbit-hole.code-metrics")
    id("rabbit-hole.publishing-conventions")
    id("com.diffplug.spotless") version "6.15.0" apply true
    id("org.springframework.boot") version "3.0.2" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    val kotlinVersion = "1.8.10"
    kotlin("jvm") version kotlinVersion apply false
    kotlin("plugin.allopen") version kotlinVersion apply false
}

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://packages.confluent.io/maven/") }
        maven {
            url = uri("https://maven.pkg.github.com/yonatankarp/rabbit-hole")
            credentials {
                username = findProperty("gpr.user")?.toString() ?: System.getenv("GITHUB_ACTOR")
                password = findProperty("gpr.key")?.toString() ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

jacoco {
    toolVersion = "0.8.7"
}
