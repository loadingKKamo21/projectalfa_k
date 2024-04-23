package com.project.alfa.services.dto

import org.springframework.web.multipart.MultipartFile
import javax.validation.constraints.NotBlank

data class PostRequestDto(
        
        var id: Long? = null,
        var writerId: Long? = null,
        
        @field:NotBlank(message = "제목을 입력하세요.")
        var title: String = "",
        
        @field:NotBlank(message = "내용을 입력하세요.")
        var content: String = "",
        
        var noticeYn: Boolean = false

) {
        var files: MutableList<MultipartFile> = ArrayList()
        var removeFileIds: MutableList<Long> = ArrayList()
}