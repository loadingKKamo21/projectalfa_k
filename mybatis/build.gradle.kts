import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.18"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
//	//Kotlin Serialization
//	kotlin("plugin.serialization") version "1.6.21"
}

group = "com.project"
version = "0.0.1-SNAPSHOT"

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
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:2.3.1")
	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")
//	//Kotlin Serialization
//	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
	//Gson
	implementation("com.google.code.gson:gson:2.10.1")
	//kotlin-logging
	implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
	//Thymeleaf Layout Dialect
	implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")
	//MyBatis Pagination - PageHelper
	implementation("com.github.pagehelper:pagehelper-spring-boot-starter:1.4.7")
	//Jackson Datatype: JSR310
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	//Jackson Databind
	implementation("com.fasterxml.jackson.core:jackson-databind")
	
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.h2database:h2")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:2.3.1")
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
