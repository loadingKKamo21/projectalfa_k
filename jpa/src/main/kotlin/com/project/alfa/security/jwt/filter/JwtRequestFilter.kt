package com.project.alfa.security.jwt.filter

import com.project.alfa.security.CustomUserDetails
import com.project.alfa.services.JwtService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtRequestFilter(
        private val userDetailsService: UserDetailsService,
        private val jwtService: JwtService
) : OncePerRequestFilter() {
    
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse,
                                  filterChain: FilterChain) {
        val header: String? = request.getHeader("Authorization")
        
        if (!header.isNullOrBlank() && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            val username = jwtService.getUsernameFromToken(token)
            
            if (!username.isNullOrBlank() && SecurityContextHolder.getContext().authentication == null) {
                val userDetails = userDetailsService.loadUserByUsername(username) as CustomUserDetails
                
                if (jwtService.validateToken(token, userDetails)) {
                    val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        }
        
        filterChain.doFilter(request, response)
    }
    
}
