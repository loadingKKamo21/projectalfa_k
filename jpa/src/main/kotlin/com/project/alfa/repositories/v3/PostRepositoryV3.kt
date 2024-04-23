package com.project.alfa.repositories.v3

import com.project.alfa.entities.Post
import com.project.alfa.repositories.v3.querydsl.PostRepositoryV3Custom
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface PostRepositoryV3 : JpaRepository<Post, Long>, PostRepositoryV3Custom {
    
    override fun findById(id: Long): Optional<Post>
    
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.deleteYn = :deleteYn")
    fun findById(@Param("id") id: Long, @Param("deleteYn") deleteYn: Boolean): Optional<Post>
    
    @Query("SELECT p FROM Post p WHERE p.deleteYn = :deleteYn")
    fun findAll(@Param("deleteYn") deleteYn: Boolean): List<Post>
    
    @Query("SELECT p FROM Post p WHERE p.id IN :ids")
    fun findAll(@Param("ids") ids: List<Long>): List<Post>
    
    @Query("SELECT p FROM Post p WHERE p.id IN :ids AND p.deleteYn = :deleteYn")
    fun findAll(@Param("ids") ids: List<Long>, @Param("deleteYn") deleteYn: Boolean): List<Post>
    
    @Query("SELECT p FROM Post p WHERE p.writer.id = :writerId")
    fun findAll(@Param("writerId") writerId: Long): List<Post>
    
    @Query("SELECT p FROM Post p WHERE p.writer.id = :writerId AND p.deleteYn = :deleteYn")
    fun findAll(@Param("writerId") writerId: Long, @Param("deleteYn") deleteYn: Boolean): List<Post>
    
    @Query("SELECT p FROM Post p ORDER BY p.createdDate DESC")
    override fun findAll(pageable: Pageable): Page<Post>
    
    @Query("SELECT p FROM Post p WHERE p.deleteYn = :deleteYn ORDER BY p.createdDate DESC")
    fun findAll(@Param("deleteYn") deleteYn: Boolean, pageable: Pageable): Page<Post>
    
    @Query("SELECT p FROM Post p WHERE p.writer.id = :writerId ORDER BY p.createdDate DESC")
    fun findAll(@Param("writerId") writerId: Long, pageable: Pageable): Page<Post>
    
    @Query("SELECT p FROM Post p WHERE p.writer.id = :writerId AND p.deleteYn = :deleteYn ORDER BY p.createdDate DESC")
    fun findAll(@Param("writerId") writerId: Long, @Param("deleteYn") deleteYn: Boolean, pageable: Pageable): Page<Post>
    
}