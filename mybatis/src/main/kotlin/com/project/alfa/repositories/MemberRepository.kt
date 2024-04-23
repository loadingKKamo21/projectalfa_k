package com.project.alfa.repositories

import com.project.alfa.entities.Member
import java.time.LocalDateTime
import java.util.*

interface MemberRepository {
    
    fun save(member: Member): Member
    
    fun findById(id: Long): Optional<Member>
    
    fun findById(id: Long, deleteYn: Boolean): Optional<Member>
    
    fun findByUsername(username: String): Optional<Member>
    
    fun findByUsername(username: String, deleteYn: Boolean): Optional<Member>
    
    fun findAll(): List<Member>
    
    fun findAllByAuth(auth: Boolean): List<Member>
    
    fun findAllByDeleteYn(deleteYn: Boolean): List<Member>
    
    fun authenticateEmail(username: String, emailAuthToken: String, authenticatedTime: LocalDateTime): Unit
    
    fun authenticateOAuth(username: String,
                          provider: String,
                          providerId: String,
                          authenticatedTime: LocalDateTime): Unit
    
    fun update(param: Member): Unit
    
    fun existsById(id: Long): Boolean
    
    fun existsById(id: Long, deleteYn: Boolean): Boolean
    
    fun existsByUsername(username: String): Boolean
    
    fun existsByUsername(username: String, deleteYn: Boolean): Boolean
    
    fun existsByNickname(nickname: String): Boolean
    
    fun existsByNickname(nickname: String, deleteYn: Boolean): Boolean
    
    fun deleteById(id: Long): Unit
    
    fun permanentlyDeleteById(id: Long): Unit
    
}