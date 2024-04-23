package com.project.alfa.security

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class CustomAuthenticationFailureHandler : AuthenticationFailureHandler {
    
    override fun onAuthenticationFailure(request: HttpServletRequest?, response: HttpServletResponse?,
                                         exception: AuthenticationException?) {
        val message: String = when (exception) {
            is BadCredentialsException -> "Invalid username or password"
            is UsernameNotFoundException -> "Account do not exist"
            is LockedException -> "Account do not complete authentication"
            is InternalAuthenticationServiceException -> "The request could not be processed due to an internal error"
            is AuthenticationCredentialsNotFoundException -> "Authentication request denied"
            else -> "Login failed for unknown reason"
        }
        
//        response!!.sendRedirect("/login?error=${URLEncoder.encode(message, "UTF-8")}")
        response!!.status = HttpStatus.UNAUTHORIZED.value()
        response.writer.write(message)
    }
    
}