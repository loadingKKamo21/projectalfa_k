package com.project.alfa.services.dto

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

data class MemberJoinRequestDto(
        
        @field:NotBlank
        @field:Email
        @field:Pattern(
                message = "올바른 형태의 이메일 주소를 입력하세요.",
                regexp = RegEx.EMAIL_REGEX
        ) var username: String = "",
        
        @field:NotBlank
        @field:Pattern(
                message = "영문 대/소문자, 숫자, 특수문자 각 최소 1개 이상 포함, 8~32자",
                regexp = RegEx.PASSWORD_REGEX
        ) var password: String = "",
        
        @field:NotBlank
        var repeatPassword: String = "",
        
        @field:NotBlank
        @field:Pattern(
                message = "영문, 숫자, 한글, 1~20자",
                regexp = RegEx.NICKNAME_REGEX
        ) var nickname: String = ""

)