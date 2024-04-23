package com.project.alfa.config.security

import org.springframework.security.test.context.support.WithSecurityContext

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithCustomSecurityContextFactory::class)
annotation class WithCustomMockUser(
        val id: Long = 1L,
        val username: String = "user1@mail.com",
        val password: String = "Password1!@",
        val auth: Boolean = true,
        val nickname: String = "user1",
        val role: String = "ROLE_USER"
)