package com.project.alfa.config

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Configuration
class ProjectConfig {
    
    @PersistenceContext
    private lateinit var em: EntityManager
    
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
    
    @Bean
    fun jpaQueryFactory(em: EntityManager): JPAQueryFactory = JPAQueryFactory(em)
    
}