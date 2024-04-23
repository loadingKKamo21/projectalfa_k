package com.project.alfa.services

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.entities.Comment
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.error.exception.InvalidValueException
import com.project.alfa.repositories.v1.CommentRepositoryV1
import com.project.alfa.services.dto.CommentRequestDto
import com.project.alfa.services.dto.CommentResponseDto
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import kotlin.random.Random

@Import(TestConfig::class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class CommentServiceTest {
    
    @Autowired
    lateinit var commentService: CommentService
    
    @Autowired
    lateinit var commentRepository: CommentRepositoryV1
    
    //@Autowired
    //lateinit var commentRepository: CommentRepositoryV2
    
    //@Autowired
    //lateinit var commentRepository: CommentRepositoryV3
    
    @PersistenceContext
    lateinit var em: EntityManager
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @AfterEach
    fun clear() {
        em.flush()
        em.clear()
    }
    
    @Test
    @DisplayName("댓글 작성")
    fun create() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        posts.forEach { em.persist(it) }
        val dto = CommentRequestDto(null,
                                    writers[0].id,
                                    posts[0].id,
                                    dummy.generateRandomString(dummy.generateRandomNumber(1, 100)))
        
        //When
        val id = commentService.create(dto)
        clear()
        
        //Then
        val findComment = em.find(Comment::class.java, id)
        
        assertThat(dto.writerId).isEqualTo(findComment.writer.id)
        assertThat(dto.postId).isEqualTo(findComment.post.id)
        assertThat(dto.content).isEqualTo(findComment.content)
    }
    
    @Test
    @DisplayName("댓글 작성, 존재하지 않는 계정")
    fun create_unknownWriter() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        posts.forEach { em.persist(it) }
        var writerId: Long
        do {
            writerId = Random.nextLong()
        } while (writers[0].id == writerId)
        
        val dto = CommentRequestDto(null,
                                    writerId,
                                    posts[0].id,
                                    dummy.generateRandomString(dummy.generateRandomNumber(1, 100)))
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { commentService.create(dto) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Member' by id: $writerId")
    }
    
    @Test
    @DisplayName("댓글 작성, 존재하지 않는 게시글")
    fun create_unknownPost() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val postId = Random.nextLong()
        
        val dto = CommentRequestDto(null,
                                    writers[0].id,
                                    postId,
                                    dummy.generateRandomString(dummy.generateRandomNumber(1, 100)))
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { commentService.create(dto) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: $postId")
    }
    
    @Test
    @DisplayName("PK로 댓글 상세 조회")
    fun read() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val comments = dummy.createComments(writers, posts, 1)
        for (comment in comments)
            em.persist(comment)
        val id = comments[0].id!!
        
        //When
        val dto = commentService.read(id)
        clear()
        
        //Then
        val findComment = em.find(Comment::class.java, id)
        
        assertThat(findComment.id).isEqualTo(dto.id)
        assertThat(findComment.writer.nickname).isEqualTo(dto.writer)
        assertThat(findComment.content).isEqualTo(dto.content)
        assertThat(findComment.createdDate).isEqualTo(dto.createdDate)
        assertThat(findComment.lastModifiedDate).isEqualTo(dto.lastModifiedDate)
    }
    
    @Test
    @DisplayName("PK로 댓글 상세 조회, 존재하지 않는 PK")
    fun read_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { commentService.read(id) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Comment' by id: $id")
    }
    
    @Test
    @DisplayName("PK로 댓글 상세 조회, 이미 삭제된 댓글")
    fun read_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val comments = dummy.createComments(writers, posts, 1)
        for (comment in comments)
            em.persist(comment)
        val comment = comments[0]
        val id = comment.id!!
        comment.isDelete(true)
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { commentService.read(id) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Comment' by id: $id")
    }
    
    @Test
    @DisplayName("댓글 정보 수정")
    fun update() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val comments = dummy.createComments(writers, posts, 1)
        for (comment in comments)
            em.persist(comment)
        val comment = comments[0]
        val id = comment.id
        val beforeContent = comment.content
        
        var afterContent: String
        do {
            afterContent = dummy.generateRandomString(dummy.generateRandomNumber(1, 100))
        } while (beforeContent == afterContent)
        
        val dto = CommentRequestDto(id,
                                    comment.writer.id,
                                    comment.post.id,
                                    afterContent)
        
        //When
        commentService.update(dto)
        clear()
        
        //Then
        val findComment = em.find(Comment::class.java, id)
        
        assertThat(findComment.content).isEqualTo(afterContent)
        assertThat(findComment.content).isNotEqualTo(beforeContent)
    }
    
    @Test
    @DisplayName("댓글 정보 수정, 접근 권한 없는 계정")
    fun update_notWriter() {
        //Given
        val writers = dummy.createMembers(2)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val comments = dummy.createComments(writers, posts, 1)
        for (comment in comments)
            em.persist(comment)
        val comment = comments[0]
        val id = comment.id
        
        var anotherWriterId: Long? = null
        for (writer in writers) if (comment.writer.id != writer.id) {
            anotherWriterId = writer.id
            break
        }
        
        val dto = CommentRequestDto(id,
                                    anotherWriterId,
                                    comment.post.id,
                                    dummy.generateRandomString(dummy.generateRandomNumber(1, 100)))
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { commentService.update(dto) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_COMMENT)
                .hasMessage("Member do not have access.")
    }
    
    @Test
    @DisplayName("댓글 정보 수정, 이미 삭제된 댓글")
    fun update_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val comments = dummy.createComments(writers, posts, 1)
        for (comment in comments)
            em.persist(comment)
        val comment = comments[0]
        val id = comment.id
        comment.isDelete(true)
        
        val dto = CommentRequestDto(id,
                                    comment.writer.id,
                                    comment.post.id,
                                    dummy.generateRandomString(dummy.generateRandomNumber(1, 100)))
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { commentService.update(dto) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Comment' by id: $id")
    }
    
    @Test
    @DisplayName("댓글 삭제")
    fun delete() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val comments = dummy.createComments(writers, posts, 1)
        for (comment in comments)
            em.persist(comment)
        val comment = comments[0]
        val id = comment.id!!
        val writerId = comment.writer.id!!
        
        //When
        commentService.delete(id, writerId)
        clear()
        
        //Then
        val deletedComment = em.find(Comment::class.java, id)
        
        assertThat(deletedComment.deleteYn).isTrue()
    }
    
    @Test
    @DisplayName("댓글 삭제, 접근 권한 없는 계정")
    fun delete_notWriter() {
        //Given
        val writers = dummy.createMembers(2)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val comments = dummy.createComments(writers, posts, 1)
        for (comment in comments)
            em.persist(comment)
        val comment = comments[0]
        val id = comment.id!!
        
        var anotherWriterId: Long? = null
        for (writer in writers) if (comment.writer.id != writer.id) {
            anotherWriterId = writer.id
            break
        }
        val unknownWriterId = anotherWriterId!!
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { commentService.delete(id, unknownWriterId) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_COMMENT)
                .hasMessage("Member do not have access.")
    }
    
    @Test
    @DisplayName("댓글 목록 삭제")
    fun deleteAll() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(10, 50)
        val comments = dummy.createComments(writers, posts, total)
        for (comment in comments)
            em.persist(comment)
        val writerId = writers[0].id!!
        val ids = comments.filter { it.writer.id == writerId }.map { it.id!! }
        
        //When
        commentService.deleteAll(ids, writerId)
        clear()
        
        //Then
        val deletedComments = commentRepository.findAll()
                .filter { ids.contains(it.id) && it.writer.id == writerId && it.deleteYn }
        
        for (comment in deletedComments)
            assertThat(comment.deleteYn).isTrue()
    }
    
    @Test
    @DisplayName("댓글 목록 삭제, 접근 권한 없는 계정")
    fun deleteAll_notWriter() {
        //Given
        val writers = dummy.createMembers(2)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(10, 50)
        val comments = dummy.createComments(writers, posts, total)
        for (comment in comments)
            em.persist(comment)
        val writerId = writers[Random.nextInt(writers.size)].id
        val ids = comments.filter { it.writer.id != writerId }.map { it.id!! }
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { commentService.deleteAll(ids, writerId!!) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_COMMENT)
                .hasMessage("Member do not have access.")
    }
    
    @Test
    @DisplayName("작성자 기준 댓글 페이징 목록 조회")
    fun findAllPageByWriter() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createComments(writers, posts, total).forEach {
            Thread.sleep(1)
            em.persist(it)
        }
        val writerId = writers[Random.nextInt(writers.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findComments = commentService.findAllPageByWriter(writerId, pageRequest)
        clear()
        
        //Then
        val comments = commentRepository.findAll().filter { it.writer.id == writerId }
                .sortedByDescending { it.createdDate }.take(10).map { CommentResponseDto(it) }
        
        assertThat(findComments.content.size).isEqualTo(comments.size)
        for (i in comments.indices) {
            val (id, writer, content, createdDate, lastModifiedDate) = comments[i]
            val (id1, writer1, content1, createdDate1, lastModifiedDate1) = findComments.content[i]
            
            assertThat(id1).isEqualTo(id)
            assertThat(writer1).isEqualTo(writer)
            assertThat(content1).isEqualTo(content)
            assertThat(createdDate1).isEqualTo(createdDate)
            assertThat(lastModifiedDate1).isEqualTo(lastModifiedDate)
        }
    }
    
    @Test
    @DisplayName("게시글 기준 댓글 페이징 목록 조회")
    fun findAllPageByPost() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createComments(writers, posts, total).forEach {
            Thread.sleep(1)
            em.persist(it)
        }
        val postId = posts[Random.nextInt(posts.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findComments = commentService.findAllPageByPost(postId, pageRequest)
        clear()
        
        //Then
        val comments = commentRepository.findAll().filter { it.post.id == postId }.sortedByDescending { it.createdDate }
                .take(10).map { CommentResponseDto(it) }
        
        assertThat(findComments.content.size).isEqualTo(comments.size)
        for (i in comments.indices) {
            val (id, writer, content, createdDate, lastModifiedDate) = comments[i]
            val (id1, writer1, content1, createdDate1, lastModifiedDate1) = findComments.content[i]
            
            assertThat(id1).isEqualTo(id)
            assertThat(writer1).isEqualTo(writer)
            assertThat(content1).isEqualTo(content)
            assertThat(createdDate1).isEqualTo(createdDate)
            assertThat(lastModifiedDate1).isEqualTo(lastModifiedDate)
        }
    }
    
}