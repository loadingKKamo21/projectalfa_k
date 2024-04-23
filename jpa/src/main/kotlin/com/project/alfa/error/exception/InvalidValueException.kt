package com.project.alfa.error.exception

/**
 * https://cheese10yun.github.io/spring-guide-exception/
 */
class InvalidValueException(value: String, errorCode: ErrorCode = ErrorCode.INVALID_INPUT_VALUE) :
        BusinessException(value, errorCode)