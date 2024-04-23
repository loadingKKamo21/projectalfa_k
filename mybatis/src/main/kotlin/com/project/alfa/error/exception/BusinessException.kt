package com.project.alfa.error.exception

/**
 * https://cheese10yun.github.io/spring-guide-exception/
 */
open class BusinessException(message: String, val errorCode: ErrorCode) : RuntimeException(message) {
    constructor(errorCode: ErrorCode) : this(errorCode.message, errorCode)
}