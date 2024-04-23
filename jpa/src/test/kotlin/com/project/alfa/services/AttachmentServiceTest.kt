package com.project.alfa.services

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.entities.Attachment
import com.project.alfa.entities.Post
import com.project.alfa.entities.UploadFile
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.repositories.v1.AttachmentRepositoryV1
import com.project.alfa.services.dto.AttachmentResponseDto
import com.project.alfa.utils.FileUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
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
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Import(TestConfig::class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class AttachmentServiceTest {
    
    @Autowired
    lateinit var attachmentService: AttachmentService
    
    @Autowired
    lateinit var attachmentRepository: AttachmentRepositoryV1
    
    //@Autowired
    //lateinit var attachmentRepository: AttachmentRepositoryV2
    
    //@Autowired
    //lateinit var attachmentRepository: AttachmentRepositoryV3
    
    @Autowired
    lateinit var fileUtil: FileUtil
    
    @PersistenceContext
    lateinit var em: EntityManager
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @Value("\${file.upload.location}")
    lateinit var fileDir: String
    lateinit var uploadPath: String
    
    @AfterEach
    fun clear() {
        em.flush()
        em.clear()
    }
    
    @BeforeEach
    fun setup() {
        uploadPath = fileDir + File.separator + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    }
    
    private fun uploadFilesToAttachments(post: Post, uploadFiles: List<UploadFile>): List<Attachment> {
        val attachments: MutableList<Attachment> = ArrayList()
        for (uploadFile in uploadFiles) {
            val attachment = Attachment(post = post,
                                        originalFilename = uploadFile.originalFilename,
                                        storeFilename = uploadFile.storeFilename,
                                        storeFilePath = uploadFile.storeFilePath,
                                        fileSize = uploadFile.fileSize)
            attachments.add(attachment)
        }
        return attachments
    }
    
    private fun getStoreFilePath(storeFilename: String): String = uploadPath + File.separator + storeFilename
    
    @Test
    @DisplayName("첨부파일 다중 저장")
    fun saveAll() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(5, 10)
        val postId = posts[0].id!!
        
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
        clear()
        
        //Then
        val findAttachments = attachmentRepository.findAll(ids).filter { ids.contains(it.id) }
        
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
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        posts[0].isDelete(true)
        val total = dummy.generateRandomNumber(5, 10)
        val postId = posts[0].id!!
        
        val multipartFiles: MutableList<MultipartFile> = ArrayList()
        for (i in 1..total) {
            val multipartFile = MockMultipartFile("filename$i",
                                                  "originalFilename$i",
                                                  "application/octet-stream",
                                                  UUID.randomUUID().toString().toByteArray())
            multipartFiles.add(multipartFile)
        }
        
        //When
        clear()
        
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
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val attachments = dummy.createAttachments(posts, 1)
        for (attachment in attachments)
            em.persist(attachment)
        val id = attachments[0].id!!
        
        //When
        val dto = attachmentService.findFileById(id)
        clear()
        
        //Then
        val findAttachment = em.find(Attachment::class.java, id)
        
        assertThat(findAttachment.id).isEqualTo(dto.id)
        assertThat(findAttachment.post.id).isEqualTo(dto.postId)
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
        val id = Random().nextLong()
        
        //When
        clear()
        
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
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        for (attachment in dummy.createAttachments(posts, total))
            em.persist(attachment)
        val postId = posts[Random().nextInt(posts.size)].id!!
        
        //When
        val findAttachments = attachmentService.findAllFilesByPost(postId)
        clear()
        
        //Then
        val attachments = attachmentRepository.findAll().filter { it.post.id == postId }
                .map { AttachmentResponseDto(it) }
        
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
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val post = posts[0]
        val postId = post.id!!
        val total = dummy.generateRandomNumber(5, 10)
        
        val multipartFiles: MutableList<MultipartFile> = ArrayList()
        for (i in 1..total) {
            val multipartFile = MockMultipartFile("filename$i",
                                                  "originalFilename$i",
                                                  "application/octet-stream",
                                                  UUID.randomUUID().toString().toByteArray())
            multipartFiles.add(multipartFile)
        }
        val uploadFiles = fileUtil.storeFiles(multipartFiles)
        val attachments = uploadFilesToAttachments(post, uploadFiles)
        attachments.forEach { em.persist(it) }
        val ids = attachments.map { it.id!! }
        
        //When
        attachmentService.deleteAllFilesByIds(ids, postId)
        clear()
        
        //Then
        val findAttachments = attachmentRepository.findAll().filter { ids.contains(it.id) }
        
        for (i in 0 until total) {
            val findAttachment = findAttachments[i]
            
            val storeFilePath = getStoreFilePath(findAttachment.storeFilename)
            
            assertThat(findAttachment.deleteYn).isTrue
            assertThat(File(storeFilePath)).doesNotExist()
        }
    }
    
}