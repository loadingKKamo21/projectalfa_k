package com.project.alfa.config

import com.project.alfa.aop.ProjectAspects.Companion.LogTraceAspect
import com.project.alfa.aop.trace.logtrace.LogTrace
import com.project.alfa.aop.trace.logtrace.ThreadLocalLogTrace
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AopConfig {
    
    @Bean
    fun logTraceAspect(logTrace: LogTrace): LogTraceAspect = LogTraceAspect(logTrace)
    
    @Bean
    fun logTrace(): LogTrace = ThreadLocalLogTrace()
    
}