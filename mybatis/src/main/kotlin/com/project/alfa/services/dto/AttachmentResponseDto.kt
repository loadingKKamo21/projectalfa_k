package com.project.alfa.services.dto

import com.project.alfa.entities.Attachment
import java.time.LocalDateTime

data class AttachmentResponseDto(
        val id: Long?,
        val postId: Long,
        val originalFilename: String,
        val storeFilename: String,
        val fileSize: Long,
        val createdDate: LocalDateTime,
        val lastModifiedDate: LocalDateTime? = null
) {
    
    constructor(attachment: Attachment) : this(
            id = attachment.id,
            postId = attachment.postId,
            originalFilename = attachment.originalFilename,
            storeFilename = attachment.storeFilename,
            fileSize = attachment.fileSize,
            createdDate = attachment.createdDate!!,
            lastModifiedDate = attachment.lastModifiedDate
    )
    
}
