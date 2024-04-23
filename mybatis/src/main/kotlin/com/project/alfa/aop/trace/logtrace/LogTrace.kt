package com.project.alfa.aop.trace.logtrace

import com.project.alfa.aop.trace.TraceStatus

interface LogTrace {
    
    fun begin(message: String): TraceStatus
    
    fun end(status: TraceStatus): Unit
    
    fun exception(status: TraceStatus, e: Exception): Unit
    
}