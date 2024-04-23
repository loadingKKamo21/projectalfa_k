package com.project.alfa.repositories.mybatis

import com.project.alfa.entities.Member
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.LocalDateTime

@Mapper
interface MemberMapper {
    
    fun save(member: Member): Unit
    
    fun findById(id: Long): Member
    
    fun findByIdAndDeleteYn(@Param("id") id: Long, @Param("deleteYn") deleteYn: Boolean): Member
    
    fun findByUsername(username: String): Member
    
    fun findByUsernameAndDeleteYn(@Param("username") username: String, @Param("deleteYn") deleteYn: Boolean): Member
    
    fun findAll(): List<Member>
    
    fun findAllByAuth(auth: Boolean): List<Member>
    
    fun findAllByDeleteYn(deleteYn: Boolean): List<Member>
    
    fun update(param: Member): Unit
    
    fun authenticateEmail(@Param("username") username: String,
                          @Param("emailAuthToken") emailAuthToken: String,
                          @Param("authenticatedTime") authenticatedTime: LocalDateTime): Unit
    
    fun authenticateOAuth(@Param("username") username: String,
                          @Param("provider") provider: String,
                          @Param("providerId") providerId: String,
                          @Param("authenticatedTime") authenticatedTime: LocalDateTime): Unit
    
    fun existsById(id: Long): Boolean
    
    fun existsByIdAndDeleteYn(@Param("id") id: Long, @Param("deleteYn") deleteYn: Boolean): Boolean
    
    fun existsByUsername(username: String): Boolean
    
    fun existsByUsernameAndDeleteYn(@Param("username") username: String, @Param("deleteYn") deleteYn: Boolean): Boolean
    
    fun existsByNickname(nickname: String): Boolean
    
    fun existsByNicknameAndDeleteYn(@Param("nickname") nickname: String, @Param("deleteYn") deleteYn: Boolean): Boolean
    
    fun deleteById(id: Long): Unit
    
    fun permanentlyDeleteById(id: Long): Unit
    
}