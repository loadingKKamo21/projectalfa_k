package com.project.alfa.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.project.alfa.config.redis.EmbeddedRedisConfig
import com.project.alfa.config.security.WithCustomMockUser
import com.project.alfa.security.CustomUserDetails
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import java.util.*

@Import(EmbeddedRedisConfig::class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class JwtServiceTest {
    
    companion object {
        val USERNAME_KEY = "USERNAME"
    }
    
    @Value("\${jwt.secret}")
    lateinit var secret: String
    
    @Value("\${jwt.issuer}")
    lateinit var issuer: String
    
    @Value("\${jwt.token.access-expiration}")
    var accessExpiration: Long? = null
    
    @Value("\${jwt.token.refresh-expiration}")
    var refreshExpiration: Long? = null
    
    @Autowired
    lateinit var jwtService: JwtService
    
    @Autowired
    lateinit var redisTemplate: StringRedisTemplate
    lateinit var algorithm: Algorithm
    
    @BeforeEach
    fun setup() {
        algorithm = Algorithm.HMAC256(secret)
    }
    
    @Test
    @DisplayName("JWT Access 토큰 생성")
    @WithCustomMockUser
    fun generateAccessToken() {
        //Given
        val userDetails = SecurityContextHolder.getContext().authentication.principal as CustomUserDetails
        
        //When
        val accessToken = jwtService.generateAccessToken(userDetails)
        
        //Then
        val username = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
                .verify(accessToken)
                .getClaim(USERNAME_KEY)
                .asString()
        
        assertThat(userDetails.username).isEqualTo(username)
    }
    
    @Test
    @DisplayName("JWT Refresh 토큰 생성")
    @WithCustomMockUser
    fun generateRefreshToken() {
        //Given
        val userDetails = SecurityContextHolder.getContext().authentication.principal as CustomUserDetails
        
        //When
        val refreshToken = jwtService.generateRefreshToken(userDetails)
        
        //Then
        val username = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
                .verify(refreshToken)
                .getClaim(USERNAME_KEY)
                .asString()
        val storedRefreshToken = redisTemplate.opsForValue().get(userDetails.username)
        
        assertThat(userDetails.username).isEqualTo(username)
        assertThat(refreshToken).isEqualTo(storedRefreshToken)
    }
    
    @Test
    @DisplayName("JWT 토큰으로 아이디 조회")
    @WithCustomMockUser
    fun getUsernameFromToken() {
        //Given
        val userDetails = SecurityContextHolder.getContext().authentication.principal as CustomUserDetails
        
        val token = JWT.create()
                .withClaim(USERNAME_KEY, userDetails.username)
                .withExpiresAt(Date(System.currentTimeMillis() + (1000 * accessExpiration!!)))
                .withIssuer(issuer)
                .sign(algorithm)
        
        //When
        val usernameFromToken = jwtService.getUsernameFromToken(token)
        
        //Then
        assertThat(userDetails.username).isEqualTo(usernameFromToken)
    }
    
    @Test
    @DisplayName("JWT 토큰 검증")
    @WithCustomMockUser
    fun validateToken() {
        //Given
        val userDetails = SecurityContextHolder.getContext().authentication.principal as CustomUserDetails
        
        val token = JWT.create()
                .withClaim(USERNAME_KEY, userDetails.username)
                .withExpiresAt(Date(System.currentTimeMillis() + (1000 * accessExpiration!!)))
                .withIssuer(issuer)
                .sign(algorithm)
        
        //When
        val result = jwtService.validateToken(token, userDetails)
        
        //Then
        assertThat(result).isTrue
    }
    
    @Test
    @DisplayName("JWT 토큰 검증, 알 수 없는 토큰")
    @WithCustomMockUser
    fun validateToken_unknownToken() {
        //Given
        val userDetails = SecurityContextHolder.getContext().authentication.principal as CustomUserDetails
        
        val token = JWT.create()
                .withClaim(USERNAME_KEY, "user2@mail.com")
                .withExpiresAt(Date(System.currentTimeMillis() + (1000 * accessExpiration!!)))
                .withIssuer(issuer)
                .sign(algorithm)
        
        //When
        val result = jwtService.validateToken(token, userDetails)
        
        //Then
        assertThat(result).isFalse
    }
    
    @Test
    @DisplayName("JWT Access 토큰 Refresh")
    @WithCustomMockUser
    fun refreshAccessToken() {
        //Given
        val userDetails = SecurityContextHolder.getContext().authentication.principal as CustomUserDetails
        val refreshToken = jwtService.generateRefreshToken(userDetails)
        val storedRefreshToken = redisTemplate.opsForValue().get(userDetails.username)
        if (refreshToken != storedRefreshToken)
            fail("Invalid Value")
        
        //When
        val accessToken = jwtService.refreshAccessToken(refreshToken, userDetails)
        
        //Then
        assertThat(accessToken).isNotNull
    }
    
    @Test
    @DisplayName("JWT Access 토큰 Refresh, 알 수 없는 토큰")
    @WithCustomMockUser
    fun refreshAccessToken_unknownToken() {
        //Given
        val userDetails = SecurityContextHolder.getContext().authentication.principal as CustomUserDetails
        val refreshToken = jwtService.generateRefreshToken(userDetails)
        val storedRefreshToken = redisTemplate.opsForValue().get(userDetails.username)
        if (refreshToken != storedRefreshToken)
            fail("Invalid Value")
        
        val unknownToken = JWT.create()
                .withClaim(USERNAME_KEY, "user2@mail.com")
                .withExpiresAt(Date(System.currentTimeMillis() + (1000 * refreshExpiration!!)))
                .withIssuer(issuer)
                .sign(algorithm)
        
        //When
        
        //Then
        assertThatThrownBy { jwtService.refreshAccessToken(unknownToken, userDetails) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("Invalid Refresh Token")
    }
    
    @Test
    @DisplayName("JWT 토큰 만료 시간 조회")
    fun getExpirationFromToken() {
        //Given
        val expirationMillis = System.currentTimeMillis() + (1000 * accessExpiration!!)
        val token = JWT.create()
                .withClaim(USERNAME_KEY, "user1@mail.com")
                .withExpiresAt(Date(expirationMillis))
                .withIssuer(issuer)
                .sign(algorithm)
        val expiration = expirationMillis / 1000
        
        //When
        val expirationFromToken = jwtService.getExpirationFromToken(token)
        
        //Then
        assertThat(expirationFromToken).isEqualTo(expiration)
    }
    
    @Test
    @DisplayName("Redis 서버에서 JWT Refresh 토큰 삭제")
    @WithCustomMockUser
    fun deleteRefreshToken() {
        //Given
        val userDetails = SecurityContextHolder.getContext().authentication.principal as CustomUserDetails
        val refreshToken = jwtService.generateRefreshToken(userDetails)
        val beforeStoredRefreshToken = redisTemplate.opsForValue().get(userDetails.username)
        if (refreshToken != beforeStoredRefreshToken)
            fail("Invalid Value")
        
        //When
        jwtService.deleteRefreshToken(refreshToken)
        
        //Then
        val afterStoredRefreshToken = redisTemplate.opsForValue().get(userDetails.username)
        
        assertThat(afterStoredRefreshToken).isNull()
    }
    
}