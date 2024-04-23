package com.project.alfa.controllers

import com.project.alfa.services.MemberService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletRequest

@Controller
class MainController(private val memberService: MemberService) {
    
    @GetMapping("/")
    @ResponseBody
    fun mainPage(): ResponseEntity<String> = ResponseEntity.ok("Main page.")
    
    @GetMapping("/verify-email")
    @ResponseBody
    fun verifyEmail(@RequestParam email: String,
                    @RequestParam authToken: String,
                    request: HttpServletRequest): ResponseEntity<String> {
        val requestTime = LocalDateTime.parse(request.getHeader("Date"), DateTimeFormatter.RFC_1123_DATE_TIME)
        memberService.verifyEmailAuth(email, authToken, requestTime)
        return ResponseEntity.ok("Email verified successfully.")
    }
    
}