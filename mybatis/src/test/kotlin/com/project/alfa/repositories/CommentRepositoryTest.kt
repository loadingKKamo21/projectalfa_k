package com.project.alfa.repositories

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.entities.Comment
import com.project.alfa.repositories.mybatis.CommentMapper
import org.assertj.core.api.Assertions.assertThat
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
internal class CommentRepositoryTest {
    
    @Autowired
    lateinit var commentRepository: CommentRepository
    
    @Autowired
    lateinit var commentMapper: CommentMapper
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @Test
    @DisplayName("댓글 저장")
    fun save() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, false)[0]
        
        //When
        val savedComment = commentRepository.save(comment)
        val id = savedComment.id!!
        
        //Then
        val findComment = commentMapper.findById(id)
        
        assertThat(findComment.writerId).isEqualTo(comment.writerId)
        assertThat(findComment.postId).isEqualTo(comment.postId)
        assertThat(findComment.content).isEqualTo(comment.content)
    }
    
    @Test
    @DisplayName("PK로 조회")
    fun findById() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        
        //When
        val findComment = commentRepository.findById(id).get()
        
        //Then
        assertThat(findComment.writerId).isEqualTo(comment.writerId)
        assertThat(findComment.postId).isEqualTo(comment.postId)
        assertThat(findComment.content).isEqualTo(comment.content)
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    fun findById_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        val unknownComment = commentRepository.findById(id)
        
        //Then
        assertThat(unknownComment.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회")
    fun findByIdAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        
        //When
        val findComment = commentRepository.findById(id, false).get()
        
        //Then
        assertThat(findComment.writerId).isEqualTo(comment.writerId)
        assertThat(findComment.postId).isEqualTo(comment.postId)
        assertThat(findComment.content).isEqualTo(comment.content)
        assertThat(findComment.deleteYn).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 존재하지 않는 PK")
    fun findByIdAndDeleteYn_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        val unknownComment = commentRepository.findById(id, false)
        
        //Then
        assertThat(unknownComment.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 이미 삭제된 댓글")
    fun findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        commentMapper.deleteById(id, comment.writerId)
        
        //When
        val deletedComment = commentRepository.findById(id, false)
        
        //Then
        assertThat(deletedComment.isPresent).isFalse
    }
    
    @Test
    @DisplayName("댓글 목록 조회")
    fun findAll() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        val comments = dummy.createComments(writers, posts, total, true)
        
        //When
        val findComments = commentRepository.findAll().sortedBy { it.id }
        
        //Then
        assertThat(findComments.size).isEqualTo(total)
        for (i in 0 until total) {
            val comment = comments[i]
            val findComment = findComments[i]
            
            assertThat(findComment.writerId).isEqualTo(comment.writerId)
            assertThat(findComment.postId).isEqualTo(comment.postId)
            assertThat(findComment.content).isEqualTo(comment.content)
        }
    }
    
    @Test
    @DisplayName("삭제 여부로 댓글 목록 조회")
    fun findAllByDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createComments(writers, posts, total, true)
        dummy.randomlyDeleteComments(commentMapper.findAll(), dummy.generateRandomNumber(1, 100))
        
        //When
        val findComments = commentRepository.findAll(false).sortedBy { it.id }
        
        //Then
        val undeletedComments = commentMapper.findAll().filter { !it.deleteYn }.sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(undeletedComments.size)
        for (i in undeletedComments.indices) {
            val comment = undeletedComments[i]
            val findComment = findComments[i]
            
            assertThat(findComment.writerId).isEqualTo(comment.writerId)
            assertThat(findComment.postId).isEqualTo(comment.postId)
            assertThat(findComment.content).isEqualTo(comment.content)
            assertThat(findComment.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("PK 목록으로 댓글 목록 조회")
    fun findAllByIds() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        val ids = dummy.createComments(writers, posts, total, true).map { it.id!! }
        
        //When
        val findComments = commentRepository.findAll(ids)
        
        //Then
        val comments = commentMapper.findAll().filter { ids.contains(it.id) }.sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(comments.size)
        for (i in comments.indices) {
            val comment = comments[i]
            val findComment = findComments[i]
            
            assertThat(findComment.writerId).isEqualTo(comment.writerId)
            assertThat(findComment.postId).isEqualTo(comment.postId)
            assertThat(findComment.content).isEqualTo(comment.content)
        }
    }
    
    @Test
    @DisplayName("PK 목록, 삭제 여부로 댓글 목록 조회")
    fun findAllByIdsAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        val comments = dummy.createComments(writers, posts, total, true)
        dummy.randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100))
        val ids = comments.filter { !it.deleteYn }.map { it.id!! }
        
        //When
        val findComments = commentRepository.findAll(ids, false)
        
        //Then
        val undeletedComments = commentMapper.findAll()
                .filter { ids.contains(it.id) && !it.deleteYn }
                .sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(undeletedComments.size)
        for (i in undeletedComments.indices) {
            val comment = undeletedComments[i]
            val findComment = findComments[i]
            
            assertThat(findComment.writerId).isEqualTo(comment.writerId)
            assertThat(findComment.postId).isEqualTo(comment.postId)
            assertThat(findComment.content).isEqualTo(comment.content)
            assertThat(findComment.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("작성자 기준 댓글 목록 조회")
    fun findAllByWriter() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createComments(writers, posts, total, true)
        val writerId = writers[Random.nextInt(writers.size)].id!!
        
        //When
        val findComments = commentRepository.findAllByWriter(writerId)
        
        //Then
        val comments = commentMapper.findAll().filter { it.writerId == writerId }.sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(comments.size)
        for (i in comments.indices) {
            val comment = comments[i]
            val findComment = findComments[i]
            
            assertThat(findComment.writerId).isEqualTo(comment.writerId)
            assertThat(findComment.postId).isEqualTo(comment.postId)
            assertThat(findComment.content).isEqualTo(comment.content)
        }
    }
    
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 댓글 목록 조회")
    fun findAllByWriterAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createComments(writers, posts, total, true)
        dummy.randomlyDeleteComments(commentMapper.findAll(), dummy.generateRandomNumber(1, 100))
        val writerId = writers[Random.nextInt(writers.size)].id!!
        
        //When
        val findComments = commentRepository.findAllByWriter(writerId, false)
        
        //Then
        val undeletedComments = commentMapper.findAll()
                .filter { it.writerId == writerId && !it.deleteYn }
                .sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(undeletedComments.size)
        for (i in undeletedComments.indices) {
            val comment = undeletedComments[i]
            val findComment = findComments[i]
            
            assertThat(findComment.writerId).isEqualTo(comment.writerId)
            assertThat(findComment.postId).isEqualTo(comment.postId)
            assertThat(findComment.content).isEqualTo(comment.content)
            assertThat(findComment.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("게시글 기준 댓글 목록 조회")
    fun findAllByPost() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createComments(writers, posts, total, true)
        val postId = posts[Random.nextInt(posts.size)].id!!
        
        //When
        val findComments = commentRepository.findAllByPost(postId)
        
        //Then
        val comments = commentMapper.findAll().filter { it.postId == postId }.sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(comments.size)
        for (i in comments.indices) {
            val comment = comments[i]
            val findComment = findComments[i]
            
            assertThat(findComment.writerId).isEqualTo(comment.writerId)
            assertThat(findComment.postId).isEqualTo(comment.postId)
            assertThat(findComment.content).isEqualTo(comment.content)
        }
    }
    
    @Test
    @DisplayName("게시글 기준, 삭제 여부로 댓글 목록 조회")
    fun findAllByPostAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createComments(writers, posts, total, true)
        dummy.randomlyDeleteComments(commentMapper.findAll(), dummy.generateRandomNumber(1, 100))
        val postId = posts[Random.nextInt(posts.size)].id!!
        
        //When
        val findComments = commentRepository.findAllByPost(postId, false)
        
        //Then
        val undeletedComments = commentMapper.findAll()
                .filter { it.postId == postId && !it.deleteYn }
                .sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(undeletedComments.size)
        for (i in undeletedComments.indices) {
            val comment = undeletedComments[i]
            val findComment = findComments[i]
            
            assertThat(findComment.writerId).isEqualTo(comment.writerId)
            assertThat(findComment.postId).isEqualTo(comment.postId)
            assertThat(findComment.content).isEqualTo(comment.content)
            assertThat(findComment.deleteYn).isFalse
        }
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
        val findComments = commentRepository.findAllByWriter(writerId, pageRequest)
        
        //Then
        val comments = commentMapper.findAll()
                .filter { it.writerId == writerId }
                .sortedByDescending { it.createdDate }
                .take(10)
        
        assertThat(findComments.size).isEqualTo(comments.size)
        for (i in comments.indices) {
            val comment = comments[i]
            val findComment = findComments[i]
            
            assertThat(findComment.writerId).isEqualTo(comment.writerId)
            assertThat(findComment.postId).isEqualTo(comment.postId)
            assertThat(findComment.content).isEqualTo(comment.content)
        }
    }
    
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 댓글 페이징 목록 조회")
    fun findAllPageByWriterAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createComments(writers, posts, total, true)
        dummy.randomlyDeleteComments(commentMapper.findAll(), dummy.generateRandomNumber(1, 100))
        val writerId = writers[Random.nextInt(writers.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findComments = commentRepository.findAllByWriter(writerId, false, pageRequest)
        
        //Then
        val undeletedComments = commentMapper.findAll()
                .filter { it.writerId == writerId && !it.deleteYn }
                .sortedByDescending { it.createdDate }
                .take(10)
        
        assertThat(findComments.size).isEqualTo(undeletedComments.size)
        for (i in undeletedComments.indices) {
            val comment = undeletedComments[i]
            val findComment = findComments[i]
            
            assertThat(findComment.writerId).isEqualTo(comment.writerId)
            assertThat(findComment.postId).isEqualTo(comment.postId)
            assertThat(findComment.content).isEqualTo(comment.content)
            assertThat(findComment.deleteYn).isFalse
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
        val findComments = commentRepository.findAllByPost(postId, pageRequest)
        
        //Then
        val comments = commentMapper.findAll()
                .filter { it.postId == postId }
                .sortedByDescending { it.createdDate }
                .take(10)
        
        assertThat(findComments.size).isEqualTo(comments.size)
        for (i in comments.indices) {
            val comment = comments[i]
            val findComment = findComments[i]
            
            assertThat(findComment.writerId).isEqualTo(comment.writerId)
            assertThat(findComment.postId).isEqualTo(comment.postId)
            assertThat(findComment.content).isEqualTo(comment.content)
        }
    }
    
    @Test
    @DisplayName("게시글 기준, 삭제 여부로 댓글 페이징 목록 조회")
    fun findAllPageByPostAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createComments(writers, posts, total, true)
        dummy.randomlyDeleteComments(commentMapper.findAll(), dummy.generateRandomNumber(1, 100))
        val postId = posts[Random.nextInt(posts.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findComments = commentRepository.findAllByPost(postId, false, pageRequest)
        
        //Then
        val undeletedComments = commentMapper.findAll()
                .filter { it.postId == postId && !it.deleteYn }
                .sortedByDescending { it.createdDate }
                .take(10)
        
        assertThat(findComments.size).isEqualTo(undeletedComments.size)
        for (i in undeletedComments.indices) {
            val comment = undeletedComments[i]
            val findComment = findComments[i]
            
            assertThat(findComment.writerId).isEqualTo(comment.writerId)
            assertThat(findComment.postId).isEqualTo(comment.postId)
            assertThat(findComment.content).isEqualTo(comment.content)
            assertThat(findComment.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("댓글 수정")
    fun update() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        val beforeContent = comment.content
        
        var afterContent: String
        do {
            afterContent = dummy.generateRandomString(dummy.generateRandomNumber(1, 100))
        } while (beforeContent == afterContent)
        
        val param = Comment(id = id, writerId = comment.writerId, postId = comment.postId, content = afterContent)
        
        //When
        commentRepository.update(param)
        
        //Then
        val findComment = commentMapper.findById(id)
        
        assertThat(findComment.content).isEqualTo(afterContent)
        assertThat(findComment.content).isNotEqualTo(beforeContent)
    }
    
    @Test
    @DisplayName("댓글 삭제")
    fun deleteById() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        val writerId = comment.writerId
        
        //When
        commentRepository.deleteById(id, writerId)
        
        //Then
        val deletedComment = commentMapper.findById(id)
        
        assertThat(deletedComment.deleteYn).isTrue
    }
    
    @Test
    @DisplayName("댓글 정보 영구 삭제")
    fun permanentlyDeleteById() {
        //Given
        val writers = dummy.createMembers(1, true)
        val posts = dummy.createPosts(writers, 1, true)
        val comment = dummy.createComments(writers, posts, 1, true)[0]
        val id = comment.id!!
        val writerId = comment.writerId
        commentMapper.deleteById(id, writerId)
        
        //When
        commentRepository.permanentlyDeleteById(id)
        
        //Then
        val unknownComment = commentMapper.findById(id)
        
        assertThat(unknownComment).isNull()
    }
    
    @Test
    @DisplayName("댓글 목록 삭제")
    fun deleteAllByIds() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val comments = dummy.createComments(writers, posts, dummy.generateRandomNumber(100, 300), true)
        val writerId = writers[Random.nextInt(writers.size)].id!!
        val ids = comments.filter { it.writerId == writerId }.map { it.id!! }
        
        //When
        commentRepository.deleteAllByIds(ids, writerId)
        
        //Then
        val deletedComments = commentMapper.findAll().filter { it.writerId == writerId }
        
        for (comment in deletedComments)
            assertThat(comment.deleteYn).isTrue
    }
    
    @Test
    @DisplayName("댓글 정보 목록 영구 삭제")
    fun permanentlyDeleteAllByIds() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true)
        val comments = dummy.createComments(writers, posts, dummy.generateRandomNumber(100, 300), true)
        val writerId = writers[Random.nextInt(writers.size)].id!!
        val ids = comments.filter { it.writerId == writerId }.map { it.id!! }
        commentMapper.deleteAllByIds(ids, writerId)
        
        //When
        commentRepository.permanentlyDeleteAllByIds(ids)
        
        //Then
        val unknownComments = commentMapper.findAll().filter { it.writerId == writerId }
        
        assertThat(unknownComments).isEmpty()
    }
    
}