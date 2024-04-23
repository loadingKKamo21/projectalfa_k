package com.project.alfa.services

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.entities.Attachment
import com.project.alfa.entities.UploadFile
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.repositories.mybatis.AttachmentMapper
import com.project.alfa.services.dto.AttachmentResponseDto
import com.project.alfa.utils.FileUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

@Import(TestConfig::class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class AttachmentServiceTest {
    
    @Autowired
    lateinit var attachmentService: AttachmentService
    
    @Autowired
    lateinit var attachmentMapper: AttachmentMapper
    
    @Autowired
    lateinit var fileUtil: FileUtil
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @Value("\${file.upload.location}")
    lateinit var fileDir: String
    lateinit var uploadPath: String
    
    @BeforeEach
    fun setup() {
        uploadPath = fileDir + File.separator + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    }
    
    private fun uploadFilesToAttachmentsAndSave(postId: Long, uploadFiles: List<UploadFile>): List<Attachment> {
        val attachments: MutableList<Attachment> = ArrayList()
        for (uploadFile in uploadFiles) {
            val attachment = Attachment(postId = postId,
                                        originalFilename = uploadFile.originalFilename,
                                        storeFilename = uploadFile.storeFilename,
                                        storeFilePath = uploadFile.storeFilePath,
                                        fileSize = uploadFile.fileSize)
            attachmentMapper.save(attachment)
            attachments.add(attachment)
        }
        return attachments
    }
    
    private fun getStoreFilePath(storeFilename: String): String = uploadPath + File.separator + storeFilename
    
    @Test
    @DisplayName("첨부파일 다중 저장")
    fun saveAll() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val total = dummy.generateRandomNumber(5, 10)
        val postId = post.id!!
        
        val multipartFiles: MutableList<MultipartFile> = ArrayList()
        for (i in 1..total) {
            val multipartFile = MockMultipartFile("filename$i",
                                                  "originalFilename$i",
                                                  "application/octet-stream",
                                                  UUID.randomUUID().toString().toByteArray())
            multipartFiles.add(multipartFile)
        }
        
        //When
        val ids = attachmentService.saveAllFiles(postId, multipartFiles)
        
        //Then
        val findAttachments = attachmentMapper.findAll().filter { ids.contains(it.id) }
        
        assertThat(ids.size).isEqualTo(total)
        for (i in 0 until total) {
            val findAttachment = findAttachments[i]
            val multipartFile = multipartFiles[i]
            
            val storeFilePath = getStoreFilePath(findAttachment.storeFilename)
            
            assertThat(findAttachment).isNotNull
            assertThat(findAttachment.originalFilename).isEqualTo(multipartFile.originalFilename)
            assertThat(findAttachment.fileSize).isEqualTo(multipartFile.size)
            assertThat(File(storeFilePath)).exists()
        }
    }
    
    @Test
    @DisplayName("첨부파일 다중 저장, 존재하지 않는 게시글")
    fun saveAll_unknownPost() {
        //Given
        val total = dummy.generateRandomNumber(5, 10)
        val postId = Random.nextLong()
        
        val multipartFiles: MutableList<MultipartFile> = ArrayList()
        for (i in 1..total) {
            val multipartFile = MockMultipartFile("filename$i",
                                                  "originalFilename$i",
                                                  "application/octet-stream",
                                                  UUID.randomUUID().toString().toByteArray())
            multipartFiles.add(multipartFile)
        }
        
        //When
        
        //Then
        assertThatThrownBy { attachmentService.saveAllFiles(postId, multipartFiles) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: $postId")
    }
    
    @Test
    @DisplayName("PK로 첨부파일 상세 정보 조회")
    fun findFileById() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val id = dummy.createAttachments(posts, 1, true)[0].id!!
        
        //When
        val dto = attachmentService.findFileById(id)
        
        //Then
        val findAttachment = attachmentMapper.findById(id)
        
        assertThat(findAttachment.id).isEqualTo(dto.id)
        assertThat(findAttachment.postId).isEqualTo(dto.postId)
        assertThat(findAttachment.originalFilename).isEqualTo(dto.originalFilename)
        assertThat(findAttachment.storeFilename).isEqualTo(dto.storeFilename)
        assertThat(findAttachment.fileSize).isEqualTo(dto.fileSize)
        assertThat(findAttachment.createdDate).isEqualTo(dto.createdDate)
        assertThat(findAttachment.lastModifiedDate).isEqualTo(dto.lastModifiedDate)
    }
    
    @Test
    @DisplayName("PK로 첨부파일 상세 정보 조회, 존재하지 않는 PK")
    fun findFileById_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        
        //Then
        assertThatThrownBy { attachmentService.findFileById(id) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Attachment' by id: $id")
    }
    
    @Test
    @DisplayName("게시글 기준 첨부파일 정보 목록 조회")
    fun findAllFilesByPost() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createAttachments(posts, total, true)
        val postId = posts[Random.nextInt(posts.size)].id!!
        
        //When
        val findAttachments = attachmentService.findAllFilesByPost(postId)
        
        //Then
        val attachments = attachmentMapper.findAll().filter { it.postId == postId }.map { AttachmentResponseDto(it) }
        
        assertThat(findAttachments.size).isEqualTo(attachments.size)
        for (i in attachments.indices) {
            val attachmentDto = attachments[i]
            val findAttachmentDto = findAttachments[i]
            
            assertThat(findAttachmentDto.id).isEqualTo(attachmentDto.id)
            assertThat(findAttachmentDto.postId).isEqualTo(attachmentDto.postId)
            assertThat(findAttachmentDto.originalFilename).isEqualTo(attachmentDto.originalFilename)
            assertThat(findAttachmentDto.storeFilename).isEqualTo(attachmentDto.storeFilename)
            assertThat(findAttachmentDto.fileSize).isEqualTo(attachmentDto.fileSize)
            assertThat(findAttachmentDto.createdDate).isEqualTo(attachmentDto.createdDate)
            assertThat(findAttachmentDto.lastModifiedDate).isEqualTo(attachmentDto.lastModifiedDate)
        }
    }
    
    @Test
    @DisplayName("첨부파일 다중 삭제")
    fun deleteAllFilesByIds() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val total = dummy.generateRandomNumber(5, 10)
        val postId = post.id!!
        
        val multipartFiles: MutableList<MultipartFile> = ArrayList()
        for (i in 1..total) {
            val multipartFile = MockMultipartFile("filename$i",
                                                  "originalFilename$i",
                                                  "application/octet-stream",
                                                  UUID.randomUUID().toString().toByteArray())
            multipartFiles.add(multipartFile)
        }
        val uploadFiles = fileUtil.storeFiles(multipartFiles)
        val attachments = uploadFilesToAttachmentsAndSave(postId, uploadFiles)
        val ids = attachments.map { it.id!! }
        
        //When
        attachmentService.deleteAllFilesByIds(ids, postId)
        
        //Then
        val findAttachments = attachmentMapper.findAllByIds(ids)
        for (i in 0 until total) {
            val findAttachment = findAttachments[i]
            
            val storeFilePath = getStoreFilePath(findAttachment.storeFilename)
            
            assertThat(findAttachment.deleteYn).isTrue
            assertThat(File(storeFilePath)).doesNotExist()
        }
    }
    
}