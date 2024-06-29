package com.project.alfa.config

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Configuration
class ProjectConfig(private val userDetailsService: UserDetailsService) {
    
    @PersistenceContext
    private lateinit var em: EntityManager
    
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
    
    @Bean
    fun jpaQueryFactory(em: EntityManager): JPAQueryFactory = JPAQueryFactory(em)
    
    @Bean
    fun authenticationManager(http: HttpSecurity): AuthenticationManager =
            http.getSharedObject(AuthenticationManagerBuilder::class.java)
                    .userDetailsService(userDetailsService).passwordEncoder(passwordEncoder())
                    .and().build()
    
}