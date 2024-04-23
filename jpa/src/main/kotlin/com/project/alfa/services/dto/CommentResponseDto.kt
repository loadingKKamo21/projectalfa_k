package com.project.alfa.services.dto

import com.project.alfa.entities.Comment
import java.time.LocalDateTime

data class CommentResponseDto(
        val id: Long?,
        val writer: String,
        val content: String,
        val createdDate: LocalDateTime,
        val lastModifiedDate: LocalDateTime? = null
) {
    constructor(comment: Comment) : this(
            id = comment.id,
            writer = comment.writer.nickname,
            content = comment.content,
            createdDate = comment.createdDate!!,
            lastModifiedDate = comment.lastModifiedDate
    )
}
