package com.project.alfa.error

import com.project.alfa.error.exception.ErrorCode
import org.springframework.validation.BindingResult
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * https://cheese10yun.github.io/spring-guide-exception/
 */
class ErrorResponse(
        private val status: Int,
        private val code: String,
        private val message: String,
        private val errors: List<FieldError>
) {
    
    private constructor(code: ErrorCode, errors: List<FieldError>) : this(code.status, code.code, code.message, errors)
    
    private constructor(code: ErrorCode) : this(code.status, code.code, code.message, ArrayList())
    
    companion object {
        
        fun of(code: ErrorCode) = ErrorResponse(code)
        
        fun of(code: ErrorCode, errors: List<FieldError>) = ErrorResponse(code, errors)
        
        fun of(code: ErrorCode, bindingResult: BindingResult) = ErrorResponse(code, FieldError.of(bindingResult))
        
        fun of(e: MethodArgumentTypeMismatchException) = ErrorResponse(
                ErrorCode.INVALID_TYPE_VALUE,
                FieldError.of(e.name, if (e.value == null) "" else e.value.toString(), e.errorCode)
        )
        
    }
    
    class FieldError private constructor(val field: String, val value: String, val reason: String) {
        
        companion object {
            
            fun of(field: String, value: String, reason: String): List<FieldError> {
                val fieldErrors: ArrayList<FieldError> = ArrayList()
                fieldErrors.add(FieldError(field, value, reason))
                return fieldErrors
            }
            
            fun of(bindingResult: BindingResult): List<FieldError> = bindingResult.fieldErrors.map { error ->
                FieldError(error.field,
                           if (error.rejectedValue == null) "" else error.rejectedValue.toString(),
                           error.defaultMessage.toString())
            }
            
        }
        
    }
    
}