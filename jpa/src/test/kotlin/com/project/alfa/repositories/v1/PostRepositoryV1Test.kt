package com.project.alfa.repositories.v1

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.entities.Post
import com.project.alfa.entities.Role
import com.project.alfa.repositories.dto.SearchParam
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
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
internal class PostRepositoryV1Test {
    
    @Autowired
    lateinit var postRepository: PostRepositoryV1
    
    @PersistenceContext
    lateinit var em: EntityManager
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @AfterEach
    fun clear() {
        em.flush()
        em.clear()
    }
    
    private fun randomlyDeletePosts(posts: List<Post>, count: Int) {
        val random = Random()
        var deleteCount = 0
        while (count > 0) {
            if (count == deleteCount)
                break
            val post = posts[random.nextInt(posts.size)]
            if (post.deleteYn)
                continue
            post.isDelete(true)
            deleteCount++
        }
    }
    
    @Test
    @DisplayName("게시글 저장")
    fun save() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val post = dummy.createPosts(writers, 1)[0]
        
        //When
        val id = postRepository.save(post).id
        
        //Then
        val findPost = em.find(Post::class.java, id)
        
        assertThat(findPost).isEqualTo(post)
    }
    
    @Test
    @DisplayName("PK로 조회")
    fun findById() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val post = dummy.createPosts(writers, 1)[0]
        em.persist(post)
        val id = post.id!!
        
        //When
        val findPost = postRepository.findById(id).get()
        
        //Then
        assertThat(findPost).isEqualTo(post)
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    fun findById_unknown() {
        //Given
        val id = Random().nextLong()
        
        //When
        val unknownPost = postRepository.findById(id)
        
        //Then
        assertThat(unknownPost.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회")
    fun findByIdAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val post = dummy.createPosts(writers, 1)[0]
        em.persist(post)
        val id = post.id!!
        
        //When
        val findPost = postRepository.findById(id, false).get()
        
        //Then
        assertThat(findPost).isEqualTo(post)
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 존재하지 않는 PK")
    fun findByIdAndDeleteYn_unknown() {
        //Given
        val id = Random().nextLong()
        
        //When
        val unknownPost = postRepository.findById(id, false)
        
        //Then
        assertThat(unknownPost.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 이미 삭제한 게시글")
    fun findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val post = dummy.createPosts(writers, 1)[0]
        em.persist(post)
        val id = post.id!!
        post.isDelete(true)
        
        //When
        val deletedPost = postRepository.findById(id, false)
        
        //Then
        assertThat(deletedPost.isPresent).isFalse
    }
    
    @Test
    @DisplayName("게시글 목록 조회")
    fun findAll() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts)
            em.persist(post)
        
        //When
        var findPosts = postRepository.findAll()
        
        //Then
        posts = posts.sortedBy { it.id }
        findPosts = findPosts.sortedBy { it.id }
        
        assertThat(findPosts.size).isEqualTo(total)
        for (i in 0 until total)
            assertThat(findPosts[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("삭제 여부로 게시글 목록 조회")
    fun findAllByDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts)
            em.persist(post)
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100))
        
        //When
        var findPosts = postRepository.findAll(false)
        
        //Then
        posts = posts.filter { !it.deleteYn }.sortedBy { it.id }
        findPosts = findPosts.sortedBy { it.id }
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in 0 until posts.size)
            assertThat(findPosts[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("PK 목록으로 게시글 목록 조회")
    fun findAllByIds() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts)
            em.persist(post)
        val ids = posts.map { it.id!! }
        
        //When
        var findPosts = postRepository.findAll(ids)
        
        //Then
        posts = posts.sortedBy { it.id }
        findPosts = findPosts.sortedBy { it.id }
        
        assertThat(findPosts.size).isEqualTo(ids.size)
        for (i in ids.indices)
            assertThat(findPosts[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("PK 목록, 삭제 여부로 게시글 목록 조회")
    fun findAllByIdsAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers) em.persist(writer)
        
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts)
            em.persist(post)
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100))
        val ids = posts.filter { !it.deleteYn }.map { it.id!! }
        
        //When
        var findPosts = postRepository.findAll(ids, false)
        
        //Then
        posts = posts.filter { ids.contains(it.id) && !it.deleteYn }.sortedBy { it.id }
        findPosts = findPosts.sortedBy { it.id }
        
        assertThat(findPosts.size).isEqualTo(ids.size)
        for (i in ids.indices)
            assertThat(findPosts[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("작성자 기준 게시글 목록 조회")
    fun findAllByWriter() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts)
            em.persist(post)
        val writerId = writers[Random().nextInt(writers.size)].id!!
        
        //When
        var findPosts = postRepository.findAll(writerId)
        
        //Then
        posts = posts.filter { it.writer.id == writerId }.sortedBy { it.id }
        findPosts = findPosts.sortedBy { it.id }
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices)
            assertThat(findPosts[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 게시글 목록 조회")
    fun findAllByWriterAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts)
            em.persist(post)
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100))
        val writerId = writers[Random().nextInt(writers.size)].id!!
        
        //When
        var findPosts = postRepository.findAll(writerId, false)
        
        //Then
        posts = posts.filter { it.writer.id == writerId && !it.deleteYn }.sortedBy { it.id }
        findPosts = findPosts.sortedBy { it.id }
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices)
            assertThat(findPosts[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("게시글 페이징 목록 조회")
    fun findAllPage() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts) {
            Thread.sleep(1)
            em.persist(post)
        }
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(pageRequest)
        
        //Then
        posts = posts.sortedByDescending { it.createdDate }.take(10)
        
        assertThat(findPosts.content.size).isEqualTo(posts.size)
        for (i in posts.indices)
            assertThat(findPosts.content[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("삭제 여부로 게시글 페이징 목록 조회")
    fun findAllPageByDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts) {
            Thread.sleep(1)
            em.persist(post)
        }
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100))
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(false, pageRequest)
        
        //Then
        posts = posts.filter { !it.deleteYn }.sortedByDescending { it.createdDate }.take(10)
        
        assertThat(findPosts.content.size).isEqualTo(posts.size)
        for (i in posts.indices)
            assertThat(findPosts.content[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("작성자 기준 게시글 페이징 목록 조회")
    fun findAllPageByWriter() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts) {
            Thread.sleep(1)
            em.persist(post)
        }
        val writerId = writers[Random().nextInt(writers.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(writerId, pageRequest)
        
        //Then
        posts = posts.filter { it.writer.id == writerId }.sortedByDescending { it.createdDate }.take(10)
        
        assertThat(findPosts.content.size).isEqualTo(posts.size)
        for (i in posts.indices)
            assertThat(findPosts.content[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 게시글 페이징 목록 조회")
    fun findAllPageByWriterAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts) {
            Thread.sleep(1)
            em.persist(post)
        }
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100))
        val writerId = writers[Random().nextInt(writers.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(writerId, false, pageRequest)
        
        //Then
        posts = posts.filter { it.writer.id == writerId && !it.deleteYn }.sortedByDescending { it.createdDate }.take(10)
        
        assertThat(findPosts.content.size).isEqualTo(posts.size)
        for (i in posts.indices)
            assertThat(findPosts.content[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("검색 조건, 키워드('키워드')로 게시글 페이징 목록 조회")
    fun findAllPageBySearchParam_OneWord() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts) {
            Thread.sleep(1)
            em.persist(post)
        }
        val keyword = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val param = SearchParam("title", keyword)
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(param, pageRequest)
        
        //Then
        posts = posts.filter { it.title.contains(keyword) }.sortedByDescending { it.createdDate }.take(10)
        
        assertThat(findPosts.content.size).isEqualTo(posts.size)
        for (i in posts.indices)
            assertThat(findPosts.content[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("검색 조건, 키워드('키워드1 키워드2')로 게시글 페이징 목록 조회")
    fun findAllPageBySearchParam_MultiWord() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts) {
            Thread.sleep(1)
            em.persist(post)
        }
        val keyword1 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val keyword2 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val param = SearchParam("titleOrContent", "$keyword1 $keyword2")
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(param, pageRequest)
        
        //Then
        posts = posts.filter {
            it.title.contains(keyword1) || it.title.contains(keyword2) ||
                    it.content!!.contains(keyword1) || it.content!!.contains(keyword2)
        }.sortedByDescending { it.createdDate }.take(10)
        
        assertThat(findPosts.content.size).isEqualTo(posts.size)
        for (i in posts.indices)
            assertThat(findPosts.content[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("검색 조건, 키워드('키워드'), 삭제 여부로 게시글 페이징 목록 조회")
    fun findAllPageBySearchParamAndDeleteYn_OneWord() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts) {
            Thread.sleep(1)
            em.persist(post)
        }
        val keyword = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val param = SearchParam("content", keyword)
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100))
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(param, false, pageRequest)
        
        //Then
        posts = posts.filter { it.content!!.contains(keyword) && !it.deleteYn }
                .sortedByDescending { it.createdDate }.take(10)
        
        assertThat(findPosts.content.size).isEqualTo(posts.size)
        for (i in posts.indices)
            assertThat(findPosts.content[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("검색 조건, 키워드('키워드1 키워드2'), 삭제 여부로 게시글 페이징 목록 조회")
    fun findAllPageBySearchParamAndDeleteYn_MultiWord() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        var posts = dummy.createPosts(writers, total)
        for (post in posts) {
            Thread.sleep(1)
            em.persist(post)
        }
        val keyword1 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val keyword2 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val param = SearchParam("titleOrContent", "$keyword1 $keyword2")
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100))
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(param, false, pageRequest)
        
        //Then
        posts = posts.filter {
            (it.title.contains(keyword1) || it.title.contains(keyword2) ||
                    it.content!!.contains(keyword1) || it.content!!.contains(keyword2)) && !it.deleteYn
        }.sortedByDescending { it.createdDate }.take(10)
        
        assertThat(findPosts.content.size).isEqualTo(posts.size)
        for (i in posts.indices)
            assertThat(findPosts.content[i]).isEqualTo(posts[i])
    }
    
    @Test
    @DisplayName("조회수 증가")
    fun addViewCount() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val post = dummy.createPosts(writers, 1)[0]
        em.persist(post)
        val id = post.id
        val beforeViewCount = post.viewCount
        
        //When
        post.addViewCount()
        
        //Then
        val afterViewCount = em.find(Post::class.java, id).viewCount
        
        assertThat(afterViewCount).isEqualTo(beforeViewCount + 1)
    }
    
    @Test
    @DisplayName("게시글 수정")
    fun update() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers) {
            em.persist(writer)
            writer.updateRole(Role.ADMIN)
        }
        val post = dummy.createPosts(writers, 1)[0]
        em.persist(post)
        val id = post.id!!
        clear()
        
        //When
        var newTitle: String
        var newContent: String
        do {
            newTitle = dummy.generateRandomString(dummy.generateRandomNumber(1, 100))
            newContent = dummy.generateRandomString(dummy.generateRandomNumber(100, 500))
        } while (post.title == newTitle || post.content == newContent)
        
        val findPost = postRepository.findById(id).get()
        findPost.updateTitle(newTitle)
        findPost.updateContent(newContent)
        findPost.updateNoticeYn(true)
        clear()
        
        //Then
        val updatedPost = em.find(Post::class.java, id)
        
        assertThat(updatedPost.title).isEqualTo(newTitle)
        assertThat(updatedPost.content).isEqualTo(newContent)
        assertThat(updatedPost.noticeYn).isTrue()
    }
    
    @Test
    @DisplayName("엔티티로 게시글 정보 영구 삭제")
    fun delete() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val post = dummy.createPosts(writers, 1)[0]
        em.persist(post)
        val id = post.id
        
        //When
        postRepository.delete(post)
        
        //Then
        val deletedPost = em.find(Post::class.java, id)
        
        assertThat(deletedPost).isNull()
    }
    
    @Test
    @DisplayName("PK로 게시글 정보 영구 삭제")
    fun deleteById() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val post = dummy.createPosts(writers, 1)[0]
        em.persist(post)
        val id = post.id!!
        
        //When
        postRepository.deleteById(id)
        
        //Then
        val deletedPost = em.find(Post::class.java, id)
        
        assertThat(deletedPost).isNull()
    }
    
    @Test
    @DisplayName("엔티티 목록으로 게시글 정보 목록 영구 삭제")
    fun deleteAll() {
        //Given
        val writers = dummy.createMembers(10)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50))
        for (post in posts)
            em.persist(post)
        
        //When
        postRepository.deleteAll(posts)
        
        //Then
        val ids = posts.map { it.id }
        
        for (id in ids)
            assertThat(em.find(Post::class.java, id)).isNull()
    }
    
    @Test
    @DisplayName("엔티티 목록으로 게시글 정보 목록 영구 삭제")
    fun deleteAllInBatch() {
        //Given
        val writers = dummy.createMembers(10)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50))
        for (post in posts)
            em.persist(post)
        
        //When
        postRepository.deleteAllInBatch(posts)
        clear()
        
        //Then
        val ids = posts.map { it.id }
        
        for (id in ids)
            assertThat(em.find(Post::class.java, id)).isNull()
    }
    
    @Test
    @DisplayName("PK 목록으로 게시글 정보 목록 영구 삭제")
    fun deleteAllById() {
        //Given
        val writers = dummy.createMembers(10)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50))
        for (post in posts)
            em.persist(post)
        val ids = posts.map { it.id!! }
        
        //When
        postRepository.deleteAllById(ids)
        
        //Then
        for (id in ids)
            assertThat(em.find(Post::class.java, id)).isNull()
    }
    
    @Test
    @DisplayName("PK 목록으로 게시글 정보 목록 영구 삭제")
    fun deleteAllByIdInBatch() {
        //Given
        val writers = dummy.createMembers(10)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50))
        for (post in posts)
            em.persist(post)
        val ids = posts.map { it.id!! }
        
        //When
        postRepository.deleteAllByIdInBatch(ids)
        clear()
        
        //Then
        for (id in ids)
            assertThat(em.find(Post::class.java, id)).isNull()
    }
    
}