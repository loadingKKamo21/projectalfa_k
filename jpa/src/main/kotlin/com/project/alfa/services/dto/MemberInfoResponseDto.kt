package com.project.alfa.services.dto

import com.project.alfa.entities.Member
import com.project.alfa.entities.Role
import java.time.LocalDateTime

data class MemberInfoResponseDto(
        val id: Long?,
        val username: String,
        val nickname: String,
        val signature: String? = null,
        val role: Role,
        val postCount: Int,
        val commentCount: Int,
        val createdDate: LocalDateTime,
        val lastModifiedDate: LocalDateTime? = null
) {
    constructor(member: Member) : this(
            id = member.id,
            username = member.username,
            nickname = member.nickname,
            signature = member.signature,
            role = member.role,
            postCount = member.getPostsCount(),
            commentCount = member.getCommentsCount(),
            createdDate = member.createdDate!!,
            lastModifiedDate = member.lastModifiedDate
    )
}
