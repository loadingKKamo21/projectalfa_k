package com.project.alfa.aop.trace

import java.util.*

data class TraceId(val id: String = createId(), val level: Int = 0) {
    
    companion object {
        private fun createId(): String = UUID.randomUUID().toString().substring(0, 8)
    }
    
    fun createNextId(): TraceId = copy(level = level + 1)
    
    fun createPreviousId(): TraceId = copy(level = level - 1)
    
    fun isFirstLevel(): Boolean = level == 0
    
}