package com.project.alfa.controllers.api

import com.google.gson.Gson
import com.project.alfa.config.security.TestSecurityConfig
import com.project.alfa.config.security.WithCustomMockUser
import com.project.alfa.entities.*
import com.project.alfa.services.CommentService
import com.project.alfa.services.dto.CommentRequestDto
import com.project.alfa.services.dto.CommentResponseDto
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.bind.MethodArgumentNotValidException
import java.time.LocalDateTime
import java.util.*

@Import(TestSecurityConfig::class)
@MockBean(JpaMetamodelMappingContext::class)
@WebMvcTest(CommentApiController::class)
internal class CommentApiControllerTest {
    
    @MockBean
    lateinit var commentService: CommentService
    
    @Autowired
    lateinit var mockMvc: MockMvc
    lateinit var comments: Page<CommentResponseDto>
    
    val gson: Gson = Gson()
    
    @BeforeEach
    fun setup() {
        val list: MutableList<CommentResponseDto> = ArrayList()
        for (i in 1..10)
            list.add(CommentResponseDto(id = i.toLong(),
                                        writer = "user1",
                                        content = "Test content $i",
                                        createdDate = LocalDateTime.now()))
        comments = PageImpl(list)
        
        whenever(commentService.findAllPageByPost(any<Long>(), any<Pageable>())).thenReturn(comments)
        whenever(commentService.findAllPageByWriter(any<Long>(), any<Pageable>())).thenReturn(comments)
        whenever(commentService.create(any<CommentRequestDto>())).thenReturn(1L)
        whenever(commentService.read(any<Long>())).thenReturn(comments.content[0])
        doNothing().`when`(commentService).update(any<CommentRequestDto>())
        doNothing().`when`(commentService).delete(any<Long>(), any<Long>())
    }
    
    @Test
    @DisplayName("게시글 기준 댓글 목록 페이지")
    fun commentsList() {
        //Given
        
        //When
        val actions = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/comments", 1)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .param("page", "0")
                                              .param("size", "10"))
        
        //Then
        actions.andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().json(gson.toJson(comments)))
                .andDo(MockMvcResultHandlers.print())
        
        verify(commentService, times(1)).findAllPageByPost(any<Long>(), any<Pageable>())
    }
    
    @Test
    @DisplayName("작성자 기준 댓글 목록 페이지")
    @WithCustomMockUser
    fun commentsListByWriter() {
        //Given
        
        //When
        val actions = mockMvc.perform(MockMvcRequestBuilders.get("/api/comments/writer", 1)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .param("page", "0")
                                              .param("size", "10"))
        
        //Then
        actions.andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().json(gson.toJson(comments)))
                .andDo(MockMvcResultHandlers.print())
        
        verify(commentService, times(1)).findAllPageByWriter(any<Long>(), any<Pageable>())
    }
    
    @Test
    @DisplayName("댓글 작성 페이지")
    @WithMockUser
    fun writeCommentPage() {
        //Given
        
        //When
        val actions = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/comments/write", 1)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
        
        //Then
        actions.andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().json(gson.toJson(CommentRequestDto())))
                .andDo(MockMvcResultHandlers.print())
    }
    
    @Test
    @DisplayName("댓글 작성")
    @WithCustomMockUser
    fun writeComment() {
        //Given
        
        //When
        val actions = mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/{postId}/comments/write", 1)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(CommentRequestDto(writerId = 1L,
                                                                                     postId = 1L,
                                                                                     content = "Test content"))))
        
        //Then
        actions.andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().string("Comment created successfully."))
                .andDo(MockMvcResultHandlers.print())
        
        verify(commentService, times(1)).create(any<CommentRequestDto>())
    }
    
    @Test
    @DisplayName("댓글 작성, @Valid 체크")
    @WithCustomMockUser
    fun writeComment_validCheck() {
        //Given
        
        //When
        val actions = mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/{postId}/comments/write", 1)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(CommentRequestDto())))
        
        //Then
        actions.andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect { result ->
                    Assertions.assertThat(result.resolvedException is MethodArgumentNotValidException)
                }
                .andDo(MockMvcResultHandlers.print())
        
        verify(commentService, never()).create(any<CommentRequestDto>())
    }
    
    @Test
    @DisplayName("댓글 수정 페이지")
    @WithCustomMockUser
    fun updateCommentPage() {
        //Given
        
        //When
        val map: MutableMap<String, Any> = HashMap()
        map["comment"] = comments.content[0]
        map["form"] = CommentRequestDto()
        
        val actions = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/comments/write/{commentId}", 1, 1)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
        
        //Then
        actions.andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().json(gson.toJson(map)))
                .andDo(MockMvcResultHandlers.print())
        
        verify(commentService, times(1)).read(any<Long>())
    }
    
    @Test
    @DisplayName("댓글 수정")
    @WithCustomMockUser
    fun updateComment() {
        //Given
        
        //When
        val actions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/posts/{postId}/comments/write/{commentId}", 1, 1)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(gson.toJson(CommentRequestDto(id = 1L,
                                                               writerId = 1L,
                                                               postId = 1L,
                                                               content = "Test content"))))
        
        //Then
        actions.andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().string("Comment updated successfully."))
                .andDo(MockMvcResultHandlers.print())
        
        verify(commentService, times(1)).update(any<CommentRequestDto>())
    }
    
    @Test
    @DisplayName("댓글 수정, @Valid 체크")
    @WithMockUser
    fun updateComment_validCheck() {
        //Given
        
        //When
        val actions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/posts/{postId}/comments/write/{commentId}", 1, 1)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(gson.toJson(CommentRequestDto())))
        
        //Then
        actions.andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect { result ->
                    Assertions.assertThat(result.resolvedException is MethodArgumentNotValidException)
                }
                .andDo(MockMvcResultHandlers.print())
        
        verify(commentService, never()).update(any<CommentRequestDto>())
    }
    
    @Test
    @DisplayName("댓글 삭제")
    @WithCustomMockUser
    fun deleteComment() {
        //Given
        
        //When
        val actions = mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/{postId}/comments/delete", 1)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(CommentRequestDto(id = 1L,
                                                                                     writerId = 1L,
                                                                                     postId = 1L))))
        
        //Then
        actions.andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().string("Comment deleted successfully."))
                .andDo(MockMvcResultHandlers.print())
        
        verify(commentService, times(1)).delete(any<Long>(), any<Long>())
    }
    
}