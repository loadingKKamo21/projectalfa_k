package com.project.alfa.services

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.config.redis.EmbeddedRedisConfig
import com.project.alfa.entities.Post
import com.project.alfa.entities.Role
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.error.exception.InvalidValueException
import com.project.alfa.repositories.dto.SearchParam
import com.project.alfa.repositories.v1.PostRepositoryV1
import com.project.alfa.services.dto.PostRequestDto
import com.project.alfa.services.dto.PostResponseDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Import(TestConfig::class, EmbeddedRedisConfig::class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class PostServiceTest {
    
    @Autowired
    lateinit var postService: PostService
    
    @Autowired
    lateinit var postRepository: PostRepositoryV1
    
    //@Autowired
    //lateinit var postRepository: PostRepositoryV2
    
    //@Autowired
    //lateinit var postRepository: PostRepositoryV3
    
    @PersistenceContext
    lateinit var em: EntityManager
    
    @Autowired
    lateinit var cacheManager: CacheManager
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @AfterEach
    fun clear() {
        em.flush()
        em.clear()
    }
    
    @Test
    @DisplayName("게시글 작성")
    fun create() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val dto = PostRequestDto(null,
                                 writers[0].id,
                                 dummy.generateRandomString(dummy.generateRandomNumber(1, 100)),
                                 dummy.generateRandomString(dummy.generateRandomNumber(100, 500)),
                                 false)
        
        //When
        val id = postService.create(dto)
        clear()
        
        //Then
        val findPost = em.find(Post::class.java, id)
        
        assertThat(dto.writerId).isEqualTo(findPost.writer.id)
        assertThat(dto.title).isEqualTo(findPost.title)
        assertThat(dto.content).isEqualTo(findPost.content)
        assertThat(dto.noticeYn).isEqualTo(findPost.noticeYn)
    }
    
    @Test
    @DisplayName("게시글 작성, 존재하지 않는 계정")
    fun create_unknownWriter() {
        //Given
        val writerId = Random().nextLong()
        val dto = PostRequestDto(null,
                                 writerId,
                                 dummy.generateRandomString(dummy.generateRandomNumber(1, 100)),
                                 dummy.generateRandomString(dummy.generateRandomNumber(100, 500)),
                                 false)
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { postService.create(dto) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Member' by id: $writerId")
    }
    
    @Test
    @DisplayName("PK로 게시글 상세 조회")
    fun read() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val id = posts[0].id!!
        
        //When
        val dto = postService.read(id)
        clear()
        
        //Then
        val findPost = em.find(Post::class.java, id)
        
        assertThat(findPost.id).isEqualTo(dto.id)
        assertThat(findPost.writer.nickname).isEqualTo(dto.writer)
        assertThat(findPost.title).isEqualTo(dto.title)
        assertThat(findPost.content).isEqualTo(dto.content)
        assertThat(findPost.viewCount).isEqualTo(dto.viewCount)
        assertThat(findPost.noticeYn).isEqualTo(dto.noticeYn)
        assertThat(findPost.getCommentsCount()).isEqualTo(dto.commentsCount)
        assertThat(findPost.getAttachmentsCount()).isEqualTo(dto.attachmentsCount)
        assertThat(findPost.createdDate).isEqualTo(dto.createdDate)
        assertThat(findPost.lastModifiedDate).isEqualTo(dto.lastModifiedDate)
    }
    
    @Test
    @DisplayName("PK로 게시글 상세 조회, 존재하지 않는 PK")
    fun read_unknown() {
        //Given
        val id = Random().nextLong()
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { postService.read(id) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: $id")
    }
    
    @Test
    @DisplayName("PK로 게시글 상세 조회, 이미 삭제된 게시글")
    fun read_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val post = posts[0]
        val id = post.id!!
        post.isDelete(true)
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { postService.read(id) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: $id")
    }
    
    @Test
    @DisplayName("PK로 게시글 상세 조회, 캐싱")
    fun read_caching() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val id = posts[0].id!!
        
        //When
        val dto = postService.readWithCaching(id, UUID.randomUUID().toString(), "127.0.0.1")
        clear()
        
        //Then
        val findPost = em.find(Post::class.java, id)
        val postCache = cacheManager.getCache("postCache")
        
        assertThat(findPost.id).isEqualTo(dto.id)
        assertThat(findPost.writer.nickname).isEqualTo(dto.writer)
        assertThat(findPost.title).isEqualTo(dto.title)
        assertThat(findPost.content).isEqualTo(dto.content)
        assertThat(findPost.viewCount).isEqualTo(dto.viewCount)
        assertThat(findPost.noticeYn).isEqualTo(dto.noticeYn)
        assertThat(findPost.getCommentsCount()).isEqualTo(dto.commentsCount)
        assertThat(findPost.getAttachmentsCount()).isEqualTo(dto.attachmentsCount)
        assertThat(findPost.createdDate).isEqualTo(dto.createdDate)
        assertThat(findPost.lastModifiedDate).isEqualTo(dto.lastModifiedDate)
        assertThat(postCache).isNotNull
    }
    
    @Test
    @DisplayName("조회수 증가")
    fun addViewCount() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val post = posts[0]
        val id = post.id!!
        val beforeViewCount = post.viewCount
        
        //When
        postService.addViewCount(id)
        clear()
        
        //Then
        val afterViewCount = em.find(Post::class.java, id).viewCount
        
        assertThat(afterViewCount).isEqualTo(beforeViewCount + 1)
    }
    
    @Test
    @DisplayName("조회수 증가, 이미 삭제된 게시글")
    fun addViewCount_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val post = posts[0]
        val id = post.id!!
        post.isDelete(true)
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { postService.addViewCount(id) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: $id")
    }
    
    @Test
    @DisplayName("조회수 증가, 캐싱")
    fun addViewCount_caching() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val post = posts[0]
        val id = post.id!!
        val beforeViewCount = post.viewCount
        
        //When
        postService.addViewCountWithCaching(id, UUID.randomUUID().toString(), "127.0.0.1")
        clear()
        
        //Then
        val afterViewCount = em.find(Post::class.java, id).viewCount
        val postCache = cacheManager.getCache("postCache")
        
        assertThat(afterViewCount).isEqualTo(beforeViewCount + 1)
        assertThat(postCache).isNotNull
    }
    
    @Test
    @DisplayName("게시글 정보 수정")
    fun update() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        writers[0].updateRole(Role.ADMIN) //게시글 작성 시 공지 여부 설정을 위한 계정 '관리자' 권한 부여
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val post = posts[0]
        val id = post.id!!
        val beforeTitle = post.title
        val beforeContent = post.content
        val beforeNoticeYn = post.noticeYn
        
        var afterTile: String
        var afterContent: String
        do {
            afterTile = dummy.generateRandomString(dummy.generateRandomNumber(1, 100))
            afterContent = dummy.generateRandomString(dummy.generateRandomNumber(100, 500))
        } while (beforeTitle == afterTile || beforeContent == afterContent)
        
        val dto = PostRequestDto(id, post.writer.id, afterTile, afterContent, true)
        
        //When
        postService.update(dto)
        clear()
        
        //Then
        val findPost = em.find(Post::class.java, id)
        
        assertThat(findPost.title).isEqualTo(afterTile)
        assertThat(findPost.title).isNotEqualTo(beforeTitle)
        assertThat(findPost.content).isEqualTo(afterContent)
        assertThat(findPost.content).isNotEqualTo(beforeContent)
        assertThat(beforeNoticeYn).isFalse
        assertThat(findPost.noticeYn).isTrue()
    }
    
    @Test
    @DisplayName("게시글 정보 수정, 접근 권한 없는 계정")
    fun update_notWriter() {
        //Given
        val writers = dummy.createMembers(2)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val post = posts[0]
        val id = post.id
        
        var anotherWriterId: Long? = null
        for (writer in writers)
            if (post.writer.id != writer.id) {
                anotherWriterId = writer.id
                break
            }
        
        val dto = PostRequestDto(id,
                                 anotherWriterId,
                                 dummy.generateRandomString(dummy.generateRandomNumber(1, 100)),
                                 dummy.generateRandomString(dummy.generateRandomNumber(100, 500)),
                                 false)
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { postService.update(dto) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_POST)
                .hasMessage("Member do not have access.")
    }
    
    @Test
    @DisplayName("게시글 정보 수정, 이미 삭제된 게시글")
    fun update_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val post = posts[0]
        val id = post.id
        post.isDelete(true)
        val dto = PostRequestDto(id,
                                 post.writer.id,
                                 dummy.generateRandomString(dummy.generateRandomNumber(1, 100)),
                                 dummy.generateRandomString(dummy.generateRandomNumber(100, 500)),
                                 false)
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { postService.update(dto) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: $id")
    }
    
    @Test
    @DisplayName("게시글 삭제")
    fun delete() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val post = posts[0]
        val id = post.id!!
        val writerId = post.writer.id!!
        
        //When
        postService.delete(id, writerId)
        clear()
        
        //Then
        val deletedPost = em.find(Post::class.java, id)
        
        assertThat(deletedPost.deleteYn).isTrue()
    }
    
    @Test
    @DisplayName("게시글 삭제, 접근 권한 없는 계정")
    fun delete_notWriter() {
        //Given
        val writers = dummy.createMembers(2)
        for (writer in writers)
            em.persist(writer)
        val posts = dummy.createPosts(writers, 1)
        for (post in posts)
            em.persist(post)
        val post = posts[0]
        val id = post.id!!
        
        var anotherWriterId: Long? = null
        for (writer in writers)
            if (post.writer.id != writer.id) {
                anotherWriterId = writer.id
                break
            }
        val unknownWriterId = anotherWriterId!!
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { postService.delete(id, unknownWriterId) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_POST)
                .hasMessage("Member do not have access.")
    }
    
    @Test
    @DisplayName("게시글 목록 삭제")
    fun deleteAll() {
        //Given
        val writers = dummy.createMembers(1)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(10, 50)
        val posts = dummy.createPosts(writers, total)
        for (post in posts)
            em.persist(post)
        val writerId = writers[0].id!!
        val ids = posts.filter { it.writer.id == writerId }.map { it.id!! }
        
        //When
        postService.deleteAll(ids, writerId)
        clear()
        
        //Then
        val deletedPosts = postRepository.findAll()
                .filter { ids.contains(it.id) && it.writer.id == writerId && it.deleteYn }
        
        for (post in deletedPosts)
            assertThat(post.deleteYn).isTrue()
    }
    
    @Test
    @DisplayName("게시글 목록 삭제, 접근 권한 없는 계정")
    fun deleteAll_notWriter() {
        //Given
        val writers = dummy.createMembers(2)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(10, 50)
        val posts = dummy.createPosts(writers, total)
        for (post in posts)
            em.persist(post)
        val writerId = writers[Random().nextInt(writers.size)].id!!
        val ids = posts.filter { it.writer.id != writerId }.map { it.id!! }
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { postService.deleteAll(ids, writerId) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_POST)
                .hasMessage("Member do not have access.")
    }
    
    @Test
    @DisplayName("게시글 페이징 목록 조회, 검색 조건 없음")
    fun findAllPage() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total).forEach {
            Thread.sleep(1)
            em.persist(it)
        }
        val param = SearchParam(null, "")
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postService.findAllPage(param, pageRequest)
        clear()
        
        //Then
        val posts = postRepository.findAll().sortedByDescending { it.createdDate }.take(10).map { PostResponseDto(it) }
        
        assertThat(findPosts.content.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val postDto = posts[i]
            val findPostDto = findPosts.content[i]
            
            assertThat(findPostDto.id).isEqualTo(postDto.id)
            assertThat(findPostDto.writer).isEqualTo(postDto.writer)
            assertThat(findPostDto.title).isEqualTo(postDto.title)
            assertThat(findPostDto.content).isEqualTo(postDto.content)
            assertThat(findPostDto.viewCount).isEqualTo(postDto.viewCount)
            assertThat(findPostDto.noticeYn).isEqualTo(postDto.noticeYn)
            assertThat(findPostDto.commentsCount).isEqualTo(postDto.commentsCount)
            assertThat(findPostDto.attachmentsCount).isEqualTo(postDto.attachmentsCount)
            assertThat(findPostDto.createdDate).isEqualTo(postDto.createdDate)
            assertThat(findPostDto.lastModifiedDate).isEqualTo(postDto.lastModifiedDate)
        }
    }
    
    @Test
    @DisplayName("게시글 페이징 목록 조회, 검색 조건 키워드('키워드')")
    fun findAllPage_OneWord() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total).forEach {
            Thread.sleep(1)
            em.persist(it)
        }
        val keyword = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val param = SearchParam("titleOrContent", keyword)
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postService.findAllPage(param, pageRequest)
        clear()
        
        //Then
        val posts = postRepository.findAll().filter { it.title.contains(keyword) || it.content!!.contains(keyword) }
                .sortedByDescending { it.createdDate }.take(10).map { PostResponseDto(it) }
        
        assertThat(findPosts.content.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val postDto = posts[i]
            val findPostDto = findPosts.content[i]
            
            assertThat(findPostDto.id).isEqualTo(postDto.id)
            assertThat(findPostDto.writer).isEqualTo(postDto.writer)
            assertThat(findPostDto.title).isEqualTo(postDto.title)
            assertThat(findPostDto.content).isEqualTo(postDto.content)
            assertThat(findPostDto.viewCount).isEqualTo(postDto.viewCount)
            assertThat(findPostDto.noticeYn).isEqualTo(postDto.noticeYn)
            assertThat(findPostDto.commentsCount).isEqualTo(postDto.commentsCount)
            assertThat(findPostDto.attachmentsCount).isEqualTo(postDto.attachmentsCount)
            assertThat(findPostDto.createdDate).isEqualTo(postDto.createdDate)
            assertThat(findPostDto.lastModifiedDate).isEqualTo(postDto.lastModifiedDate)
        }
    }
    
    @Test
    @DisplayName("게시글 페이징 목록 조회, 검색 조건 키워드('키워드1 키워드2')")
    fun findAllPage_MultiWords() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total).forEach {
            Thread.sleep(1)
            em.persist(it)
        }
        val keyword1 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val keyword2 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val param = SearchParam("content", "$keyword1 $keyword2")
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postService.findAllPage(param, pageRequest)
        clear()
        
        //Then
        val posts = postRepository.findAll()
                .filter { it.content!!.contains(keyword1) || it.content!!.contains(keyword2) }
                .sortedByDescending { it.createdDate }.take(10).map { PostResponseDto(it) }
        
        assertThat(findPosts.content.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val postDto = posts[i]
            val findPostDto = findPosts.content[i]
            
            assertThat(findPostDto.id).isEqualTo(postDto.id)
            assertThat(findPostDto.writer).isEqualTo(postDto.writer)
            assertThat(findPostDto.title).isEqualTo(postDto.title)
            assertThat(findPostDto.content).isEqualTo(postDto.content)
            assertThat(findPostDto.viewCount).isEqualTo(postDto.viewCount)
            assertThat(findPostDto.noticeYn).isEqualTo(postDto.noticeYn)
            assertThat(findPostDto.commentsCount).isEqualTo(postDto.commentsCount)
            assertThat(findPostDto.attachmentsCount).isEqualTo(postDto.attachmentsCount)
            assertThat(findPostDto.createdDate).isEqualTo(postDto.createdDate)
            assertThat(findPostDto.lastModifiedDate).isEqualTo(postDto.lastModifiedDate)
        }
    }
    
    @Test
    @DisplayName("작성자 기준 게시글 페이징 목록 조회")
    fun findAllPageByWriter() {
        //Given
        val writers = dummy.createMembers(20)
        for (writer in writers)
            em.persist(writer)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total).forEach {
            Thread.sleep(1)
            em.persist(it)
        }
        val writerId = writers[Random().nextInt(writers.size)].id!!
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postService.findAllPageByWriter(writerId, pageRequest)
        clear()
        
        //Then
        val posts = postRepository.findAll().filter { it.writer.id == writerId }.sortedByDescending { it.createdDate }
                .take(10).map { PostResponseDto(it) }
        
        assertThat(findPosts.content.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val postDto = posts[i]
            val findPostDto = findPosts.content[i]
            
            assertThat(findPostDto.id).isEqualTo(postDto.id)
            assertThat(findPostDto.writer).isEqualTo(postDto.writer)
            assertThat(findPostDto.title).isEqualTo(postDto.title)
            assertThat(findPostDto.content).isEqualTo(postDto.content)
            assertThat(findPostDto.viewCount).isEqualTo(postDto.viewCount)
            assertThat(findPostDto.noticeYn).isEqualTo(postDto.noticeYn)
            assertThat(findPostDto.commentsCount).isEqualTo(postDto.commentsCount)
            assertThat(findPostDto.attachmentsCount).isEqualTo(postDto.attachmentsCount)
            assertThat(findPostDto.createdDate).isEqualTo(postDto.createdDate)
            assertThat(findPostDto.lastModifiedDate).isEqualTo(postDto.lastModifiedDate)
        }
    }
    
}