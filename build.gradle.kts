plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.vlog.web"
version = "0.0.45-SERVER-8091"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    //implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    //implementation("org.springframework.boot:spring-boot-starter-data-redis")
    //implementation("org.springframework.session:spring-session-data-redis")
    //testImplementation("org.springframework.boot:spring-boot-starter-test")
    //testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    ///testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.4")
    //runtimeOnly("com.mysql:mysql-connector-j")
    //testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.3")
    //implementation("org.apache.httpcomponents:httpclient:4.5.13")

}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
