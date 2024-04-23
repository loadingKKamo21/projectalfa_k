package com.project.alfa.controllers

import com.project.alfa.config.security.TestSecurityConfig
import com.project.alfa.services.MemberService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@Import(TestSecurityConfig::class)
@WebMvcTest(MainController::class)
internal class MainControllerTest {
    
    @MockBean
    lateinit var memberService: MemberService
    
    @Autowired
    lateinit var mockMvc: MockMvc
    
    @Test
    @DisplayName("메인 페이지")
    fun mainPage() {
        //Given
        
        //When
        val actions = mockMvc.perform(get("/"))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().string("Main page."))
                .andDo(print())
    }
    
    @Test
    @DisplayName("이메일 인증")
    fun verifyEmail() {
        //Given
        
        //When
        val requestTime = LocalDateTime.now()
        val actions = mockMvc.perform(get("/verify-email")
                                              .header("Date", DateTimeFormatter.RFC_1123_DATE_TIME
                                                      .format(requestTime.atOffset(ZoneOffset.UTC)))
                                              .param("email", "user1@mail.com")
                                              .param("authToken", UUID.randomUUID().toString()))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().string("Email verified successfully."))
                .andDo(print())
        
        doNothing().`when`(memberService).verifyEmailAuth(any<String>(), any<String>(), any<LocalDateTime>())
    }
    
}