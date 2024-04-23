package com.project.alfa.repositories.v3

import com.project.alfa.entities.Comment
import com.project.alfa.repositories.v3.querydsl.CommentRepositoryV3Custom
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface CommentRepositoryV3 : JpaRepository<Comment, Long>, CommentRepositoryV3Custom {
    
    override fun findById(id: Long): Optional<Comment>
    
    @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.deleteYn = :deleteYn")
    fun findById(@Param("id") id: Long, @Param("deleteYn") deleteYn: Boolean): Optional<Comment>
    
    @Query("SELECT c from Comment c WHERE c.deleteYn = :deleteYn")
    fun findAll(@Param("deleteYn") deleteYn: Boolean): List<Comment>
    
    @Query("SELECT c FROM Comment c WHERE c.id IN :ids")
    fun findAll(@Param("ids") ids: List<Long>): List<Comment>
    
    @Query("SELECT c FROM Comment c WHERE c.id IN :ids AND c.deleteYn = :deleteYn")
    fun findAll(@Param("ids") ids: List<Long>, @Param("deleteYn") deleteYn: Boolean): List<Comment>
    
    @Query("SELECT c FROM Comment c WHERE c.writer.id = :writerId")
    fun findAllByWriter(@Param("writerId") writerId: Long): List<Comment>
    
    @Query("SELECT c FROM Comment c WHERE c.writer.id = :writerId AND c.deleteYn = :deleteYn")
    fun findAllByWriter(@Param("writerId") writerId: Long, @Param("deleteYn") deleteYn: Boolean): List<Comment>
    
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId")
    fun findAllByPost(@Param("postId") postId: Long): List<Comment>
    
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.deleteYn = :deleteYn")
    fun findAllByPost(@Param("postId") postId: Long, @Param("deleteYn") deleteYn: Boolean): List<Comment>
    
    @Query("SELECT c FROM Comment c ORDER BY c.createdDate DESC")
    override fun findAll(pageable: Pageable): Page<Comment>
    
    @Query("SELECT c FROM Comment c WHERE c.writer.id = :writerId ORDER BY c.createdDate DESC")
    fun findAllByWriter(@Param("writerId") writerId: Long, pageable: Pageable): Page<Comment>
    
    @Query("SELECT c FROM Comment c WHERE c.writer.id = :writerId AND c.deleteYn = :deleteYn ORDER BY c.createdDate DESC")
    fun findAllByWriter(@Param("writerId") writerId: Long,
                        @Param("deleteYn") deleteYn: Boolean,
                        pageable: Pageable): Page<Comment>
    
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.createdDate DESC")
    fun findAllByPost(@Param("postId") postId: Long, pageable: Pageable): Page<Comment>
    
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.deleteYn = :deleteYn ORDER BY c.createdDate DESC")
    fun findAllByPost(@Param("postId") postId: Long,
                      @Param("deleteYn") deleteYn: Boolean,
                      pageable: Pageable): Page<Comment>
    
}