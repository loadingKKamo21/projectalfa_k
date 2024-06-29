package com.project.alfa.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

private const val USERNAME_KEY = "USERNAME"

@Service
class JwtService(private val redisTemplate: StringRedisTemplate) {
    
    @Value("\${jwt.secret}")
    private lateinit var secret: String
    
    @Value("\${jwt.issuer}")
    private lateinit var issuer: String
    
    @Value("\${jwt.token.access-expiration}")
    private var accessExpiration: Long? = null
    
    @Value("\${jwt.token.refresh-expiration}")
    private var refreshExpiration: Long? = null
    
    lateinit var algorithm: Algorithm
    
    @PostConstruct
    fun postConstruct() {
        algorithm = Algorithm.HMAC256(secret)
    }
    
    /**
     * JWT Access 토큰 생성
     *
     * @param userDetails - 인증 정보
     * @return JWT Access 토큰
     */
    fun generateAccessToken(userDetails: UserDetails): String = JWT.create()
            .withClaim(USERNAME_KEY, userDetails.username)
            .withExpiresAt(Date(System.currentTimeMillis() + (1000 * accessExpiration!!)))
            .withIssuer(issuer)
            .sign(algorithm)
    
    /**
     * JWT Refresh 토큰 생성 및 Redis 서버 저장
     *
     * @param userDetails - 인증 정보
     * @return JWT Refresh 토큰
     */
    fun generateRefreshToken(userDetails: UserDetails): String {
        val refreshToken = JWT.create()
                .withClaim(USERNAME_KEY, userDetails.username)
                .withExpiresAt(Date(System.currentTimeMillis() + (1000 * refreshExpiration!!)))
                .withIssuer(issuer)
                .sign(algorithm)
        redisTemplate.opsForValue().set(userDetails.username,
                                        refreshToken,
                                        refreshExpiration!!,
                                        TimeUnit.MILLISECONDS)
        return refreshToken
    }
    
    /**
     * JWT 토큰으로 아이디 조회
     *
     * @param token - JWT 토큰
     * @return 아이디
     */
    fun getUsernameFromToken(token: String): String = JWT.require(algorithm)
            .withIssuer(issuer)
            .build()
            .verify(token)
            .getClaim(USERNAME_KEY)
            .asString()
    
    /**
     * JWT 토큰 검증
     *
     * @param token       - JWT 토큰
     * @param userDetails - 인증 정보
     * @return 검증 결과
     */
    fun validateToken(token: String, userDetails: UserDetails): Boolean =
            getUsernameFromToken(token) == userDetails.username && !isTokenExpired(token)
    
    /**
     * JWT Refresh 토큰으로 Access 토큰 갱신
     *
     * @param refreshToken - JWT Refresh 토큰
     * @param userDetails  - 인증 정보
     * @return 신규 JWT Access 토큰
     */
    fun refreshAccessToken(refreshToken: String, userDetails: UserDetails): String {
        val username = getUsernameFromToken(refreshToken)
        val storedRefreshToken = redisTemplate.opsForValue().get(username)
        
        if (storedRefreshToken.isNullOrBlank() || storedRefreshToken != refreshToken)
            throw java.lang.RuntimeException("Invalid Refresh Token")
        
        return generateAccessToken(userDetails)
    }
    
    /**
     * JWT 토큰 만료 시간 조회
     *
     * @param token - JWT 토큰
     * @return 만료 시간(초)
     */
    fun getExpirationFromToken(token: String): Long =
            JWT.require(algorithm).withIssuer(issuer).build().verify(token).expiresAt.time / 1000
    
    /**
     * Redis 서버에서 JWT Refresh 토큰 삭제
     *
     * @param refreshToken - JWT Refresh 토큰
     */
    fun deleteRefreshToken(refreshToken: String): Unit {
        redisTemplate.delete(getUsernameFromToken(refreshToken))
    }
    
    /**
     * JWT 토큰 만료 여부 확인
     *
     * @param token - JWT 토큰
     * @return 만료 여부
     */
    private fun isTokenExpired(token: String): Boolean =
            JWT.require(algorithm).withIssuer(issuer).build().verify(token).expiresAt.before(Date())
    
}