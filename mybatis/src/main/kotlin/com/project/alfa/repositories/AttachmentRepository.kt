package com.project.alfa.repositories

import com.project.alfa.entities.Attachment
import java.util.*

interface AttachmentRepository {
    
    fun save(attachment: Attachment): Attachment
    
    fun saveAll(attachments: List<Attachment>): List<Attachment>
    
    fun findById(id: Long): Optional<Attachment>
    
    fun findById(id: Long, deleteYn: Boolean): Optional<Attachment>
    
    fun findAll(): List<Attachment>
    
    fun findAll(deleteYn: Boolean): List<Attachment>
    
    fun findAll(ids: List<Long>): List<Attachment>
    
    fun findAll(ids: List<Long>, deleteYn: Boolean): List<Attachment>
    
    fun findAll(postId: Long): List<Attachment>
    
    fun findAll(postId: Long, deleteYn: Boolean): List<Attachment>
    
    fun deleteById(id: Long, postId: Long): Unit
    
    fun permanentlyDeleteById(id: Long): Unit
    
    fun deleteAllByIds(ids: List<Long>, postId: Long): Unit
    
    fun permanentlyDeleteAllByIds(ids: List<Long>): Unit
    
}