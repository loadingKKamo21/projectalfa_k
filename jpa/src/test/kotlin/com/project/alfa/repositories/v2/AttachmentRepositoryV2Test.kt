package com.project.alfa.repositories.v2

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.entities.Attachment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Import(TestConfig::class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class AttachmentRepositoryV2Test {
    
    @Autowired
    lateinit var attachmentRepository: AttachmentRepositoryV2
    
    @PersistenceContext
    lateinit var em: EntityManager
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @AfterEach
    fun clear() {
        em.flush()
        em.clear()
    }
    
    private fun randomlyDeleteAttachments(attachments: List<Attachment>, count: Int) {
        val random = Random()
        var deleteCount = 0
        while (count > 0) {
            if (count == deleteCount)
                break
            val attachment = attachments[random.nextInt(attachments.size)]
            if (attachment.deleteYn)
                continue
            attachment.isDelete(true)
            deleteCount++
        }
    }
    
    @Test
    @DisplayName("첨부파일 저장")
    fun save() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val attachment = dummy.createAttachments(posts, 1)[0]
        
        //When
        val id = attachmentRepository.save(attachment).id
        
        //Then
        val findAttachment = em.find(Attachment::class.java, id)
        
        assertThat(findAttachment).isEqualTo(attachment)
    }
    
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
        var attachments = dummy.createAttachments(posts, total)
        
        //When
        var savedAttachments = attachmentRepository.saveAll(attachments)
        
        //Then
        attachments = attachments.sortedBy { it.id }
        savedAttachments = savedAttachments.sortedBy { it.id }
        
        assertThat(savedAttachments.size).isEqualTo(total)
        for (i in 0 until total)
            assertThat(savedAttachments[i]).isEqualTo(attachments[i])
    }
    
    @Test
    @DisplayName("첨부파일 다중 저장")
    fun saveAllAndFlush() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(5, 10)
        var attachments = dummy.createAttachments(posts, total)
        
        //When
        var savedAttachments = attachmentRepository.saveAllAndFlush(attachments)
        
        //Then
        attachments = attachments.sortedBy { it.id }
        savedAttachments = savedAttachments.sortedBy { it.id }
        
        assertThat(savedAttachments.size).isEqualTo(total)
        for (i in 0 until total)
            assertThat(savedAttachments[i]).isEqualTo(attachments[i])
    }
    
    @Test
    @DisplayName("PK로 조회")
    fun findById() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val attachment = dummy.createAttachments(posts, 1)[0]
        em.persist(attachment)
        val id = attachment.id!!
        
        //When
        val findAttachment = attachmentRepository.findById(id).get()
        
        //Then
        assertThat(findAttachment).isEqualTo(attachment)
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    fun findById_unknown() {
        //Given
        val id = Random().nextLong()
        
        //When
        val unknownAttachment = attachmentRepository.findById(id)
        
        //Then
        assertThat(unknownAttachment.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회")
    fun findByIdAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val attachment = dummy.createAttachments(posts, 1)[0]
        em.persist(attachment)
        val id = attachment.id!!
        
        //When
        val findAttachment = attachmentRepository.findById(id, false).get()
        
        //Then
        assertThat(findAttachment).isEqualTo(attachment)
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 존재하지 않는 PK")
    fun findByIdAndDeleteYn_unknown() {
        //Given
        val id = Random().nextLong()
        
        //When
        val unknownAttachment = attachmentRepository.findById(id, false)
        
        //Then
        assertThat(unknownAttachment.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 이미 삭제한 첨부파일")
    fun findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val attachment = dummy.createAttachments(posts, 1)[0]
        em.persist(attachment)
        val id = attachment.id!!
        attachment.isDelete(true)
        
        //When
        val deletedAttachment = attachmentRepository.findById(id, false)
        
        //Then
        assertThat(deletedAttachment.isPresent).isFalse
    }
    
    @Test
    @DisplayName("첨부파일 목록조회")
    fun findAll() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var attachments = dummy.createAttachments(posts, total)
        for (attachment in attachments)
            em.persist(attachment)
        
        //When
        var findAttachments = attachmentRepository.findAll()
        
        //Then
        attachments = attachments.sortedBy { it.id }
        findAttachments = findAttachments.sortedBy { it.id }
        
        assertThat(findAttachments.size).isEqualTo(total)
        for (i in 0 until total)
            assertThat(findAttachments[i]).isEqualTo(attachments[i])
    }
    
    @Test
    @DisplayName("삭제 여부로 첨부파일 목록조회")
    fun findAllByDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var attachments = dummy.createAttachments(posts, total)
        for (attachment in attachments)
            em.persist(attachment)
        randomlyDeleteAttachments(attachments, dummy.generateRandomNumber(1, 100))
        
        //When
        var findAttachments = attachmentRepository.findAll(false)
        
        //Then
        attachments = attachments.filter { !it.deleteYn }.sortedBy { it.id }
        findAttachments = findAttachments.sortedBy { it.id }
        
        assertThat(findAttachments.size).isEqualTo(attachments.size)
        for (i in attachments.indices)
            assertThat(findAttachments[i]).isEqualTo(attachments[i])
    }
    
    @Test
    @DisplayName("PK 목록으로 첨부파일 목록조회")
    fun findAllByIds() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var attachments = dummy.createAttachments(posts, total)
        for (attachment in attachments)
            em.persist(attachment)
        val ids = attachments.map { it.id!! }
        
        //When
        var findAttachments = attachmentRepository.findAll(ids)
        
        //Then
        attachments = attachments.sortedBy { it.id }
        findAttachments = findAttachments.sortedBy { it.id }
        
        assertThat(findAttachments.size).isEqualTo(ids.size)
        for (i in ids.indices)
            assertThat(findAttachments[i]).isEqualTo(attachments[i])
    }
    
    @Test
    @DisplayName("PK 목록, 삭제 여부로 첨부파일 목록조회")
    fun findAllByIdsAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var attachments = dummy.createAttachments(posts, total)
        for (attachment in attachments)
            em.persist(attachment)
        randomlyDeleteAttachments(attachments, dummy.generateRandomNumber(1, 100))
        val ids = attachments.filter { !it.deleteYn }.map { it.id!! }
        
        //When
        var findAttachments = attachmentRepository.findAll(ids, false)
        
        //Then
        attachments = attachments.filter { ids.contains(it.id) && !it.deleteYn }.sortedBy { it.id }
        findAttachments = findAttachments.sortedBy { it.id }
        
        assertThat(findAttachments.size).isEqualTo(ids.size)
        for (i in ids.indices)
            assertThat(findAttachments[i]).isEqualTo(attachments[i])
    }
    
    @Test
    @DisplayName("게시글 기준 첨부파일 목록조회")
    fun findAllByPost() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var attachments = dummy.createAttachments(posts, total)
        for (attachment in attachments)
            em.persist(attachment)
        val postId = posts[Random().nextInt(posts.size)].id!!
        
        //When
        var findAttachments = attachmentRepository.findAll(postId)
        
        //Then
        attachments = attachments.filter { it.post.id == postId }.sortedBy { it.id }
        findAttachments = findAttachments.sortedBy { it.id }
        
        assertThat(findAttachments.size).isEqualTo(attachments.size)
        for (i in attachments.indices)
            assertThat(findAttachments[i]).isEqualTo(attachments[i])
    }
    
    @Test
    @DisplayName("게시글 기준, 삭제 여부로 첨부파일 목록조회")
    fun findAllByPostAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var attachments = dummy.createAttachments(posts, total)
        for (attachment in attachments)
            em.persist(attachment)
        randomlyDeleteAttachments(attachments, dummy.generateRandomNumber(1, 100))
        val postId = posts[Random().nextInt(posts.size)].id!!
        
        //When
        var findAttachments = attachmentRepository.findAll(postId, false)
        
        //Then
        attachments = attachments.filter { it.post.id == postId && !it.deleteYn }.sortedBy { it.id }
        findAttachments = findAttachments.sortedBy { it.id }
        
        assertThat(findAttachments.size).isEqualTo(attachments.size)
        for (i in attachments.indices)
            assertThat(findAttachments[i]).isEqualTo(attachments[i])
    }
    
    @Test
    @DisplayName("엔티티로 첨부파일 정보 영구 삭제")
    fun delete() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val attachment = dummy.createAttachments(posts, 1)[0]
        em.persist(attachment)
        val id = attachment.id
        
        //When
        attachmentRepository.delete(attachment)
        
        //Then
        val deletedAttachment = em.find(Attachment::class.java, id)
        
        assertThat(deletedAttachment).isNull()
    }
    
    @Test
    @DisplayName("PK로 첨부파일 정보 영구 삭제")
    fun deleteById() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val attachment = dummy.createAttachments(posts, 1)[0]
        em.persist(attachment)
        val id = attachment.id!!
        
        //When
        attachmentRepository.deleteById(id)
        
        //Then
        val deletedAttachment = em.find(Attachment::class.java, id)
        
        assertThat(deletedAttachment).isNull()
    }
    
    @Test
    @DisplayName("엔티티 목록으로 첨부파일 정보 목록 영구 삭제")
    fun deleteAll() {
        //Given
        val writers = dummy.createMembers(10)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50))
        for (post in posts)
            em.persist(post)
        val attachments = dummy.createAttachments(posts, dummy.generateRandomNumber(50, 100))
        for (attachment in attachments)
            em.persist(attachment)
        
        //When
        attachmentRepository.deleteAll(attachments)
        
        //Then
        val ids = attachments.map { it.id }
        
        for (id in ids)
            assertThat(em.find(Attachment::class.java, id)).isNull()
    }
    
    @Test
    @DisplayName("엔티티 목록으로 첨부파일 정보 목록 영구 삭제")
    fun deleteAllInBatch() {
        //Given
        val writers = dummy.createMembers(10)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50))
        for (post in posts)
            em.persist(post)
        val attachments = dummy.createAttachments(posts, dummy.generateRandomNumber(50, 100))
        for (attachment in attachments)
            em.persist(attachment)
        
        //When
        attachmentRepository.deleteAllInBatch(attachments)
        clear()
        
        //Then
        val ids = attachments.map { it.id }
        
        for (id in ids)
            assertThat(em.find(Attachment::class.java, id)).isNull()
    }
    
    @Test
    @DisplayName("PK 목록으로 첨부파일 정보 목록 영구 삭제")
    fun deleteAllById() {
        //Given
        val writers = dummy.createMembers(10)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50))
        for (post in posts)
            em.persist(post)
        val attachments = dummy.createAttachments(posts, dummy.generateRandomNumber(50, 100))
        for (attachment in attachments)
            em.persist(attachment)
        val ids = attachments.map { it.id!! }
        
        //When
        attachmentRepository.deleteAllById(ids)
        
        //Then
        for (id in ids)
            assertThat(em.find(Attachment::class.java, id)).isNull()
    }
    
    @Test
    @DisplayName("PK 목록으로 첨부파일 정보 목록 영구 삭제")
    fun deleteAllByIdInBatch() {
        //Given
        val writers = dummy.createMembers(10)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50))
        for (post in posts)
            em.persist(post)
        val attachments = dummy.createAttachments(posts, dummy.generateRandomNumber(50, 100))
        for (attachment in attachments)
            em.persist(attachment)
        val ids = attachments.map { it.id!! }
        
        //When
        attachmentRepository.deleteAllByIdInBatch(ids)
        clear()
        
        //Then
        for (id in ids)
            assertThat(em.find(Attachment::class.java, id)).isNull()
    }
    
}