import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.18"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	//All-open compiler plugin
	id("org.jetbrains.kotlin.plugin.allopen") version "1.6.21"
	//No-arg compiler plugin
	id("org.jetbrains.kotlin.plugin.noarg") version "1.6.21"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("plugin.jpa") version "1.6.21"
	//kapt compiler plugin
	kotlin("kapt") version "1.6.21"
	//Kotlin Serialization
	kotlin("plugin.serialization") version "1.6.21"
}

group = "com.project"
version = "0.0.1-SNAPSHOT"

val queryDslVersion = "5.0.0"

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")
	//kotlin-logging
	implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
	//Kotlin Serialization
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
	//Jackson Datatype: JSR310
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	//Jackson Databind
	implementation("com.fasterxml.jackson.core:jackson-databind")
	//Gson
	implementation("com.google.code.gson:gson:2.10.1")
	//Querydsl
	implementation("com.querydsl:querydsl-jpa:${queryDslVersion}")
	//Spring Boot DataSource Decorator - P6Spy
	implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.8.1")
	//Thymeleaf Layout Dialect
	implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")
	
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.h2database:h2")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	//Querydsl
	kapt("com.querydsl:querydsl-apt:${queryDslVersion}:jpa")
	
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	//Mockito-Kotlin
	testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
	//Embedded Redis
	testImplementation("it.ozimov:embedded-redis:0.7.2")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

allOpen {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.MappedSuperclass")
	annotation("javax.persistence.Embeddable")
}

noArg {
	annotation("javax.persistence.Entity")
}
