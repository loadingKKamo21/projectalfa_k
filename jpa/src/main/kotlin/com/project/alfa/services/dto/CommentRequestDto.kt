package com.project.alfa.services.dto

import javax.validation.constraints.NotBlank

data class CommentRequestDto(
        
        var id: Long? = null,
        var writerId: Long? = null,
        var postId: Long? = null,
        
        @field:NotBlank(message = "내용을 입력하세요.")
        var content: String = ""

)
