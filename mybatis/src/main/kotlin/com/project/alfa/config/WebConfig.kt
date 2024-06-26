package com.project.alfa.config

import com.project.alfa.interceptor.LogInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(LogInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/css/**", "/*.ico", "/error")
    }
    
}