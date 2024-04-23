package com.project.alfa.services.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class MemberUpdateRequestDto(
        
        @field:NotNull
        var id: Long? = null,
        
        @field:NotBlank
        @field:Pattern(
                message = "영문 대/소문자, 숫자, 특수문자 각 최소 1개 이상 포함, 8~32자",
                regexp = RegEx.PASSWORD_REGEX
        ) var password: String = "",
        
        @field:NotBlank
        @field:Pattern(
                message = "영문, 숫자, 한글, 1~20자",
                regexp = RegEx.NICKNAME_REGEX
        ) var nickname: String? = null,
        
        @field:Pattern(
                message = "최대 100자",
                regexp = RegEx.SIGNATURE_REGEX
        ) var signature: String? = null,
        
        @field:NotBlank
        @field:Pattern(
                message = "영문 대/소문자, 숫자, 특수문자 각 최소 1개 이상 포함, 8~32자",
                regexp = RegEx.PASSWORD_REGEX
        ) var newPassword: String? = null,
        
        var repeatNewPassword: String? = null

)
