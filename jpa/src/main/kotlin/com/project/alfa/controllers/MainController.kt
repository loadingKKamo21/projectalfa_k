package com.project.alfa.controllers

import com.project.alfa.services.MemberService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletRequest

@Controller
@Tag(name = "Main API", description = "Main API 입니다.")
class MainController(private val memberService: MemberService) {
    
    @GetMapping("/")
    @ResponseBody
    @Tag(name = "Main API")
    @Operation(summary = "메인 페이지", description = "메인 페이지를 출력합니다.")
    fun mainPage(): ResponseEntity<String> = ResponseEntity.ok("Main page.")
    
    @GetMapping("/verify-email")
    @ResponseBody
    @Tag(name = "Main API")
    @Operation(summary = "이메일 인증", description = "이메일 인증을 수행합니다.")
    fun verifyEmail(
        @RequestParam email: String,
        @RequestParam authToken: String,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        val header: String? = request.getHeader("Date")
        var requestTime: LocalDateTime = if (header.isNullOrBlank())
            LocalDateTime.now()
        else
            LocalDateTime.parse(header, DateTimeFormatter.RFC_1123_DATE_TIME)
        memberService.verifyEmailAuth(email, authToken, requestTime)
        return ResponseEntity.ok("Email verified successfully.")
    }
    
}