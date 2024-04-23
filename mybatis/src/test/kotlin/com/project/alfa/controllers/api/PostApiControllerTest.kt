package com.project.alfa.controllers.api

import com.google.gson.Gson
import com.project.alfa.config.security.TestSecurityConfig
import com.project.alfa.config.security.WithCustomMockUser
import com.project.alfa.repositories.dto.SearchParam
import com.project.alfa.services.AttachmentService
import com.project.alfa.services.PostService
import com.project.alfa.services.dto.AttachmentResponseDto
import com.project.alfa.services.dto.PostRequestDto
import com.project.alfa.services.dto.PostResponseDto
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
import org.springframework.data.domain.Pageable
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

@Import(TestSecurityConfig::class)
@WebMvcTest(PostApiController::class)
internal class PostApiControllerTest {
    
    @MockBean
    lateinit var postService: PostService
    
    @MockBean
    lateinit var attachmentService: AttachmentService
    
    @Autowired
    lateinit var mockMvc: MockMvc
    lateinit var posts: MutableList<PostResponseDto>
    
    val gson: Gson = Gson()
    
    @BeforeEach
    fun setup() {
        posts = ArrayList()
        for (i in 1..10)
            posts.add(PostResponseDto(id = i.toLong(),
                                      writer = "user$i",
                                      title = "Test title $i",
                                      content = "Test content $i",
                                      viewCount = 0,
                                      noticeYn = false,
                                      commentCount = 0,
                                      createdDate = LocalDateTime.now()))
        
        whenever(postService.findAllPage(any<SearchParam>(), any<Pageable>())).thenReturn(posts)
        whenever(postService.findAllPageByWriter(any<Long>(), any<Pageable>())).thenReturn(posts)
        whenever(postService.read(any<Long>())).thenReturn(posts[0])
        whenever(postService.readWithCaching(any<Long>(), any<String>(), any<String>())).thenReturn(posts[0])
        whenever(postService.create(any<PostRequestDto>())).thenReturn(1L)
        doNothing().`when`(postService).addViewCountWithCaching(any<Long>(), any<String>(), any<String>())
        doNothing().`when`(postService).update(any<PostRequestDto>())
        doNothing().`when`(postService).delete(any<Long>(), any<Long>())
        
        whenever(attachmentService.saveAllFiles(any<Long>(), any())).thenReturn(emptyList())
        doNothing().`when`(attachmentService).deleteAllFilesByIds(any(), any<Long>())
    }
    
    @Test
    @DisplayName("게시글 목록 페이지")
    fun postsList() {
        //Given
        
        //When
        val actions = mockMvc.perform(get("/api/posts")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .param("page", "0")
                                              .param("size", "10"))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().json(gson.toJson(posts)))
                .andDo(print())
        
