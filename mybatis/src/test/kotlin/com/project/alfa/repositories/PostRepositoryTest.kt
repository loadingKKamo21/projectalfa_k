package com.project.alfa.repositories

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.entities.Post
import com.project.alfa.repositories.dto.SearchParam
import com.project.alfa.repositories.mybatis.PostMapper
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
internal class PostRepositoryTest {
    
    @Autowired
    lateinit var postRepository: PostRepository
    
    @Autowired
    lateinit var postMapper: PostMapper
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @Test
    @DisplayName("게시글 저장")
    fun save() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, false)[0]
        
        //When
        val savedPost = postRepository.save(post)
        val id = savedPost.id!!
        
        //Then
        val findPost = postMapper.findById(id)
        
        assertThat(findPost.writerId).isEqualTo(savedPost.writerId)
        assertThat(findPost.title).isEqualTo(savedPost.title)
        assertThat(findPost.content).isEqualTo(savedPost.content)
        assertThat(findPost.noticeYn).isEqualTo(savedPost.noticeYn)
    }
    
    @Test
    @DisplayName("PK로 조회")
    fun findById() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        
        //When
        val findPost = postRepository.findById(id).get()
        
        //Then
        assertThat(findPost.writerId).isEqualTo(post.writerId)
        assertThat(findPost.title).isEqualTo(post.title)
        assertThat(findPost.content).isEqualTo(post.content)
        assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    fun findById_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        val unknownPost = postRepository.findById(id)
        
        //Then
        assertThat(unknownPost.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회")
    fun findByIdAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        
        //When
        val findPost = postRepository.findById(id, false).get()
        
        //Then
        assertThat(findPost.writerId).isEqualTo(post.writerId)
        assertThat(findPost.title).isEqualTo(post.title)
        assertThat(findPost.content).isEqualTo(post.content)
        assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 존재하지 않는 PK")
    fun findByIdAndDeleteYn_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        val unknownPost = postRepository.findById(id, false)
        
        //Then
        assertThat(unknownPost.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 이미 삭제한 게시글")
    fun findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        postMapper.deleteById(id, post.writerId)
        
        //When
        val deletedPost = postRepository.findById(id, false)
        
        //Then
        assertThat(deletedPost.isPresent).isFalse
    }
    
    @Test
    @DisplayName("게시글 목록 조회")
    fun findAll() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        val posts = dummy.createPosts(writers, total, true)
        
        //When
        val findPosts = postRepository.findAll().sortedBy { it.id }
        
        //Then
        assertThat(findPosts.size).isEqualTo(total)
        for (i in 0 until total) {
            val post = posts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
        }
    }
    
    @Test
    @DisplayName("삭제 여부로 게시글 목록 조회")
    fun findAllByDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        dummy.randomlyDeletePosts(postMapper.findAll(), dummy.generateRandomNumber(1, 100))
        
        //When
        val findPosts = postRepository.findAll(false).sortedBy { it.id }
        
        //Then
        val undeletedPosts = postMapper.findAll().filter { !it.deleteYn }.sortedBy { it.id }
        
        assertThat(findPosts.size).isEqualTo(undeletedPosts.size)
        for (i in undeletedPosts.indices) {
            val post = undeletedPosts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
            assertThat(findPost.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("PK 목록으로 게시글 목록 조회")
    fun findAllByIds() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        val ids = dummy.createPosts(writers, total, true).map { it.id!! }
        
        //When
        val findPosts = postRepository.findAll(ids)
        
        //Then
        val posts = postMapper.findAll().filter { ids.contains(it.id) }.sortedBy { it.id }
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val post = posts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
        }
    }
    
    @Test
    @DisplayName("PK 목록, 삭제 여부로 게시글 목록 조회")
    fun findAllByIdsAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        val posts = dummy.createPosts(writers, total, true)
        dummy.randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100))
        val ids = posts.filter { !it.deleteYn }.map { it.id!! }
        
        //When
        val findPosts = postRepository.findAll(ids, false)
        
        //Then
        val undeletedPosts = postMapper.findAll().filter { ids.contains(it.id) && !it.deleteYn }.sortedBy { it.id }
        
        assertThat(findPosts.size).isEqualTo(undeletedPosts.size)
        for (i in undeletedPosts.indices) {
            val post = undeletedPosts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
            assertThat(findPost.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("작성자 기준 게시글 목록 조회")
    fun findAllByWriter() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        val writerId = writers[Random.nextInt(writers.size)].id!!
        
        //When
        val findPosts = postRepository.findAll(writerId)
        
        //Then
        val posts = postMapper.findAll().filter { it.writerId == writerId }.sortedBy { it.id }
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val post = posts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
        }
    }
    
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 게시글 목록 조회")
    fun findAllByWriterAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        dummy.randomlyDeletePosts(postMapper.findAll(), dummy.generateRandomNumber(1, 100))
        val writerId = writers[Random.nextInt(writers.size)].id!!
        
        //When
        val findPosts = postRepository.findAll(writerId, false)
        
        //Then
        val posts = postMapper.findAll().filter { it.writerId == writerId && !it.deleteYn }.sortedBy { it.id }
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val post = posts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
            assertThat(findPost.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("게시글 페이징 목록 조회")
    fun findAllPage() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(pageRequest)
        
        //Then
        val posts = postMapper.findAll().sortedByDescending { it.createdDate }.take(10)
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val post = posts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
        }
    }
    
    @Test
    @DisplayName("삭제 여부로 게시글 페이징 목록 조회")
    fun findAllPageByDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        dummy.randomlyDeletePosts(postMapper.findAll(), dummy.generateRandomNumber(1, 100))
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(false, pageRequest)
        
        //Then
        val undeletedPosts = postMapper.findAll()
                .filter { !it.deleteYn }
                .sortedByDescending { it.createdDate }
                .take(10)
        
        assertThat(findPosts.size).isEqualTo(undeletedPosts.size)
        for (i in undeletedPosts.indices) {
            val post = undeletedPosts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
            assertThat(findPost.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("작성자 기준 게시글 페이징 목록 조회")
    fun findAllPageByWriter() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        val writerId = writers[Random.nextInt(writers.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(writerId, pageRequest)
        
        //Then
        val posts = postMapper.findAll()
                .filter { it.writerId == writerId }
                .sortedByDescending { it.createdDate }
                .take(10)
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val post = posts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
        }
    }
    
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 게시글 페이징 목록 조회")
    fun findAllPageByWriterAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        dummy.randomlyDeletePosts(postMapper.findAll(), dummy.generateRandomNumber(1, 100))
        val writerId = writers[Random.nextInt(writers.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(writerId, false, pageRequest)
        
        //Then
        val undeletedPosts = postMapper.findAll()
                .filter { it.writerId == writerId && !it.deleteYn }
                .sortedByDescending { it.createdDate }
                .take(10)
        
        assertThat(findPosts.size).isEqualTo(undeletedPosts.size)
        for (i in undeletedPosts.indices) {
            val post = undeletedPosts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
            assertThat(findPost.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("검색 조건, 키워드('키워드')로 게시글 페이징 목록 조회")
    fun findAllPageBySearchParam_OneWord() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        val keyword = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val param = SearchParam("title", keyword)
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(param, pageRequest)
        
        //Then
        val posts = postMapper.findAll()
                .filter { it.title.contains(keyword) }
                .sortedByDescending { it.createdDate }
                .take(10)
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val post = posts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
        }
    }
    
    @Test
    @DisplayName("검색 조건, 키워드('키워드1, 키워드2')로 게시글 페이징 목록 조회")
    fun findAllPageBySearchParam_MultiWord() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        val keyword1 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val keyword2 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val param = SearchParam("titleOrContent", "$keyword1 $keyword2")
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(param, pageRequest)
        
        //Then
        val posts = postMapper.findAll()
                .filter {
                    it.title.contains(keyword1) || it.title.contains(keyword2)
                            || it.content!!.contains(keyword1) || it.content!!.contains(keyword2)
                }
                .sortedByDescending { it.createdDate }
                .take(10)
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val post = posts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
        }
    }
    
    @Test
    @DisplayName("검색 조건, 키워드('키워드'), 삭제 여부로 게시글 페이징 목록 조회")
    fun findAllPageBySearchParamAndDeleteYn_OneWord() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        dummy.randomlyDeletePosts(postMapper.findAll(), dummy.generateRandomNumber(1, 100))
        val keyword = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val param = SearchParam("content", keyword)
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(param, false, pageRequest)
        
        //Then
        val undeletedPosts = postMapper.findAll()
                .filter { it.content!!.contains(keyword) && !it.deleteYn }
                .sortedByDescending { it.createdDate }
                .take(10)
        
        assertThat(findPosts.size).isEqualTo(undeletedPosts.size)
        for (i in undeletedPosts.indices) {
            val post = undeletedPosts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
            assertThat(findPost.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("검색 조건, 키워드('키워드1, 키워드2'), 삭제 여부로 게시글 페이징 목록 조회")
    fun findAllPageBySearchParamAndDeleteYn_MultiWord() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        dummy.randomlyDeletePosts(postMapper.findAll(), dummy.generateRandomNumber(1, 100))
        val keyword1 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val keyword2 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val param = SearchParam("titleOrContent", "$keyword1 $keyword2")
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postRepository.findAll(param, false, pageRequest)
        
        //Then
        val posts = postMapper.findAll()
                .filter {
                    (it.title.contains(keyword1) || it.title.contains(keyword2)
                            || it.content!!.contains(keyword1) || it.content!!.contains(keyword2))
                            && !it.deleteYn
                }
                .sortedByDescending { it.createdDate }
                .take(10)
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val post = posts[i]
            val findPost = findPosts[i]
            
            assertThat(findPost.writerId).isEqualTo(post.writerId)
            assertThat(findPost.title).isEqualTo(post.title)
            assertThat(findPost.content).isEqualTo(post.content)
            assertThat(findPost.noticeYn).isEqualTo(post.noticeYn)
            assertThat(findPost.deleteYn).isFalse
        }
    }
    
    @Test
    @DisplayName("조회수 증가")
    fun addViewCount() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        val beforeViewCount = postMapper.findById(id).viewCount
        
        //When
        postRepository.addViewCount(id)
        
        //Then
        val afterViewCount = postMapper.findById(id).viewCount
        
        assertThat(afterViewCount).isEqualTo(beforeViewCount + 1)
    }
    
    @Test
    @DisplayName("게시글 수정")
    fun update() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        val beforeTitle = post.title
        val beforeContent = post.content!!
        val beforeNoticeYn = post.noticeYn
        
        var afterTitle: String
        var afterContent: String
        do {
            afterTitle = dummy.generateRandomString(dummy.generateRandomNumber(1, 100))
            afterContent = dummy.generateRandomString(dummy.generateRandomNumber(100, 500))
        } while (beforeTitle == afterTitle || beforeContent == afterContent)
        
        val param = Post(id = id, writerId = post.writerId, title = afterTitle, content = afterContent, noticeYn = true)
        
        //When
        postRepository.update(param)
        
        //Then
        val findPost = postMapper.findById(id)
        
        assertThat(findPost.title).isEqualTo(afterTitle)
        assertThat(findPost.title).isNotEqualTo(beforeTitle)
        assertThat(findPost.content).isEqualTo(afterContent)
        assertThat(findPost.content).isNotEqualTo(beforeContent)
        assertThat(beforeNoticeYn).isFalse
        assertThat(findPost.noticeYn).isTrue
    }
    
    @Test
    @DisplayName("PK로 엔티티 존재 여부 확인")
    fun existsById() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        
        //When
        val exists = postRepository.existsById(id)
        
        //Then
        assertThat(exists).isTrue
    }
    
    @Test
    @DisplayName("PK로 엔티티 존재 여부 확인, 없음")
    fun existsById_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        val exists = postRepository.existsById(id)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 엔티티 존재 여부 확인")
    fun existsByIdAndDeleteYn() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        
        //When
        val exists = postRepository.existsById(id, false)
        
        //Then
        assertThat(exists).isTrue
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 엔티티 존재 여부 확인, 없음")
    fun existsByIdAndDeleteYn_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        val exists = postRepository.existsById(id, false)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 엔티티 존재 여부 확인, 이미 삭제된 게시글")
    fun existsByIdAndDeleteYn_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        postMapper.deleteById(id, post.writerId)
        
        //When
        val exists = postRepository.existsById(id, false)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("게시글 삭제")
    fun deleteById() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        val writerId = post.writerId
        
        //When
        postRepository.deleteById(id, writerId)
        
        //Then
        val deletedPost = postMapper.findById(id)
        
        assertThat(deletedPost.deleteYn).isTrue
    }
    
    @Test
    @DisplayName("게시글 정보 영구 삭제")
    fun permanentlyDeleteById() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        val writerId = post.writerId
        postMapper.deleteById(id, writerId)
        
        //When
        postRepository.permanentlyDeleteById(id)
        
        //Then
        val unknownPost = postMapper.findById(id)
        
        assertThat(unknownPost).isNull()
    }
    
    @Test
    @DisplayName("게시글 목록 삭제")
    fun deleteAllByIds() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(100, 300), true)
        val writerId = writers[Random.nextInt(writers.size)].id!!
        val ids = posts.filter { it.writerId == writerId }.map { it.id!! }
        
        //When
        postRepository.deleteAllByIds(ids, writerId)
        
        //Then
        val deletedPosts = postMapper.findAll().filter { it.writerId == writerId }
        
        for (post in deletedPosts)
            assertThat(post.deleteYn).isTrue
    }
    
    @Test
    @DisplayName("게시글 정보 목록 영구 삭제")
    fun permanentlyDeleteAllByIds() {
        //Given
        val writers = dummy.createMembers(20, true)
        val posts = dummy.createPosts(writers, dummy.generateRandomNumber(100, 300), true)
        val writerId = writers[Random.nextInt(writers.size)].id!!
        val ids = posts.filter { it.writerId == writerId }.map { it.id!! }
        postMapper.deleteAllByIds(ids, writerId)
        
        //When
        postRepository.permanentlyDeleteAllByIds(ids)
        
        //Then
        val unknownPosts = postMapper.findAll().filter { it.writerId == writerId }
        
        assertThat(unknownPosts).isEmpty()
    }
    
}