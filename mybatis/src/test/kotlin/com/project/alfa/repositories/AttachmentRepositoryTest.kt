package com.project.alfa.repositories

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.repositories.mybatis.AttachmentMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Import(TestConfig::class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class AttachmentRepositoryTest {
    
    @Autowired
    lateinit var attachmentRepository: AttachmentRepository
    
    @Autowired
    lateinit var attachmentMapper: AttachmentMapper
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @Test
    @DisplayName("첨부파일 저장")
    fun save() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val attachment = dummy.createAttachments(posts, 1, false)[0]
        
        //When
        val savedAttachment = attachmentRepository.save(attachment)
        val id = savedAttachment.id!!
        
        //Then
        val findAttachment = attachmentMapper.findById(id)
        
        assertThat(findAttachment.postId).isEqualTo(attachment.postId)
        assertThat(findAttachment.originalFilename).isEqualTo(attachment.originalFilename)
        assertThat(findAttachment.storeFilename).isEqualTo(attachment.storeFilename)
        assertThat(findAttachment.storeFilePath).isEqualTo(attachment.storeFilePath)
        assertThat(findAttachment.fileSize).isEqualTo(attachment.fileSize)
    }
    
    @Test
    @DisplayName("첨부파일 다중 저장")
    fun saveAll() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val total = dummy.generateRandomNumber(5, 10)
        val attachments = dummy.createAttachments(posts, total, false)
        
        //When
        val savedAttachments = attachmentRepository.saveAll(attachments)
        
        //Then
        val findAttachments = attachmentMapper.findAll()
        
        assertThat(findAttachments.size).isEqualTo(total)
        for (i in 0 until total) {
            val attachment = savedAttachments[i]
            val findAttachment = findAttachments[i]
            
            assertThat(findAttachment.postId).isEqualTo(attachment.postId)
            assertThat(findAttachment.originalFilename).isEqualTo(attachment.originalFilename)
            assertThat(findAttachment.storeFilename).isEqualTo(attachment.storeFilename)
            assertThat(findAttachment.storeFilePath).isEqualTo(attachment.storeFilePath)
            assertThat(findAttachment.fileSize).isEqualTo(attachment.fileSize)
        }
    }
    
    @Test
    @DisplayName("PK로 조회")
    fun findById() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val attachment = dummy.createAttachments(posts, 1, true)[0]
        val id = attachment.id!!
        
        //When
        val findAttachment = attachmentRepository.findById(id).get()
        
        //Then
        assertThat(findAttachment.postId).isEqualTo(attachment.postId)
        assertThat(findAttachment.originalFilename).isEqualTo(attachment.originalFilename)
        assertThat(findAttachment.storeFilename).isEqualTo(attachment.storeFilename)
        assertThat(findAttachment.storeFilePath).isEqualTo(attachment.storeFilePath)
        assertThat(findAttachment.fileSize).isEqualTo(attachment.fileSize)
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    fun findById_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        val unknownAttachment = attachmentRepository.findById(id)
        
        //Then
        assertThat(unknownAttachment.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회")
    fun findByIdAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val attachment = dummy.createAttachments(posts, 1, true)[0]
        val id = attachment.id!!
        
        //When
        val findAttachment = attachmentRepository.findById(id, false).get()
        
        //Then
        assertThat(findAttachment.postId).isEqualTo(attachment.postId)
        assertThat(findAttachment.originalFilename).isEqualTo(attachment.originalFilename)
        assertThat(findAttachment.storeFilename).isEqualTo(attachment.storeFilename)
        assertThat(findAttachment.storeFilePath).isEqualTo(attachment.storeFilePath)
        assertThat(findAttachment.fileSize).isEqualTo(attachment.fileSize)
        assertThat(findAttachment.deleteYn).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 존재하지 않는 PK")
    fun findByIdAndDeleteYn_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        val unknownAttachment = attachmentRepository.findById(id, false)
        
        //Then
        assertThat(unknownAttachment.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 이미 삭제한 첨부파일")
    fun findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val attachment = dummy.createAttachments(posts, 1, true)[0]
        val id = attachment.id!!
        attachmentMapper.deleteById(id, attachment.postId)
        
        //When
        val deletedAttachment = attachmentRepository.findById(id, false)
        
        //Then
        assertThat(deletedAttachment.isPresent).isFalse
    }
    
    @Test
    @DisplayName("첨부파일 목록 조회")
    fun findAll() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        val attachments = dummy.createAttachments(posts, total, true)
        
        //When
        val findAttachments = attachmentRepository.findAll().sortedBy { it.id }
        
        //Then
        assertThat(findAttachments.size).isEqualTo(total)
        for (i in 0 until total) {
            val attachment = attachments[i]
            val findAttachment = findAttachments[i]
            
            assertThat(findAttachment.postId).isEqualTo(attachment.postId)
            assertThat(findAttachment.originalFilename).isEqualTo(attachment.originalFilename)
            assertThat(findAttachment.storeFilename).isEqualTo(attachment.storeFilename)
            assertThat(findAttachment.storeFilePath).isEqualTo(attachment.storeFilePath)
            assertThat(findAttachment.fileSize).isEqualTo(attachment.fileSize)
        }
    }
    
    @Test
    @DisplayName("삭제 여부로 첨부파일 목록 조회")
    fun findAllByDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createAttachments(posts, total, true)
        dummy.randomlyDeleteAttachments(attachmentMapper.findAll(), dummy.generateRandomNumber(1, 100))
        
        //When
        val findAttachments = attachmentRepository.findAll(false).sortedBy { it.id }
        
        //Then
        val undeletedAttachments = attachmentMapper.findAll().filter { !it.deleteYn }.sortedBy { it.id }
        
        assertThat(findAttachments.size).isEqualTo(undeletedAttachments.size)
        for (i in 0 until undeletedAttachments.size) {
            val attachment = undeletedAttachments[i]
            val findAttachment = findAttachments[i]
            
            assertThat(findAttachment.postId).isEqualTo(attachment.postId)
            assertThat(findAttachment.originalFilename).isEqualTo(attachment.originalFilename)
            assertThat(findAttachment.storeFilename).isEqualTo(attachment.storeFilename)
            assertThat(findAttachment.storeFilePath).isEqualTo(attachment.storeFilePath)
            assertThat(findAttachment.fileSize).isEqualTo(attachment.fileSize)
            assertThat(findAttachment.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("PK 목록으로 첨부파일 목록 조회")
    fun findAllByIds() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        val ids = dummy.createAttachments(posts, total, true).map { it.id!! }
        
        //When
        val findAttachments = attachmentRepository.findAll(ids)
        
        //Then
        val attachments = attachmentMapper.findAll().filter { ids.contains(it.id) }.sortedBy { it.id }
        
        assertThat(findAttachments.size).isEqualTo(attachments.size)
        for (i in 0 until total) {
            val attachment = attachments[i]
            val findAttachment = findAttachments[i]
            
            assertThat(findAttachment.postId).isEqualTo(attachment.postId)
            assertThat(findAttachment.originalFilename).isEqualTo(attachment.originalFilename)
            assertThat(findAttachment.storeFilename).isEqualTo(attachment.storeFilename)
            assertThat(findAttachment.storeFilePath).isEqualTo(attachment.storeFilePath)
            assertThat(findAttachment.fileSize).isEqualTo(attachment.fileSize)
        }
    }
    
    @Test
    @DisplayName("PK 목록, 삭제 여부로 첨부파일 목록 조회")
    fun findAllByIdsAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        val attachments = dummy.createAttachments(posts, total, true)
        dummy.randomlyDeleteAttachments(attachments, dummy.generateRandomNumber(1, 100))
        val ids = attachments.filter { !it.deleteYn }.map { it.id!! }
        
        //When
        val findAttachments = attachmentRepository.findAll(ids, false)
        
        //Then
        val undeletedAttachments = attachmentMapper.findAll().filter { ids.contains(it.id) && !it.deleteYn }
                .sortedBy { it.id }
        
        assertThat(findAttachments.size).isEqualTo(undeletedAttachments.size)
        for (i in 0 until undeletedAttachments.size) {
            val attachment = undeletedAttachments[i]
            val findAttachment = findAttachments[i]
            
            assertThat(findAttachment.postId).isEqualTo(attachment.postId)
            assertThat(findAttachment.originalFilename).isEqualTo(attachment.originalFilename)
            assertThat(findAttachment.storeFilename).isEqualTo(attachment.storeFilename)
            assertThat(findAttachment.storeFilePath).isEqualTo(attachment.storeFilePath)
            assertThat(findAttachment.fileSize).isEqualTo(attachment.fileSize)
            assertThat(findAttachment.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("게시글 기준 첨부파일 목록 조회")
    fun findAllByPost() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createAttachments(posts, total, true)
        val postId = posts[Random.nextInt(posts.size)].id!!
        
        //When
        val findAttachments = attachmentRepository.findAll(postId)
        
        //Then
        val attachments = attachmentMapper.findAll().filter { it.postId == postId }.sortedBy { it.id }
        
        assertThat(findAttachments.size).isEqualTo(attachments.size)
        for (i in 0 until attachments.size) {
            val attachment = attachments[i]
            val findAttachment = findAttachments[i]
            
            assertThat(findAttachment.postId).isEqualTo(attachment.postId)
            assertThat(findAttachment.originalFilename).isEqualTo(attachment.originalFilename)
            assertThat(findAttachment.storeFilename).isEqualTo(attachment.storeFilename)
            assertThat(findAttachment.storeFilePath).isEqualTo(attachment.storeFilePath)
            assertThat(findAttachment.fileSize).isEqualTo(attachment.fileSize)
        }
    }
    
    @Test
    @DisplayName("게시글 기준, 삭제 여부로 첨부파일 목록 조회")
    fun findAllByPostAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createAttachments(posts, total, true)
        dummy.randomlyDeleteAttachments(attachmentMapper.findAll(), dummy.generateRandomNumber(1, 100))
        val postId = posts[Random.nextInt(posts.size)].id!!
        
        //When
        val findAttachments = attachmentRepository.findAll(postId, false)
        
        //Then
        val undeletedAttachments = attachmentMapper.findAll().filter { it.postId == postId && !it.deleteYn }
                .sortedBy { it.id }
        
        assertThat(findAttachments.size).isEqualTo(undeletedAttachments.size)
        for (i in 0 until undeletedAttachments.size) {
            val attachment = undeletedAttachments[i]
            val findAttachment = findAttachments[i]
            
            assertThat(findAttachment.postId).isEqualTo(attachment.postId)
            assertThat(findAttachment.originalFilename).isEqualTo(attachment.originalFilename)
            assertThat(findAttachment.storeFilename).isEqualTo(attachment.storeFilename)
            assertThat(findAttachment.storeFilePath).isEqualTo(attachment.storeFilePath)
            assertThat(findAttachment.fileSize).isEqualTo(attachment.fileSize)
            assertThat(findAttachment.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("첨부파일 삭제")
    fun deleteById() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val attachment = dummy.createAttachments(posts, 1, true)[0]
        val id = attachment.id!!
        val postId = attachment.postId
        
        //When
        attachmentRepository.deleteById(id, postId)
        
        //Then
        val deletedAttachment = attachmentMapper.findById(id)
        
        assertThat(deletedAttachment.deleteYn).isTrue
    }
    
    @Test
    @DisplayName("첨부파일 정보 영구 삭제")
    fun permanentlyDeleteById() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val attachment = dummy.createAttachments(posts, 1, true)[0]
        val id = attachment.id!!
        val postId = attachment.postId
        attachmentMapper.deleteById(id, postId)
        
        //When
        attachmentRepository.permanentlyDeleteById(id)
        
        //Then
        val unknownAttachment = attachmentMapper.findById(id)
        
        assertThat(unknownAttachment).isNull()
    }
    
    @Test
    @DisplayName("첨부파일 목록 삭제")
    fun deleteAllByIds() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val attachments = dummy.createAttachments(posts, dummy.generateRandomNumber(100, 300), true)
        val postId = posts[Random.nextInt(posts.size)].id!!
        val ids = attachments.filter { it.postId == postId }.map { it.id!! }
        
        //When
        attachmentRepository.deleteAllByIds(ids, postId)
        
        //Then
        val deletedAttachments = attachmentMapper.findAll().filter { it.postId == postId }
        
        for (attachment in deletedAttachments)
            assertThat(attachment.deleteYn).isTrue
    }
    
    @Test
    @DisplayName("첨부파일 정보 목록 영구 삭제")
    fun permanentlyDeleteAllByIds() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val attachments = dummy.createAttachments(posts, dummy.generateRandomNumber(100, 300), true)
        val postId = posts[Random.nextInt(posts.size)].id!!
        val ids = attachments.filter { it.postId == postId }.map { it.id!! }
        attachmentMapper.deleteAllByIds(ids, postId)
        
        //When
        attachmentRepository.permanentlyDeleteAllByIds(ids)
        
        //Then
        val unknownAttachments = attachmentMapper.findAll().filter { it.postId == postId }
        
        assertThat(unknownAttachments).isEmpty()
    }
    
}