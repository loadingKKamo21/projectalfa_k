package com.project.alfa.repositories

import com.project.alfa.entities.Comment
import org.springframework.data.domain.Pageable
import java.util.*

interface CommentRepository {
    
    fun save(comment: Comment): Comment
    
    fun findById(id: Long): Optional<Comment>
    
    fun findById(id: Long, deleteYn: Boolean): Optional<Comment>
    
    fun findAll(): List<Comment>
    
    fun findAll(deleteYn: Boolean): List<Comment>
    
    fun findAll(ids: List<Long>): List<Comment>
    
    fun findAll(ids: List<Long>, deleteYn: Boolean): List<Comment>
    
    fun findAllByWriter(writerId: Long): List<Comment>
    
    fun findAllByWriter(writerId: Long, deleteYn: Boolean): List<Comment>
    
    fun findAllByPost(postId: Long): List<Comment>
    
    fun findAllByPost(postId: Long, deleteYn: Boolean): List<Comment>
    
    fun findAllByWriter(writerId: Long, pageable: Pageable): List<Comment>
    
    fun findAllByWriter(writerId: Long, deleteYn: Boolean, pageable: Pageable): List<Comment>
    
    fun findAllByPost(postId: Long, pageable: Pageable): List<Comment>
    
    fun findAllByPost(postId: Long, deleteYn: Boolean, pageable: Pageable): List<Comment>
    
    fun update(param: Comment): Unit
    
    fun deleteById(id: Long, writerId: Long): Unit
    
    fun permanentlyDeleteById(id: Long): Unit
    
    fun deleteAllByIds(ids: List<Long>, writerId: Long): Unit
    
    fun permanentlyDeleteAllByIds(ids: List<Long>): Unit
    
}