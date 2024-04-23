package com.project.alfa.repositories.v3

import com.project.alfa.entities.Attachment
import com.project.alfa.repositories.v3.querydsl.AttachmentRepositoryV3Custom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface AttachmentRepositoryV3 : JpaRepository<Attachment, Long>, AttachmentRepositoryV3Custom {
    
    override fun findById(id: Long): Optional<Attachment>
    
    @Query("SELECT a FROM Attachment a WHERE a.id = :id AND a.deleteYn = :deleteYn")
    fun findById(@Param("id") id: Long, @Param("deleteYn") deleteYn: Boolean): Optional<Attachment>
    
    @Query("SELECT a FROM Attachment a WHERE a.deleteYn = :deleteYn")
    fun findAll(@Param("deleteYn") deleteYn: Boolean): List<Attachment>
    
    @Query("SELECT a FROM Attachment a WHERE a.id IN :ids")
    fun findAll(@Param("ids") ids: List<Long>): List<Attachment>
    
    @Query("SELECT a FROM Attachment a WHERE a.id IN :ids AND a.deleteYn = :deleteYn")
    fun findAll(@Param("ids") ids: List<Long>, @Param("deleteYn") deleteYn: Boolean): List<Attachment>
    
    @Query("SELECT a FROM Attachment a WHERE a.post.id = :postId")
    fun findAll(@Param("postId") postId: Long): List<Attachment>
    
    @Query("SELECT a FROM Attachment a WHERE a.post.id = :postId AND a.deleteYn = :deleteYn")
    fun findAll(@Param("postId") postId: Long, @Param("deleteYn") deleteYn: Boolean): List<Attachment>
    
}