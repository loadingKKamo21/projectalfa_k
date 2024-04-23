package com.project.alfa.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestConfig {
    
    @Bean
    fun dummyGenerator(): DummyGenerator = DummyGenerator()
    
}