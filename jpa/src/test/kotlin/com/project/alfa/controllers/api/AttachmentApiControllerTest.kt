package com.project.alfa.controllers.api

import com.google.gson.Gson
import com.project.alfa.config.security.TestSecurityConfig
import com.project.alfa.entities.*
import com.project.alfa.services.AttachmentService
import com.project.alfa.services.dto.AttachmentResponseDto
import com.project.alfa.utils.FileUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.core.io.UrlResource
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.io.ByteArrayInputStream
import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.*

@Import(TestSecurityConfig::class)
@MockBean(JpaMetamodelMappingContext::class)
@WebMvcTest(AttachmentApiController::class)
internal class AttachmentApiControllerTest {
    
    @MockBean
    lateinit var attachmentService: AttachmentService
    
    @MockBean
    lateinit var fileUtil: FileUtil
    
    @Autowired
    lateinit var mockMvc: MockMvc
    lateinit var attachments: MutableList<AttachmentResponseDto>
    
    val gson: Gson = Gson()
    
    @BeforeEach
    fun setup() {
        attachments = ArrayList()
        for (i in 1..10)
            attachments.add(AttachmentResponseDto(id = i.toLong(),
                                                  postId = 1L,
                                                  originalFilename = "Test originalFilename $i",
                                                  storeFilename = "Test storeFilename $i",
                                                  fileSize = 1000L,
                                                  createdDate = LocalDateTime.now()))
        
        val resource = Mockito.mock(UrlResource::class.java)
        val inputStream = ByteArrayInputStream(UUID.randomUUID().toString().toByteArray())
        
        whenever(attachmentService.findAllFilesByPost(any<Long>())).thenReturn(attachments)
        whenever(attachmentService.findFileById(any<Long>())).thenReturn(attachments[0])
        whenever(fileUtil.readAttachmentFileAsResource(any<AttachmentResponseDto>())).thenReturn(resource)
        whenever(resource.inputStream).thenReturn(inputStream)
    }
    
    @Test
    @DisplayName("첨부파일 목록 조회")
    fun findAllFilesByPost() {
        //Given
        val random = Random()
        var postId: Long
        do {
            postId = random.nextLong()
        } while (postId < 0)
        
        //When
        val actions = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/attachments", postId)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
        
        //Then
        actions.andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().json(gson.toJson(attachments)))
                .andDo(MockMvcResultHandlers.print())
        
        verify(attachmentService, Mockito.times(1)).findAllFilesByPost(any<Long>())
    }
    
    @Test
    @DisplayName("첨부파일 다운로드")
    fun downloadFile() {
        //Given
        val random = Random()
        var postId: Long
        var fileId: Long
        do {
            postId = random.nextLong()
            fileId = random.nextLong()
        } while (postId < 0 || fileId < 0)
        
        //When
        val actions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/posts/{postId}/attachments/{fileId}/download", postId, fileId)
                        .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
        
        //Then
        val filename = URLEncoder.encode(attachments[0].originalFilename, "UTF-8")
        
        actions.andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType("application/octet-stream;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.header()
                                   .string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\""))
                .andExpect(
                        MockMvcResultMatchers.header().longValue(HttpHeaders.CONTENT_LENGTH, attachments[0].fileSize))
                .andDo(MockMvcResultHandlers.print())
        
        verify(attachmentService, Mockito.times(1)).findFileById(any<Long>())
        verify(fileUtil, Mockito.times(1)).readAttachmentFileAsResource(any<AttachmentResponseDto>())
    }
    
}