package com.project.alfa.repositories.v2

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.entities.Comment
import org.assertj.core.api.Assertions.assertThat
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
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Import(TestConfig::class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class CommentRepositoryV2Test {
    
    @Autowired
    lateinit var commentRepository: CommentRepositoryV2
    
    @PersistenceContext
    lateinit var em: EntityManager
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @AfterEach
    fun clear() {
        em.flush()
        em.clear()
    }
    
    private fun randomlyDeleteComments(comments: List<Comment>, count: Int) {
        val random = Random()
        var deleteCount = 0
        while (count > 0) {
            if (count == deleteCount)
                break
            val comment = comments[random.nextInt(comments.size)]
            if (comment.deleteYn)
                continue
            comment.isDelete(true)
            deleteCount++
        }
    }
    
    @Test
    @DisplayName("댓글 저장")
    fun save() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val comment = dummy.createComments(writers, posts, 1)[0]
        
        //When
        val id = commentRepository.save(comment).id
        
        //Then
        val findComment = em.find(Comment::class.java, id)
        
        assertThat(findComment).isEqualTo(comment)
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
        val comment = dummy.createComments(writers, posts, 1)[0]
        em.persist(comment)
        val id = comment.id!!
        
        //When
        val findComment = commentRepository.findById(id).get()
        
        //Then
        assertThat(findComment).isEqualTo(comment)
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    fun findById_unknown() {
        //Given
        val id = Random().nextLong()
        
        //When
        val unknownComment = commentRepository.findById(id)
        
        //Then
        assertThat(unknownComment.isPresent).isFalse
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
        val comment = dummy.createComments(writers, posts, 1)[0]
        em.persist(comment)
        val id = comment.id!!
        
        //When
        val findComment = commentRepository.findById(id, false).get()
        
        //Then
        assertThat(findComment).isEqualTo(comment)
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 존재하지 않는 PK")
    fun findByIdAndDeleteYn_unknown() {
        //Given
        val id = Random().nextLong()
        
        //When
        val unknownComment = commentRepository.findById(id, false)
        
        //Then
        assertThat(unknownComment.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 이미 삭제한 댓글")
    fun findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val comment = dummy.createComments(writers, posts, 1)[0]
        em.persist(comment)
        val id = comment.id!!
        comment.isDelete(true)
        
        //When
        val deletedComment = commentRepository.findById(id, false)
        
        //Then
        assertThat(deletedComment.isPresent).isFalse
    }
    
    @Test
    @DisplayName("댓글 목록 조회")
    fun findAll() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var comments = dummy.createComments(writers, posts, total)
        for (comment in comments)
            em.persist(comment)
        
        //When
        var findComments: List<Comment> = commentRepository.findAll()
        
        //Then
        comments = comments.sortedBy { it.id }
        findComments = findComments.sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(total)
        for (i in 0 until total)
            assertThat(findComments[i]).isEqualTo(comments[i])
    }
    
    @Test
    @DisplayName("삭제 여부로 댓글 목록 조회")
    fun findAllByDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var comments = dummy.createComments(writers, posts, total)
        for (comment in comments)
            em.persist(comment)
        randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100))
        
        //When
        var findComments = commentRepository.findAll(false)
        
        //Then
        comments = comments.filter { !it.deleteYn }.sortedBy { it.id }
        findComments = findComments.sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(comments.size)
        for (i in comments.indices)
            assertThat(findComments[i]).isEqualTo(comments[i])
    }
    
    @Test
    @DisplayName("PK 목록으로 댓글 목록 조회")
    fun findAllByIds() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var comments = dummy.createComments(writers, posts, total)
        for (comment in comments)
            em.persist(comment)
        val ids = comments.map { it.id!! }
        
        //When
        var findComments = commentRepository.findAll(ids)
        
        //Then
        comments = comments.sortedBy { it.id }
        findComments = findComments.sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(ids.size)
        for (i in ids.indices)
            assertThat(findComments[i]).isEqualTo(comments[i])
    }
    
    @Test
    @DisplayName("PK 목록, 삭제 여부로 댓글 목록 조회")
    fun findAllByIdsAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var comments = dummy.createComments(writers, posts, total)
        for (comment in comments)
            em.persist(comment)
        randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100))
        val ids = comments.filter { !it.deleteYn }.map { it.id!! }
        
        //When
        var findComments = commentRepository.findAll(ids, false)
        
        //Then
        comments = comments.filter { ids.contains(it.id) && !it.deleteYn }.sortedBy { it.id }
        findComments = findComments.sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(ids.size)
        for (i in ids.indices)
            assertThat(findComments[i]).isEqualTo(comments[i])
    }
    
    @Test
    @DisplayName("작성자 기준 댓글 목록 조회")
    fun findAllByWriter() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var comments = dummy.createComments(writers, posts, total)
        for (comment in comments)
            em.persist(comment)
        val writerId = writers[Random().nextInt(writers.size)].id!!
        
        //When
        var findComments = commentRepository.findAllByWriter(writerId)
        
        //Then
        comments = comments.filter { it.writer.id == writerId }.sortedBy { it.id }
        findComments = findComments.sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(comments.size)
        for (i in comments.indices)
            assertThat(findComments[i]).isEqualTo(comments[i])
    }
    
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 댓글 목록 조회")
    fun findAllByWriterAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var comments = dummy.createComments(writers, posts, total)
        for (comment in comments)
            em.persist(comment)
        randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100))
        val writerId = writers[Random().nextInt(writers.size)].id!!
        
        //When
        var findComments = commentRepository.findAllByWriter(writerId, false)
        
        //Then
        comments = comments.filter { it.writer.id == writerId && !it.deleteYn }.sortedBy { it.id }
        findComments = findComments.sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(comments.size)
        for (i in comments.indices)
            assertThat(findComments[i]).isEqualTo(comments[i])
    }
    
    @Test
    @DisplayName("게시글 기준 댓글 목록 조회")
    fun findAllByPost() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var comments = dummy.createComments(writers, posts, total)
        for (comment in comments)
            em.persist(comment)
        val postId = posts[Random().nextInt(posts.size)].id!!
        
        //When
        var findComments = commentRepository.findAllByPost(postId)
        
        //Then
        comments = comments.filter { it.post.id == postId }.sortedBy { it.id }
        findComments = findComments.sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(comments.size)
        for (i in comments.indices)
            assertThat(findComments[i]).isEqualTo(comments[i])
    }
    
    @Test
    @DisplayName("게시글 기준, 삭제 여부로 댓글 목록 조회")
    fun findAllByPostAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var comments = dummy.createComments(writers, posts, total)
        for (comment in comments)
            em.persist(comment)
        randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100))
        val postId = posts[Random().nextInt(posts.size)].id!!
        
        //When
        var findComments = commentRepository.findAllByPost(postId, false)
        
        //Then
        comments = comments.filter { it.post.id == postId && !it.deleteYn }.sortedBy { it.id }
        findComments = findComments.sortedBy { it.id }
        
        assertThat(findComments.size).isEqualTo(comments.size)
        for (i in comments.indices)
            assertThat(findComments[i]).isEqualTo(comments[i])
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
        var comments = dummy.createComments(writers, posts, total)
        for (comment in comments) {
            Thread.sleep(1)
            em.persist(comment)
        }
        val writerId = writers[Random().nextInt(writers.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findComments = commentRepository.findAllByWriter(writerId, pageRequest)
        
        //Then
        comments = comments.filter { it.writer.id == writerId }.sortedByDescending { it.createdDate }.take(10)
        
        assertThat(findComments.content.size).isEqualTo(comments.size)
        for (i in comments.indices)
            assertThat(findComments.content[i]).isEqualTo(comments[i])
    }
    
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 게시글 페이징 목록 조회")
    fun findAllPageByWriterAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var comments = dummy.createComments(writers, posts, total)
        for (comment in comments) {
            Thread.sleep(1)
            em.persist(comment)
        }
        randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100))
        val writerId = writers[Random().nextInt(writers.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findComments = commentRepository.findAllByWriter(writerId, false, pageRequest)
        
        //Then
        comments = comments.filter { it.writer.id == writerId && !it.deleteYn }
                .sortedByDescending { it.createdDate }.take(10)
        
        assertThat(findComments.content.size).isEqualTo(comments.size)
        for (i in comments.indices)
            assertThat(findComments.content[i]).isEqualTo(comments[i])
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
        var comments = dummy.createComments(writers, posts, total)
        for (comment in comments) {
            Thread.sleep(1)
            em.persist(comment)
        }
        val postId = posts[Random().nextInt(posts.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findComments = commentRepository.findAllByPost(postId, pageRequest)
        
        //Then
        comments = comments.filter { it.post.id == postId }.sortedByDescending { it.createdDate }.take(10)
        
        assertThat(findComments.content.size).isEqualTo(comments.size)
        for (i in comments.indices)
            assertThat(findComments.content[i]).isEqualTo(comments[i])
    }
    
    @Test
    @DisplayName("게시글 기준, 삭제 여부로 게시글 페이징 목록 조회")
    fun findAllPageByPostAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100))
        for (post in posts)
            em.persist(post)
        val total = dummy.generateRandomNumber(100, 300)
        var comments = dummy.createComments(writers, posts, total)
        for (comment in comments) {
            Thread.sleep(1)
            em.persist(comment)
        }
        randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100))
        val postId = posts[Random().nextInt(posts.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findComments = commentRepository.findAllByPost(postId, false, pageRequest)
        
        //Then
        comments = comments.filter { it.post.id == postId && !it.deleteYn }
                .sortedByDescending { it.createdDate }.take(10)
        
        assertThat(findComments.content.size).isEqualTo(comments.size)
        for (i in comments.indices)
            assertThat(findComments.content[i]).isEqualTo(comments[i])
    }
    
    @Test
    @DisplayName("댓글 수정")
    fun update() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val comment = dummy.createComments(writers, posts, 1)[0]
        em.persist(comment)
        val id = comment.id!!
        clear()
        
        //When
        var newContent: String
        do {
            newContent = dummy.generateRandomString(dummy.generateRandomNumber(1, 100))
        } while (comment.content == newContent)
        
        val findComment = commentRepository.findById(id).get()
        findComment.updateContent(newContent)
        clear()
        
        //Then
        val updatedComment = em.find(Comment::class.java, id)
        
        assertThat(updatedComment.content).isEqualTo(newContent)
    }
    
    @Test
    @DisplayName("엔티티로 댓글 정보 영구 삭제")
    fun delete() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val comment = dummy.createComments(writers, posts, 1)[0]
        em.persist(comment)
        val id = comment.id
        
        //When
        commentRepository.delete(comment)
        
        //Then
        val deletedComment = em.find(Comment::class.java, id)
        
        assertThat(deletedComment).isNull()
    }
    
    @Test
    @DisplayName("PK로 댓글 정보 영구 삭제")
    fun deleteById() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val comment = dummy.createComments(writers, posts, 1)[0]
        em.persist(comment)
        val id = comment.id!!
        
        //When
        commentRepository.deleteById(id)
        
        //Then
        val deletedComment = em.find(Comment::class.java, id)
        
        assertThat(deletedComment).isNull()
    }
    
    @Test
    @DisplayName("엔티티 목록으로 댓글 정보 목록 영구 삭제")
    fun deleteAll() {
        //Given
        val writers = dummy.createMembers(10)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50))
        for (post in posts)
            em.persist(post)
        val comments = dummy.createComments(writers, posts, dummy.generateRandomNumber(50, 100))
        for (comment in comments)
            em.persist(comment)
        
        //When
        commentRepository.deleteAll(comments)
        
        //Then
        val ids = comments.map { it.id }
        
        for (id in ids)
            assertThat(em.find(Comment::class.java, id)).isNull()
    }
    
    @Test
    @DisplayName("엔티티 목록으로 댓글 정보 목록 영구 삭제")
    fun deleteAllInBatch() {
        //Given
        val writers = dummy.createMembers(10)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50))
        for (post in posts)
            em.persist(post)
        val comments = dummy.createComments(writers, posts, dummy.generateRandomNumber(50, 100))
        for (comment in comments)
            em.persist(comment)
        
        //When
        commentRepository.deleteAllInBatch(comments)
        clear()
        
        //Then
        val ids = comments.map { it.id }
        
        for (id in ids)
            assertThat(em.find(Comment::class.java, id)).isNull()
    }
    
    @Test
    @DisplayName("PK 목록으로 댓글 정보 목록 영구 삭제")
    fun deleteAllById() {
        //Given
        val writers = dummy.createMembers(10)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50))
        for (post in posts)
            em.persist(post)
        val comments = dummy.createComments(writers, posts, dummy.generateRandomNumber(50, 100))
        for (comment in comments)
            em.persist(comment)
        val ids = comments.map { it.id!! }
        
        //When
        commentRepository.deleteAllById(ids)
        
        //Then
        for (id in ids)
            assertThat(em.find(Comment::class.java, id)).isNull()
    }
    
    @Test
    @DisplayName("PK 목록으로 댓글 정보 목록 영구 삭제")
    fun deleteAllByIdInBatch() {
        //Given
        val writers = dummy.createMembers(10)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50))
        for (post in posts)
            em.persist(post)
        val comments = dummy.createComments(writers, posts, dummy.generateRandomNumber(50, 100))
        for (comment in comments)
            em.persist(comment)
        val ids = comments.map { it.id!! }
        
        //When
        commentRepository.deleteAllByIdInBatch(ids)
        clear()
        
        //Then
        for (id in ids)
            assertThat(em.find(Comment::class.java, id)).isNull()
    }
    
}