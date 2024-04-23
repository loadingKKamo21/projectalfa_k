package com.project.alfa.services.dto

import com.project.alfa.entities.Post
import java.time.LocalDateTime

data class PostResponseDto(
        val id: Long?,
        val writer: String,
        val title: String,
        val content: String?,
        val viewCount: Int,
        val noticeYn: Boolean,
        val commentCount: Int,
        val createdDate: LocalDateTime,
        val lastModifiedDate: LocalDateTime? = null
) {
    constructor(post: Post) : this(
            id = post.id,
            writer = post.nickname,
            title = post.title,
            content = post.content,
            viewCount = post.viewCount,
            noticeYn = post.noticeYn,
            commentCount = post.commentIds.size,
            createdDate = post.createdDate!!,
            lastModifiedDate = post.lastModifiedDate
    )
}
