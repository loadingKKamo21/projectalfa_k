package com.project.alfa.repositories.v2

import com.project.alfa.entities.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.*

interface MemberJpaRepository : JpaRepository<Member, Long> {
    
    fun findByIdAndDeleteYn(id: Long, deleteYn: Boolean): Optional<Member>
    
    fun findByUsername(username: String): Optional<Member>
    
    fun findByUsernameAndDeleteYn(username: String, deleteYn: Boolean): Optional<Member>
    
    fun findAllByAuthInfo_Auth(auth: Boolean): List<Member>
    
    fun findAllByDeleteYn(deleteYn: Boolean): List<Member>
    
    fun findByUsernameAndAuthInfo_EmailAuthTokenAndAuthInfo_EmailAuthExpireTimeGreaterThanEqualAndDeleteYnFalse(
            username: String, emailAuthToken: String, authenticatedTime: LocalDateTime): Optional<Member>
    
    fun findByUsernameAndAuthInfo_oAuthProviderAndAuthInfo_oAuthProviderIdAndDeleteYnFalse(username: String,
                                                                                           oAuthProvider: String,
                                                                                           oAuthProviderId: String): Optional<Member>
    
    fun existsByUsername(username: String): Boolean
    
    fun existsByUsernameAndDeleteYn(username: String, deleteYn: Boolean): Boolean
    
    fun existsByNickname(nickname: String): Boolean
    
    fun existsByNicknameAndDeleteYn(nickname: String, deleteYn: Boolean): Boolean
    
}