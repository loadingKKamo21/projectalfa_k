package com.project.alfa.aop

import com.project.alfa.aop.trace.TraceStatus
import com.project.alfa.aop.trace.logtrace.LogTrace
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.core.annotation.Order

class ProjectAspects {
    
    companion object {
        
        @Aspect
        @Order(1)
        class LogTraceAspect(private val logTrace: LogTrace) {
            
            @Around("com.project.alfa.aop.Pointcuts.allMvc()" +
                            "|| com.project.alfa.aop.Pointcuts.allUtils()" +
                            "|| com.project.alfa.aop.Pointcuts.authentication()")
            fun execute(joinPoint: ProceedingJoinPoint): Any? {
                lateinit var status: TraceStatus
                try {
                    val message = joinPoint.signature.toShortString()
                    status = logTrace.begin(message)
                    
                    val result = joinPoint.proceed()
                    
                    logTrace.end(status)
                    return result
                } catch (e: Exception) {
                    logTrace.exception(status, e)
                    throw e
                }
            }
            
        }
        
    }
    
}