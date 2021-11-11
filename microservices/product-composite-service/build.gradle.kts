plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "com.thatveryfewthings.microservices.composite.product"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val springdocVersion = "1.5.10"
val springMockkVersion = "3.0.1"

dependencies {
    implementation(project(":api"))
    implementation(project(":util"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("org.springdoc:springdoc-openapi-webflux-ui:$springdocVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.getByName<Jar>("jar") {
    enabled = false
}
