package com.project.alfa.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class ProjectConfig(private val userDetailsService: UserDetailsService) {
    
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
    
    @Bean
    fun authenticationManager(http: HttpSecurity): AuthenticationManager =
            http.getSharedObject(AuthenticationManagerBuilder::class.java)
                    .userDetailsService(userDetailsService).passwordEncoder(passwordEncoder())
                    .and().build()
    
}