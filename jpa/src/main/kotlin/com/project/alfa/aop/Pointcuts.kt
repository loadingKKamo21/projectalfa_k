package com.project.alfa.aop

import org.aspectj.lang.annotation.Pointcut

class Pointcuts {
    
    @Pointcut("execution(* *.*(..))")
    fun allMatch() {
    }
    
    @Pointcut("execution(* com.project.alfa.repositories..*.*(..))")
    fun allRepositories() {
    }
    
    @Pointcut("execution(* com.project.alfa.services..*.*(..))")
    fun allServices() {
    }
    
    @Pointcut("execution(* com.project.alfa.controllers..*.*(..))")
    fun allControllers() {
    }
    
    @Pointcut("allRepositories() || allServices() || allControllers()")
    fun allMvc() {
    }
    
    @Pointcut("execution(* com.project.alfa.utils..*.*(..))")
    fun allUtils() {
    }
    
    @Pointcut("execution(* com.project.alfa.security..*.*(..))")
    fun authentication() {
    }
    
}