        verify(postService, times(1)).findAllPage(any<SearchParam>(), any<Pageable>())
    }
    
    @Test
    @DisplayName("게시글 목록 페이지, 검색 조건 추가")
    fun postsListWithSearch() {
        //Given
        
        //When
        val actions = mockMvc.perform(get("/api/posts")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .param("page", "0")
                                              .param("size", "10")
                                              .param("condition", "title")
                                              .param("keyword", "Test search"))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().json(gson.toJson(posts)))
                .andDo(print())
        
        verify(postService, times(1)).findAllPage(any<SearchParam>(), any<Pageable>())
    }
    
    @Test
    @DisplayName("작성자 기준 게시글 목록 페이지")
    @WithCustomMockUser
    fun postsListByWriter() {
        //Given
        
        //When
        val actions = mockMvc.perform(get("/api/posts/writer")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().json(gson.toJson(posts)))
                .andDo(print())
        
        verify(postService, times(1)).findAllPageByWriter(any<Long>(), any<Pageable>())
    }
    
    @Test
    @DisplayName("게시글 상세 조회 페이지")
    fun readPostPage() {
        //Given
        
        //When
        val map: MutableMap<String, Any> = HashMap()
        map["post"] = posts[0]
        map["files"] = emptyList<AttachmentResponseDto>()
        
        val actions = mockMvc.perform(get("/api/posts/{postId}", 1)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().json(gson.toJson(map)))
                .andDo(print())
        
        verify(postService, times(1)).readWithCaching(any<Long>(), any<String>(), any<String>())
        verify(attachmentService, times(1)).findAllFilesByPost(any<Long>())
    }
    
    @Test
    @DisplayName("게시글 작성 페이지")
    @WithMockUser
    fun writePostPage() {
        //Given
        
        //When
        val actions = mockMvc.perform(get("/api/posts/write")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().json(gson.toJson(PostRequestDto())))
                .andDo(print())
    }
    
    @Test
    @DisplayName("게시글 작성")
    @WithCustomMockUser
    fun writePost() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/posts/write")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(PostRequestDto(writerId = 1L,
                                                                                  title = "Test title",
                                                                                  content = "Test content",
                                                                                  noticeYn = false))))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().string("Post created successfully."))
                .andDo(print())
        
        verify(postService, times(1)).create(any<PostRequestDto>())
        verify(attachmentService, times(1)).saveAllFiles(any<Long>(), any())
    }
    
    @Test
    @DisplayName("게시글 작성, @Valid 체크")
    @WithMockUser
    fun writePost_validCheck() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/posts/write")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(PostRequestDto())))
        
        //Then
        actions.andExpect(status().isBadRequest)
                .andExpect { result -> assertThat(result.resolvedException is MethodArgumentNotValidException) }
                .andDo(print())
        
        verify(postService, never()).create(any<PostRequestDto>())
        verify(attachmentService, never()).saveAllFiles(any<Long>(), any())
    }
    
    @Test
    @DisplayName("게시글 수정 페이지")
    @WithMockUser
    fun updatePostPage() {
        //Given
        
        //When
        val map: MutableMap<String, Any> = HashMap()
        map["post"] = posts[0]
        map["form"] = PostRequestDto()
        map["files"] = emptyList<AttachmentResponseDto>()
        
        val actions = mockMvc.perform(get("/api/posts/write/{postId}", 1)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().json(gson.toJson(map)))
                .andDo(print())
        
        verify(postService, times(1)).read(any<Long>())
        verify(attachmentService, times(1)).findAllFilesByPost(any<Long>())
    }
    
    @Test
    @DisplayName("게시글 수정")
    @WithCustomMockUser
    fun updatePost() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/posts/write/{postId}", 1)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(PostRequestDto(id = 1L,
                                                                                  writerId = 1L,
                                                                                  title = "Test title",
                                                                                  content = "Test content",
                                                                                  noticeYn = false))))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().string("Post updated successfully."))
                .andDo(print())
        
        verify(postService, times(1)).update(any<PostRequestDto>())
        verify(attachmentService, times(1)).deleteAllFilesByIds(any(), any<Long>())
        verify(attachmentService, times(1)).saveAllFiles(any<Long>(), any())
    }
    
    @Test
    @DisplayName("게시글 수정, @Valid 체크")
    @WithMockUser
    fun updatePost_validCheck() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/posts/write/{postId}", 1)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(PostRequestDto())))
        
        //Then
        actions.andExpect(status().isBadRequest)
                .andExpect { result -> assertThat(result.resolvedException is MethodArgumentNotValidException) }
                .andDo(print())
        
        verify(postService, never()).update(any<PostRequestDto>())
        verify(attachmentService, never()).deleteAllFilesByIds(any(), any<Long>())
        verify(attachmentService, never()).saveAllFiles(any<Long>(), any())
    }
    
    @Test
    @DisplayName("게시글 삭제")
    @WithCustomMockUser
    fun deletePost() {
        //Given
        
        //When
        val actions = mockMvc.perform(post("/api/posts/delete")
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(gson.toJson(PostRequestDto(id = 1L, writerId = 1L))))
        
        //Then
        actions.andExpect(status().isOk)
                .andExpect(content().string("Post deleted successfully."))
                .andDo(print())
        
        verify(postService, times(1)).delete(any<Long>(), any<Long>())
        verify(attachmentService, times(1)).deleteAllFilesByIds(any(), any<Long>())
    }
    
}