package com.project.alfa.repositories.v2

import com.project.alfa.entities.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.*

interface PostJpaRepository : JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    
    fun findByIdAndDeleteYn(id: Long, deleteYn: Boolean): Optional<Post>
    
    fun findAllByDeleteYn(deleteYn: Boolean): List<Post>
    
    fun findAllByIdIn(ids: List<Long>): List<Post>
    
    fun findAllByIdInAndDeleteYn(ids: List<Long>, deleteYn: Boolean): List<Post>
    
    fun findAllByWriter_Id(writerId: Long): List<Post>
    
    fun findAllByWriter_IdAndDeleteYn(writerId: Long, deleteYn: Boolean): List<Post>
    
    fun findAllByOrderByCreatedDateDesc(pageable: Pageable): Page<Post>
    
    fun findAllByDeleteYn(deleteYn: Boolean, pageable: Pageable): Page<Post>
    
    fun findAllByDeleteYnOrderByCreatedDateDesc(deleteYn: Boolean, pageable: Pageable): Page<Post>
    
    fun findAllByWriter_Id(writerId: Long, pageable: Pageable): Page<Post>
    
    fun findAllByWriter_IdOrderByCreatedDateDesc(writerId: Long, pageable: Pageable): Page<Post>
    
    fun findAllByWriter_IdAndDeleteYn(writerId: Long, deleteYn: Boolean, pageable: Pageable): Page<Post>
    
    fun findAllByWriter_IdAndDeleteYnOrderByCreatedDateDesc(writerId: Long,
                                                            deleteYn: Boolean,
                                                            pageable: Pageable): Page<Post>
    
}