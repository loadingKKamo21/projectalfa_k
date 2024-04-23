package com.project.alfa.config.security

import com.project.alfa.entities.Role
import com.project.alfa.security.CustomUserDetails
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithCustomSecurityContextFactory : WithSecurityContextFactory<WithCustomMockUser> {
    
    override fun createSecurityContext(annotation: WithCustomMockUser?): SecurityContext {
        val userDetails = CustomUserDetails(id = annotation!!.id,
                                            username = annotation.username,
                                            password = annotation.password,
                                            auth = annotation.auth,
                                            nickname = annotation.nickname,
                                            role = findByValue(annotation.role).value)
        
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken(userDetails,
                                                                             annotation.password,
                                                                             userDetails.authorities)
        
        return securityContext
    }
    
    private fun findByValue(value: String): Role {
        var role: Role? = null
        for (roleValue in Role.values())
            if (roleValue.value.contains(value)) {
                role = roleValue
                break
            }
        return role!!
    }
    
}
