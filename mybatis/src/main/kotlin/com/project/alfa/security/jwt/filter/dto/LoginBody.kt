package com.project.alfa.security.jwt.filter.dto

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

data class LoginBody(
        @field:NotBlank
        @field:Email
        @field:Pattern(regexp = "^[a-zA-Z0-9_!#\$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\$")
        var username: String = "",
        @field:NotBlank
        @field:Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[`~!@#\$%^&*()-_=+[{]}|;:'\",<.>/?]).{8,32}\$")
        var password: String = ""
)
