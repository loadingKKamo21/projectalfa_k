package com.project.alfa.security.jwt.filter

import com.google.gson.Gson
import com.project.alfa.security.CustomUserDetails
import com.project.alfa.security.jwt.filter.dto.LoginBody
import com.project.alfa.services.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(
        private val authenticationManager: AuthenticationManager,
        private val jwtService: JwtService
) : UsernamePasswordAuthenticationFilter() {
    
    private val gson = Gson()
    
    init {
        setFilterProcessesUrl("/login")
    }
    
    override fun attemptAuthentication(request: HttpServletRequest?, response: HttpServletResponse?): Authentication {
        val loginBody = gson.fromJson(request!!.reader, LoginBody::class.java)
        
        val authentication = UsernamePasswordAuthenticationToken(loginBody.username, loginBody.password)
        
        return authenticationManager.authenticate(authentication)
    }
    
    override fun successfulAuthentication(request: HttpServletRequest?, response: HttpServletResponse?,
                                          chain: FilterChain?, authResult: Authentication?) {
        val userDetails = authResult!!.principal as CustomUserDetails
        
        val accessToken = jwtService.generateAccessToken(userDetails)
        val refreshToken = jwtService.generateRefreshToken(userDetails)
        
        response!!.setHeader("Authorization", "Bearer $accessToken")
        
        //1. RefreshToken 쿠키로 전달
        val cookie = Cookie("refreshToken", refreshToken)
        cookie.isHttpOnly = true
        cookie.path = "/"
        cookie.maxAge = jwtService.getExpirationFromToken(refreshToken).toInt()
        
        response.addCookie(cookie)
        
        //2. RefreshToken JSON으로 전달
//        response.contentType = "application/json"
//        response.characterEncoding = "UTF-8"
//        response.writer.write(gson.toJson(refreshToken))
    }
    
    override fun unsuccessfulAuthentication(request: HttpServletRequest?, response: HttpServletResponse?,
                                            failed: AuthenticationException?) {
        response!!.status = HttpServletResponse.SC_UNAUTHORIZED
        response.writer.write("Authentication failed")
    }
    
}
