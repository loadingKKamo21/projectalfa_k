package com.project.alfa.aop.trace.logtrace

import com.project.alfa.aop.trace.TraceId
import com.project.alfa.aop.trace.TraceStatus
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

class ThreadLocalLogTrace : LogTrace {
    
    companion object {
        private const val START_PREFIX = "--->"
        private const val COMPLETE_PREFIX = "<---"
        private const val EX_PREFIX = "<-X-"
        
        private fun addSpace(prefix: String, level: Int): String {
            val sb = StringBuilder()
            for (i in 0 until level)
                sb.append(if (i == level - 1) " |$prefix" else " |      ")
            return sb.toString()
        }
    }
    
    private val log: KLogger = KotlinLogging.logger { }
    
    private val traceIdHolder: ThreadLocal<TraceId> = ThreadLocal<TraceId>()
    
    override fun begin(message: String): TraceStatus {
        syncTraceId()
        val traceId = traceIdHolder.get()
        val startTime = System.currentTimeMillis()
        
        log.info { "[${traceId.id}] ${addSpace(START_PREFIX, traceId.level)} $message" }
        
        return TraceStatus(traceId, startTime, message)
    }
    
    override fun end(status: TraceStatus) = complete(status, null)
    
    override fun exception(status: TraceStatus, e: Exception) = complete(status, e)
    
    private fun complete(status: TraceStatus, e: Exception?) {
        val stopTime = System.currentTimeMillis()
        val resultTime = stopTime - status.startTime
        val traceId = status.traceId
        
        if (e == null)
            log.info {
                "[${traceId.id}] ${addSpace(COMPLETE_PREFIX, traceId.level)} ${status.message} / time = $resultTime ms"
            }
        else
            log.info {
                "[${traceId.id}] ${
                    addSpace(EX_PREFIX, traceId.level)
                } ${status.message} / time = $resultTime ms, ex = ${e.toString()}"
            }
        
        releaseTraceId()
    }
    
    private fun syncTraceId() {
        val traceId = traceIdHolder.get()
        if (traceId == null)
            traceIdHolder.set(TraceId())
        else
            traceIdHolder.set(traceId.createNextId())
    }
    
    private fun releaseTraceId() {
        val traceId = traceIdHolder.get()
        if (traceId.isFirstLevel())
            traceIdHolder.remove()
        else
            traceIdHolder.set(traceId.createPreviousId())
    }
    
}