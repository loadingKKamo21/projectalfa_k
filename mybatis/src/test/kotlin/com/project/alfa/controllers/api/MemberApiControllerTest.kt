package com.project.alfa.controllers.api

import com.google.gson.Gson
import com.project.alfa.config.security.TestSecurityConfig
import com.project.alfa.config.security.WithCustomMockUser
import com.project.alfa.entities.Role
import com.project.alfa.services.MemberService
import com.project.alfa.services.dto.MemberInfoResponseDto
import com.project.alfa.services.dto.MemberJoinRequestDto
import com.project.alfa.services.dto.MemberUpdateRequestDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.MethodArgumentNotValidException
import java.time.LocalDateTime
import java.util.*

@Import(TestSecurityConfig::class)
@WebMvcTest(MemberApiController::class)
internal class MemberApiControllerTest {
    
    @MockBean
    lateinit var memberService: MemberService
    
    @Autowired
    lateinit var mockMvc: MockMvc
    lateinit var responseDto: MemberInfoResponseDto
    
    val gson: Gson = Gson()
    
    @BeforeEach
    fun setup() {
        responseDto = MemberInfoResponseDto(id = 1L,
                                            username = "user1@mail.com",
                                            nickname = "user1",
                                            role = Role.USER,
                                            postCount = 0,
                                            commentCount = 0,
                                            createdDate = LocalDateTime.now())
        
        whenever(memberService.join(any<MemberJoinRequestDto>())).thenReturn(1L)
        whenever(memberService.findByUsername(any<String>())).thenReturn(responseDto)
        doNothing().`when`(memberService).update(any<MemberUpdateRequestDto>())
        doNothing().`when`(memberService).delete(any<Long>(), any<String>())
    }
    
    @Test
    @DisplayName("회원 가입 페이지")
    fun joinPage() {
        //Given
        
        //When
        val actions = mockMvc.perform(get("/api/members")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().json(gson.toJson(MemberJoinRequestDto())))
                .andDo(print())
    }
    
    @Test
    @DisplayName("회원 가입")
    fun join() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/members")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(
                                                      MemberJoinRequestDto("user1@mail.com",
                                                                           "Password1!@",
                                                                           "Password1!@",
                                                                           "user1"))
                                              ))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().string("Member joined successfully."))
                .andDo(print())
        
        verify(memberService, times(1)).join(any<MemberJoinRequestDto>())
    }
    
    @Test
    @DisplayName("회원 가입, @Valid 체크")
    fun join_validCheck() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/members")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(MemberJoinRequestDto())))
        
        //Then
        actions.andExpect(status().isBadRequest)
                .andExpect { result -> assertThat(result.resolvedException is MethodArgumentNotValidException) }
                .andDo(print())
        
        verify(memberService, never()).join(any<MemberJoinRequestDto>())
    }
    
    @Test
    @DisplayName("비밀번호 찾기")
    fun forgotPassword() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/members/forgot-password")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content("user1@mail.com"))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().string("Successfully sending of \"Find Password\" email."))
                .andDo(print())
        
        verify(memberService, times(1)).findPassword(any<String>())
    }
    
    @Test
    @DisplayName("비밀번호 찾기, 올바르지 않은 아이디(이메일)")
    fun forgotPassword_invalidUsername() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/members/forgot-password")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(UUID.randomUUID().toString()))
        
        //Then
        actions.andExpect(status().isBadRequest)
                .andDo(print())
        
        verify(memberService, never()).findPassword(any<String>())
    }
    
    @Test
    @DisplayName("프로필 조회 페이지")
    @WithMockUser
    fun profilePage() {
        //Given
        
        //When
        val actions = mockMvc.perform(get("/api/members/profile")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().json(gson.toJson(responseDto)))
                .andDo(print())
        
        verify(memberService, times(1)).findByUsername(any<String>())
    }
    
    @Test
    @DisplayName("프로필 수정 페이지")
    @WithMockUser
    fun profileUpdatePage() {
        //Given
        
        //When
        val map: MutableMap<String, Any> = HashMap()
        map["member"] = responseDto
        map["form"] = MemberUpdateRequestDto()
        
        val actions = mockMvc.perform(get("/api/members/profile-update")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().json(gson.toJson(map)))
                .andDo(print())
        
        verify(memberService, times(1)).findByUsername(any<String>())
    }
    
    @Test
    @DisplayName("프로필 수정")
    @WithCustomMockUser
    fun profileUpdate() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/members/profile-update")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(MemberUpdateRequestDto(id = 1L,
                                                                                          password = "Password1!@",
                                                                                          nickname = "user2",
                                                                                          signature = "Signature",
                                                                                          newPassword = "Password2!@",
                                                                                          repeatNewPassword = "Password2!@"))
                                              ))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().string("Member updated successfully."))
                .andDo(print())
        
        verify(memberService, times(1)).update(any<MemberUpdateRequestDto>())
    }
    
    @Test
    @DisplayName("프로필 수정, @Valid 체크")
    @WithCustomMockUser
    fun profileUpdate_validCheck() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/members/profile-update")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(MemberUpdateRequestDto())))
        
        //Then
        actions.andExpect(status().isBadRequest)
                .andExpect { result -> assertThat(result.resolvedException is MethodArgumentNotValidException) }
                .andDo(print())
        
        verify(memberService, never()).update(any<MemberUpdateRequestDto>())
    }
    
    @Test
    @DisplayName("프로필 수정, UserDetails ID != DTO ID")
    @WithCustomMockUser(id = 2L)
    fun profileUpdate_invalidId() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/members/profile-update")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(MemberUpdateRequestDto(id = 1L,
                                                                                          password = "Password1!@",
                                                                                          nickname = "user2",
                                                                                          signature = "Signature",
                                                                                          newPassword = "Password2!@",
                                                                                          repeatNewPassword = "Password2!@"))
                                              ))
        
        //Then
        actions.andExpect(status().isUnauthorized)
                .andExpect(content().string("Member update denied."))
                .andDo(print())
        
        verify(memberService, never()).update(any<MemberUpdateRequestDto>())
    }
    
    @Test
    @DisplayName("회원 탈퇴")
    @WithCustomMockUser
    fun deleteAccount() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/members/delete")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(MemberUpdateRequestDto(id = 1L,
                                                                                          password = "Password1!@"))))
        
        //Then
        actions.andExpect(status().isFound)
                .andExpect(content().string("Member deleted successfully."))
                .andDo(print())
        
        verify(memberService, times(1)).delete(any<Long>(), any<String>())
    }
    
    @Test
    @DisplayName("회원 탈퇴, UserDetails ID != DTO ID")
    @WithCustomMockUser(id = 2L)
    fun deleteAccount_invalidId() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/members/delete")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(MemberUpdateRequestDto(id = 1L,
                                                                                          password = "Password1!@"))))
        
        //Then
        actions.andExpect(status().isUnauthorized)
                .andExpect(content().string("Member delete denied."))
                .andDo(print())
        
        verify(memberService, never()).delete(any<Long>(), any<String>())
    }
    
}