package com.project.alfa.error

import com.project.alfa.error.exception.BusinessException
import com.project.alfa.error.exception.ErrorCode
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * https://cheese10yun.github.io/spring-guide-exception/
 */
@ControllerAdvice
class GlobalExceptionHandler {
    
    private val log: KLogger = KotlinLogging.logger { }
    
    /**
     * javax.validation.Valid 또는 @Validated 로 바인딩 실패 시 발생
     * HttpMessageConverter에 등록한 HttpMessageConverter로 바인딩 실패 시 발생
     * 주로 @RequestBody, @RequestPart 어노테이션에서 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleMethodArgumentNotValidException(
            e: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        log.error(e) { "handleMethodArgumentNotValidException" }
        val response: ErrorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.bindingResult)
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }
    
    /**
     * @ModelAttribute로 바인딩 실패 시 발생
     */
    @ExceptionHandler(BindException::class)
    protected fun handleBindException(e: BindException): ResponseEntity<ErrorResponse> {
        log.error(e) { "handleBindException" }
        val response: ErrorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.bindingResult)
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }
    
    /**
     * enum 타입 불일치로 바인딩 실패 시 발생
     * 주로 @RequestParam enum으로 바인딩 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    protected fun handleMethodArgumentTypeMismatchException(
            e: MethodArgumentTypeMismatchException
    ): ResponseEntity<ErrorResponse> {
        log.error(e) { "handleMethodArgumentTypeMismatchException" }
        val response: ErrorResponse = ErrorResponse.of(e)
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }
    
    /**
     * 지원하지 않는 HTTP 메서드 호출 시 발생
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    protected fun handleHttpRequestMethodNotSupportedException(
            e: HttpRequestMethodNotSupportedException
    ): ResponseEntity<ErrorResponse> {
        log.error(e) { "handleHttpRequestMethodNotSupportedException" }
        val response: ErrorResponse = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED)
        return ResponseEntity(response, HttpStatus.METHOD_NOT_ALLOWED)
    }
    
    /**
     * Authentication 객체가 필요한 권한을 보유하지 않았을 때 발생
     */
    @ExceptionHandler(AccessDeniedException::class)
    protected fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        log.error(e) { "handleAccessDeniedException" }
        val response: ErrorResponse = ErrorResponse.of(ErrorCode.HANDLE_ACCESS_DENIED)
        return ResponseEntity(response, HttpStatus.valueOf(ErrorCode.HANDLE_ACCESS_DENIED.status))
    }
    
    @ExceptionHandler(BusinessException::class)
    protected fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        log.error(e) { "handleBusinessException" }
        val errorCode: ErrorCode = e.errorCode
        val response: ErrorResponse = ErrorResponse.of(errorCode)
        return ResponseEntity(response, HttpStatus.valueOf(errorCode.status))
    }
    
    @ExceptionHandler(Exception::class)
    protected fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error(e) { "handleException" }
        val response: ErrorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR)
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }
    
}