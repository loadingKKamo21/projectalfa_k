package com.project.alfa.services

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.error.exception.InvalidValueException
import com.project.alfa.repositories.CommentRepository
import com.project.alfa.repositories.mybatis.CommentMapper
import com.project.alfa.services.dto.CommentRequestDto
import com.project.alfa.services.dto.CommentResponseDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
    lateinit var commentRepository: CommentRepository
    
    @Autowired
    lateinit var commentMapper: CommentMapper
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @Test
    @DisplayName("댓글 작성")
    fun create() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val writer = writers[0]
        val dto = CommentRequestDto(id = null,
                                    writerId = writer.id!!,
                                    postId = post.id!!,
                                    content = dummy.generateRandomString(dummy.generateRandomNumber(1, 100)))
        
        //When
        val id = commentService.create(dto)
        
        //Then
        val findComment = commentMapper.findById(id)
        
        assertThat(dto.writerId).isEqualTo(findComment.writerId)
        assertThat(dto.postId).isEqualTo(findComment.postId)
        assertThat(dto.content).isEqualTo(findComment.content)
    }
    
    @Test
    @DisplayName("댓글 작성, 존재하지 않는 계정")
    fun create_unknownWriter() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        
        var writerId: Long
        do {
            writerId = Random.nextLong()
        } while (writers[0].id == writerId)
        
        val dto = CommentRequestDto(id = null,
                                    writerId = writerId,
                                    postId = post.id!!,
                                    content = dummy.generateRandomString(dummy.generateRandomNumber(1, 100)))
        
        //When
        
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
        val writerId = dummy.createMembers(1, true)[0].id!!
        val postId = Random.nextLong()
        val dto = CommentRequestDto(id = null,
                                    writerId = writerId,
                                    postId = postId,
                                    content = dummy.generateRandomString(dummy.generateRandomNumber(1, 100)))
        
        //When
        
        //Then
        assertThatThrownBy { commentService.create(dto) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: $postId")
    }
    
    @Test
    @DisplayName("PK로 댓글 상세 조회")
    fun findById() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        
        //When
        val dto = commentService.read(id)
        
        //Then
        val findComment = commentMapper.findById(id)
        
        assertThat(findComment.id).isEqualTo(dto.id)
        assertThat(findComment.nickname).isEqualTo(dto.writer)
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
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        commentMapper.deleteById(id, comment.writerId)
        
        //When
        
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
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        val writerId = comment.writerId
        val postId = comment.postId
        val beforeContent = comment.content
        
        var afterContent: String
        do {
            afterContent = dummy.generateRandomString(dummy.generateRandomNumber(1, 100))
        } while (beforeContent == afterContent)
        
        val dto = CommentRequestDto(id = id, writerId = writerId, postId = postId, content = afterContent)
        
        //When
        commentService.update(dto)
        
        //Then
        val findComment = commentMapper.findById(id)
        
        assertThat(findComment.content).isEqualTo(afterContent)
        assertThat(findComment.content).isNotEqualTo(beforeContent)
    }
    
    @Test
    @DisplayName("댓글 정보 수정, 접근 권한 없는 계정")
    fun update_notWriter() {
        //Given
        val writers = dummy.createMembers(2, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        val postId = comment.postId
        
        var anotherWriterId: Long? = null
        for (writer in writers)
            if (comment.writerId != writer.id) {
                anotherWriterId = writer.id!!
                break
            }
        
        val dto = CommentRequestDto(id = id,
                                    writerId = anotherWriterId!!,
                                    postId = postId,
                                    content = dummy.generateRandomString(dummy.generateRandomNumber(1, 100)))
        
        //When
        
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
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        val writerId = comment.writerId
        val postId = comment.postId
        commentMapper.deleteById(id, writerId)
        val dto = CommentRequestDto(id = id,
                                    writerId = writerId,
                                    postId = postId,
                                    content = dummy.generateRandomString(dummy.generateRandomNumber(1, 100)))
        
        //When
        
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
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        val writerId = comment.writerId
        
        //When
        commentService.delete(id, writerId)
        
        //Then
        val deletedComment = commentMapper.findById(id)
        
        assertThat(deletedComment.deleteYn).isTrue
    }
    
    @Test
    @DisplayName("댓글 삭제, 접근 권한 없는 계정")
    fun delete_notWriter() {
        //Given
        val writers = dummy.createMembers(2, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        
        var anotherWriterId: Long? = null
        for (writer in writers)
            if (comment.writerId != writer.id) {
                anotherWriterId = writer.id
                break
            }
        
        //When
        
        //Then
        assertThatThrownBy { commentService.delete(id, anotherWriterId!!) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_COMMENT)
                .hasMessage("Member do not have access.")
    }
    
    @Test
    @DisplayName("댓글 삭제, 이미 삭제된 댓글")
    fun delete_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        val writerId = comment.writerId
        commentMapper.deleteById(id, writerId)
        
        //When
        
        //Then
        assertThatThrownBy { commentService.delete(id, writerId) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Comment' by id: $id")
    }
    
    @Test
    @DisplayName("댓글 목록 삭제")
    fun deleteAll() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val total = dummy.generateRandomNumber(10, 50)
        val comments = dummy.createComments(writers, posts, total, true)
        val writerId = writers[0].id!!
        val ids = comments.filter { it.writerId == writerId }.map { it.id!! }
        
        //When
        commentService.deleteAll(ids, writerId)
        
        //Then
        val deletedComments = commentMapper.findAll()
                .filter { ids.contains(it.id) && it.writerId == writerId && it.deleteYn }
        
        for (comment in deletedComments)
            assertThat(comment.deleteYn).isTrue
    }
    
    @Test
    @DisplayName("댓글 목록 삭제, 접근 권한 없는 계정")
    fun deleteAll_notWriter() {
        //Given
        val writers = dummy.createMembers(2, true)
        val posts = dummy.createPosts(writers, 1, true)
        val total = dummy.generateRandomNumber(10, 50)
        val comments = dummy.createComments(writers, posts, total, true)
        val writerId = writers[Random.nextInt(writers.size)].id!!
        val ids = comments.filter { it.writerId != writerId }.map { it.id!! }
        
        //When
        
        //Then
        assertThatThrownBy { commentService.deleteAll(ids, writerId) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_COMMENT)
                .hasMessage("Member do not have access.")
    }
    
    @Test
    @DisplayName("작성자 기준 댓글 페이징 목록 조회")
    fun findAllPageByWriter() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createComments(writers, posts, total, true)
        val writerId = writers[Random.nextInt(writers.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findComments = commentService.findAllPageByWriter(writerId, pageRequest)
        
        //Then
        val comments = commentMapper.findAll()
                .filter { it.writerId == writerId }
                .sortedByDescending { it.createdDate }
                .take(10)
                .map { CommentResponseDto(it) }
        
        assertThat(findComments.size).isEqualTo(comments.size)
        for (i in comments.indices) {
            val commentDto = comments[i]
            val findCommentDto = findComments[i]
            
            assertThat(findCommentDto.id).isEqualTo(commentDto.id)
            assertThat(findCommentDto.writer).isEqualTo(commentDto.writer)
            assertThat(findCommentDto.content).isEqualTo(commentDto.content)
            assertThat(findCommentDto.createdDate).isEqualTo(commentDto.createdDate)
            assertThat(findCommentDto.lastModifiedDate).isEqualTo(commentDto.lastModifiedDate)
        }
    }
    
    @Test
    @DisplayName("게시글 기준 댓글 페이징 목록 조회")
    fun findAllPageByPost() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createComments(writers, posts, total, true)
        val postId = posts[Random.nextInt(posts.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findComments = commentService.findAllPageByPost(postId, pageRequest)
        
        //Then
        val comments = commentMapper.findAll()
                .filter { it.postId == postId }
                .sortedByDescending { it.createdDate }
                .take(10)
                .map { CommentResponseDto(it) }
        
        assertThat(findComments.size).isEqualTo(comments.size)
        for (i in comments.indices) {
            val commentDto = comments[i]
            val findCommentDto = findComments[i]
            
            assertThat(findCommentDto.id).isEqualTo(commentDto.id)
            assertThat(findCommentDto.writer).isEqualTo(commentDto.writer)
            assertThat(findCommentDto.content).isEqualTo(commentDto.content)
            assertThat(findCommentDto.createdDate).isEqualTo(commentDto.createdDate)
            assertThat(findCommentDto.lastModifiedDate).isEqualTo(commentDto.lastModifiedDate)
        }
    }
    
}