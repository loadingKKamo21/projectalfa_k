package com.project.alfa.security

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationProvider(
        val userDetailsService: CustomUserDetailsService,
        val passwordEncoder: PasswordEncoder
) : AuthenticationProvider {
    
    override fun authenticate(authentication: Authentication?): Authentication {
        val username: String = authentication!!.name
        val password: String = authentication.credentials.toString()
        
        val userDetails: CustomUserDetails = userDetailsService.loadUserByUsername(username) as CustomUserDetails
        
        if (!passwordEncoder.matches(password, userDetails.password))
            throw BadCredentialsException("Password do not match")
        
        if (!userDetails.auth)
            throw LockedException("Account do not complete authentication")
        
        return UsernamePasswordAuthenticationToken(userDetails, password, userDetails.authorities)
    }
    
    override fun supports(authentication: Class<*>?): Boolean =
            authentication!! == UsernamePasswordAuthenticationToken::class.java
    
}