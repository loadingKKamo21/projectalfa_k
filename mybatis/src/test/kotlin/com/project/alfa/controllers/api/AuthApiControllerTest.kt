package com.project.alfa.controllers.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import com.project.alfa.config.security.TestSecurityConfig
import com.project.alfa.config.security.WithCustomMockUser
import com.project.alfa.services.JwtService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*
import javax.servlet.http.Cookie

@Import(TestSecurityConfig::class)
@WebMvcTest(AuthApiController::class)
internal class AuthApiControllerTest {
    
    @MockBean
    lateinit var jwtService: JwtService
    
    @Autowired
    lateinit var mockMvc: MockMvc
    lateinit var responseRefreshToken: String
    lateinit var responseAccessToken: String
    
    val gson: Gson = Gson()
    
    @BeforeEach
    fun setup() {
        val refreshExpirationMillis = System.currentTimeMillis() + (1000 * 604800)
        responseRefreshToken = JWT.create()
                .withClaim("USERNAME", "user1@mail.com")
                .withExpiresAt(Date(refreshExpirationMillis))
                .withIssuer("issuer")
                .sign(Algorithm.HMAC256("secret"))
        
        responseAccessToken = JWT.create()
                .withClaim("USERNAME", "user1@mail.com")
                .withExpiresAt(Date(System.currentTimeMillis() + (1000 * 1800)))
                .withIssuer("issuer")
                .sign(Algorithm.HMAC256("secret"))
        
        whenever(jwtService.refreshAccessToken(any<String>(), any<UserDetails>())).thenReturn(responseAccessToken)
        whenever(jwtService.generateRefreshToken(any<UserDetails>())).thenReturn(responseRefreshToken)
        whenever(jwtService.getExpirationFromToken(any<String>())).thenReturn(refreshExpirationMillis / 1000)
    }
    
    @Test
    @DisplayName("JWT Access 토큰 Refresh, 쿠키 사용")
    @WithCustomMockUser
    fun refreshTokenByCookie() {
        //Given
        val requestRefreshToken = JWT.create()
                .withClaim("USERNAME", "user1@mail.com")
                .withExpiresAt(Date(System.currentTimeMillis() + (1000 * 604800)))
                .withIssuer("issuer")
                .sign(Algorithm.HMAC256("secret"))
        
        //When
        val actions = mockMvc.perform(post("/api/auth/refresh")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .cookie(Cookie("refreshToken", requestRefreshToken)))
        
        //Then
        actions.andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer $responseAccessToken"))
                .andExpect(cookie().value("refreshToken", responseRefreshToken))
                .andExpect(content().string("Access Token refresh complete."))
                .andDo(print())
        
        verify(jwtService, times(1)).refreshAccessToken(any<String>(), any<UserDetails>())
        verify(jwtService, times(1)).generateRefreshToken(any<UserDetails>())
        verify(jwtService, times(1)).getExpirationFromToken(any<String>())
    }
    
    @Test
    @DisplayName("JWT Access 토큰 Refresh, 헤더 사용")
    @WithCustomMockUser
    fun refreshTokenByHeader() {
        //Given
        val requestRefreshToken = JWT.create()
                .withClaim("USERNAME", "user1@mail.com")
                .withExpiresAt(Date(System.currentTimeMillis() + (1000 * 604800)))
                .withIssuer("issuer")
                .sign(Algorithm.HMAC256("secret"))
        
        //When
        val actions = mockMvc.perform(post("/api/auth/refresh")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .header("Authorization", "Refresh $requestRefreshToken"))
        
        //Then
        actions.andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer $responseAccessToken"))
                .andExpect(cookie().value("refreshToken", responseRefreshToken))
                .andExpect(content().string("Access Token refresh complete."))
                .andDo(print())
        
        verify(jwtService, times(1)).refreshAccessToken(any<String>(), any<UserDetails>())
        verify(jwtService, times(1)).generateRefreshToken(any<UserDetails>())
        verify(jwtService, times(1)).getExpirationFromToken(any<String>())
    }
    
    @Test
    @DisplayName("JWT Access 토큰 Refresh, JSON 사용")
    @WithCustomMockUser
    fun refreshTokenByJson() {
        //Given
        val requestRefreshToken = JWT.create()
                .withClaim("USERNAME", "user1@mail.com")
                .withExpiresAt(Date(System.currentTimeMillis() + (1000 * 604800)))
                .withIssuer("issuer")
                .sign(Algorithm.HMAC256("secret"))
        val body: MutableMap<String, String> = HashMap()
        body["refreshToken"] = requestRefreshToken
        
        //When
        val actions = mockMvc.perform(post("/api/auth/refresh")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(body)))
        
        //Then
        actions.andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer $responseAccessToken"))
                .andExpect(cookie().value("refreshToken", responseRefreshToken))
                .andExpect(content().string("Access Token refresh complete."))
                .andDo(print())
        
        verify(jwtService, times(1)).refreshAccessToken(any<String>(), any<UserDetails>())
        verify(jwtService, times(1)).generateRefreshToken(any<UserDetails>())
        verify(jwtService, times(1)).getExpirationFromToken(any<String>())
    }
    
    @Test
    @DisplayName("JWT Access 토큰 Refresh, 토큰 없이 요청")
    @WithCustomMockUser
    fun refreshToken_noToken() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/auth/refresh")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
        
        //Then
        actions.andExpect(status().isBadRequest)
                .andExpect(content().string("Refresh Token is missing."))
                .andDo(print())
        
        verify(jwtService, never()).refreshAccessToken(any<String>(), any<UserDetails>())
        verify(jwtService, never()).generateRefreshToken(any<UserDetails>())
        verify(jwtService, never()).getExpirationFromToken(any<String>())
    }
    
}