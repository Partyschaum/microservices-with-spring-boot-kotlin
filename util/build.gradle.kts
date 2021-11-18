plugins {
    id("io.spring.dependency-management")
    id("java-test-fixtures")
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "com.thatveryfewthings.microservices.util"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val springBootVersion = "2.5.4"
val testcontainersVersion = "1.16.2"

dependencies {
    implementation(project(":api"))

    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    testFixturesImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    testFixturesImplementation("org.testcontainers:testcontainers")
    testFixturesImplementation("org.testcontainers:junit-jupiter")
    testFixturesImplementation("org.testcontainers:mongodb")
    testFixturesImplementation("org.testcontainers:mysql")
}
