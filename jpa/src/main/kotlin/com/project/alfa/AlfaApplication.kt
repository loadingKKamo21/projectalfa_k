package com.project.alfa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class AlfaApplication

fun main(args: Array<String>) {
	runApplication<AlfaApplication>(*args)
}
