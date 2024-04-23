package com.project.alfa.repositories

import com.project.alfa.entities.Post
import com.project.alfa.repositories.dto.SearchParam
import org.springframework.data.domain.Pageable
import java.util.*

interface PostRepository {
    
    fun save(post: Post): Post
    
    fun findById(id: Long): Optional<Post>
    
    fun findById(id: Long, deleteYn: Boolean): Optional<Post>
    
    fun findAll(): List<Post>
    
    fun findAll(deleteYn: Boolean): List<Post>
    
    fun findAll(ids: List<Long>): List<Post>
    
    fun findAll(ids: List<Long>, deleteYn: Boolean): List<Post>
    
    fun findAll(writerId: Long): List<Post>
    
    fun findAll(writerId: Long, deleteYn: Boolean): List<Post>
    
    fun findAll(pageable: Pageable): List<Post>
    
    fun findAll(deleteYn: Boolean, pageable: Pageable): List<Post>
    
    fun findAll(writerId: Long, pageable: Pageable): List<Post>
    
    fun findAll(writerId: Long, deleteYn: Boolean, pageable: Pageable): List<Post>
    
    fun findAll(param: SearchParam, pageable: Pageable): List<Post>
    
    fun findAll(param: SearchParam, deleteYn: Boolean, pageable: Pageable): List<Post>
    
    fun addViewCount(id: Long): Unit
    
    fun update(param: Post): Unit
    
    fun existsById(id: Long): Boolean
    
    fun existsById(id: Long, deleteYn: Boolean): Boolean
    
    fun deleteById(id: Long, writerId: Long): Unit
    
    fun permanentlyDeleteById(id: Long): Unit
    
    fun deleteAllByIds(ids: List<Long>, writerId: Long): Unit
    
    fun permanentlyDeleteAllByIds(ids: List<Long>): Unit
    
}