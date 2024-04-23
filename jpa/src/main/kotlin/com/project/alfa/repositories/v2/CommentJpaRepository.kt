package com.project.alfa.repositories.v2

import com.project.alfa.entities.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CommentJpaRepository : JpaRepository<Comment, Long> {
    
    fun findByIdAndDeleteYn(id: Long, deleteYn: Boolean): Optional<Comment>
    
    fun findAllByDeleteYn(deleteYn: Boolean): List<Comment>
    
    fun findAllByIdIn(ids: List<Long>): List<Comment>
    
    fun findAllByIdInAndDeleteYn(ids: List<Long>, deleteYn: Boolean): List<Comment>
    
    fun findAllByWriter_Id(writerId: Long): List<Comment>
    
    fun findAllByWriter_IdAndDeleteYn(writerId: Long, deleteYn: Boolean): List<Comment>
    
    fun findAllByPost_Id(postId: Long): List<Comment>
    
    fun findAllByPost_IdAndDeleteYn(postId: Long, deleteYn: Boolean): List<Comment>
    
    fun findAllByWriter_Id(writerId: Long, pageable: Pageable): Page<Comment>
    
    fun findAllByWriter_IdOrderByCreatedDateDesc(writerId: Long, pageable: Pageable): Page<Comment>
    
    fun findAllByWriter_IdAndDeleteYn(writerId: Long, deleteYn: Boolean, pageable: Pageable): Page<Comment>
    
    fun findAllByWriter_IdAndDeleteYnOrderByCreatedDateDesc(writerId: Long,
                                                            deleteYn: Boolean,
                                                            pageable: Pageable): Page<Comment>
    
    fun findAllByPost_Id(postId: Long, pageable: Pageable): Page<Comment>
    
    fun findAllByPost_IdOrderByCreatedDateDesc(postId: Long, pageable: Pageable): Page<Comment>
    
    fun findAllByPost_IdAndDeleteYn(postId: Long, deleteYn: Boolean, pageable: Pageable): Page<Comment>
    
    fun findAllByPost_IdAndDeleteYnOrderByCreatedDateDesc(postId: Long,
                                                          deleteYn: Boolean,
                                                          pageable: Pageable): Page<Comment>
    
}
