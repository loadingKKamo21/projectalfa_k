package com.project.alfa.config.security

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@TestConfiguration("SecurityConfig")
class TestSecurityConfig {
    
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable()
        
        http.authorizeRequests()
                .regexMatchers(HttpMethod.GET, "/api/posts/(?:\\d+)?$", "/api/posts/\\d+/attachments$", "/api/posts/\\d+/attachments/\\d+/download$").permitAll()
                .regexMatchers(HttpMethod.GET,"/api/posts\\??(&?(?:page=\\d+)?)(&?(?:size=\\d+)?)(&?(?:condition=(title|content|titleOrContent|writer)?)?)(&?(?:keyword=.*)?)$").permitAll()
                .regexMatchers(HttpMethod.GET, "/api/posts/\\d+/comments\\??(&?(?:page=\\d+)?)(&?(?:size=\\d+)?)$").permitAll()
                .mvcMatchers("/api/members", "/api/members/forgot-password").permitAll()
                .mvcMatchers("/api/members/**", "/logout", "/api/posts/**", "/api/posts/*/comments/**", "/api/comments/**").authenticated()
                .anyRequest().permitAll()
        
        return http.build()
    }
    
}