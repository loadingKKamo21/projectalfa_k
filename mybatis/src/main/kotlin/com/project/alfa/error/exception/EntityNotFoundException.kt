package com.project.alfa.error.exception

/**
 * https://cheese10yun.github.io/spring-guide-exception/
 */
class EntityNotFoundException(message: String) : BusinessException(message, ErrorCode.ENTITY_NOT_FOUND)