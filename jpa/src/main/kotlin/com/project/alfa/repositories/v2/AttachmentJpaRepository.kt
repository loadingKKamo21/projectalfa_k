package com.project.alfa.repositories.v2

import com.project.alfa.entities.Attachment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AttachmentJpaRepository : JpaRepository<Attachment, Long> {
    
    fun findByIdAndDeleteYn(id: Long, deleteYn: Boolean): Optional<Attachment>
    
    fun findAllByDeleteYn(deleteYn: Boolean): List<Attachment>
    
    fun findAllByIdInAndDeleteYn(ids: List<Long>, deleteYn: Boolean): List<Attachment>
    
    fun findAllByPost_Id(postId: Long): List<Attachment>
    
    fun findAllByPost_IdAndDeleteYn(postId: Long, deleteYn: Boolean): List<Attachment>
    
}
