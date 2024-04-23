package com.project.alfa.repositories.v3

import com.project.alfa.entities.Member
import com.project.alfa.repositories.v3.querydsl.MemberRepositoryV3Custom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface MemberRepositoryV3 : JpaRepository<Member, Long>, MemberRepositoryV3Custom {
    
    override fun findById(id: Long): Optional<Member>
    
    @Query("SELECT m FROM Member m WHERE m.id = :id AND m.deleteYn = :deleteYn")
    fun findById(@Param("id") id: Long, @Param("deleteYn") deleteYn: Boolean): Optional<Member>
    
    fun findByUsername(username: String): Optional<Member>
    
    @Query("SELECT m FROM Member m WHERE m.username = :username AND m.deleteYn = :deleteYn")
    fun findByUsername(@Param("username") username: String, @Param("deleteYn") deleteYn: Boolean): Optional<Member>
    
    @Query("SELECT m FROM Member m WHERE m.authInfo.auth = :auth")
    fun findAllByAuth(@Param("auth") auth: Boolean): List<Member>
    
    fun findAllByDeleteYn(deleteYn: Boolean): List<Member>
    
    @Query("SELECT m FROM Member m WHERE m.username = :username AND m.authInfo.emailAuthToken = :emailAuthToken AND m.authInfo.emailAuthExpireTime >= :authenticatedTime AND m.deleteYn = false")
    fun authenticateEmail(@Param("username") username: String,
                          @Param("emailAuthToken") emailAuthToken: String,
                          @Param("authenticatedTime") authenticatedTime: LocalDateTime): Optional<Member>
    
    @Query("SELECT m FROM Member m WHERE m.username = :username AND m.authInfo.oAuthProvider = :provider AND m.authInfo.oAuthProviderId = :providerId AND m.deleteYn = false")
    fun authenticateOAuth(@Param("username") username: String,
                          @Param("provider") provider: String,
                          @Param("providerId") providerId: String): Optional<Member>
    
    fun existsByUsername(username: String): Boolean
    
    @Query("SELECT CASE WHEN (COUNT(m) > 0) THEN TRUE ELSE FALSE END FROM Member m WHERE m.username = :username AND m.deleteYn = :deleteYn")
    fun existsByUsername(@Param("username") username: String, @Param("deleteYn") deleteYn: Boolean): Boolean
    
    fun existsByNickname(nickname: String): Boolean
    
    @Query("SELECT CASE WHEN (COUNT(m) > 0) THEN TRUE ELSE FALSE END FROM Member m WHERE m.nickname = :nickname AND m.deleteYn = :deleteYn")
    fun existsByNickname(@Param("nickname") nickname: String, @Param("deleteYn") deleteYn: Boolean): Boolean
}