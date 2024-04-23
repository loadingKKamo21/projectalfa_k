package com.project.alfa.interceptor

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private const val LOG_ID = "LOG_ID"

class LogInterceptor : HandlerInterceptor {
    
    private val log: KLogger = KotlinLogging.logger { }
    
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val requestURI = request.requestURI
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        
        request.setAttribute(LOG_ID, uuid)
        
        log.info { "[$uuid] REQUEST: [$requestURI][$handler]" }
        
        return true
    }
    
    override fun postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any,
                            modelAndView: ModelAndView?) {
        log.info { "postHandle [$modelAndView]" }
    }
    
    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any,
                                 ex: Exception?) {
        val requestURI = request.requestURI
        val logId = request.getAttribute(LOG_ID).toString()
        
        log.info { "[$logId] RESPONSE: [$requestURI][$handler]" }
        
        if (ex != null)
            log.error(ex) { "afterCompletion error" }
    }
    
}