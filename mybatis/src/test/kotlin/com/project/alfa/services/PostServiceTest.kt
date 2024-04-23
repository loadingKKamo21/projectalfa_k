package com.project.alfa.services

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.config.redis.EmbeddedRedisConfig
import com.project.alfa.entities.AuthInfo
import com.project.alfa.entities.Member
import com.project.alfa.entities.Role
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.error.exception.InvalidValueException
import com.project.alfa.repositories.PostRepository
import com.project.alfa.repositories.dto.SearchParam
import com.project.alfa.repositories.mybatis.MemberMapper
import com.project.alfa.repositories.mybatis.PostMapper
import com.project.alfa.services.dto.PostRequestDto
import com.project.alfa.services.dto.PostResponseDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
import kotlin.random.Random

@Import(TestConfig::class, EmbeddedRedisConfig::class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class PostServiceTest {
    
    @Autowired
    lateinit var postService: PostService
    
    @Autowired
    lateinit var postRepository: PostRepository
    
    @Autowired
    lateinit var postMapper: PostMapper
    
    @Autowired
    lateinit var memberMapper: MemberMapper
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @Autowired
    lateinit var cacheManager: CacheManager
    
    @Test
    @DisplayName("게시글 작성")
    fun create() {
        //Given
        val writer = dummy.createMembers(1, true)[0]
        val dto = PostRequestDto(writerId = writer.id!!,
                                 title = dummy.generateRandomString(dummy.generateRandomNumber(1, 100)),
                                 content = dummy.generateRandomString(dummy.generateRandomNumber(100, 500)),
                                 noticeYn = false)
        
        //When
        val id = postService.create(dto)
        
        //Then
        val findPost = postMapper.findById(id)
        
        assertThat(dto.writerId).isEqualTo(findPost.writerId)
        assertThat(dto.title).isEqualTo(findPost.title)
        assertThat(dto.content).isEqualTo(findPost.content)
        assertThat(dto.noticeYn).isEqualTo(findPost.noticeYn)
    }
    
    @Test
    @DisplayName("게시글 작성, 존재하지 않는 계정")
    fun create_unknownWriter() {
        //Given
        val writerId = Random.nextLong()
        val dto = PostRequestDto(writerId = writerId,
                                 title = dummy.generateRandomString(dummy.generateRandomNumber(1, 100)),
                                 content = dummy.generateRandomString(dummy.generateRandomNumber(100, 500)),
                                 noticeYn = false)
        
        //When
        
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
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        
        //When
        val dto = postService.read(id)
        
        //Then
        val findPost = postMapper.findById(id)
        
        assertThat(findPost.id).isEqualTo(dto.id)
        assertThat(findPost.nickname).isEqualTo(dto.writer)
        assertThat(findPost.title).isEqualTo(dto.title)
        assertThat(findPost.content).isEqualTo(dto.content)
        assertThat(findPost.viewCount).isEqualTo(dto.viewCount)
        assertThat(findPost.noticeYn).isEqualTo(dto.noticeYn)
        assertThat(findPost.commentIds.size).isEqualTo(dto.commentCount)
        assertThat(findPost.createdDate).isEqualTo(dto.createdDate)
        assertThat(findPost.lastModifiedDate).isEqualTo(dto.lastModifiedDate)
    }
    
    @Test
    @DisplayName("PK로 게시글 상세 조회, 캐싱")
    fun read_caching() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        
        //When
        val dto = postService.readWithCaching(id, UUID.randomUUID().toString(), "127.0.0.1")
        
        //Then
        val findPost = postMapper.findById(id)
        val postCache = cacheManager.getCache("postCache")
        
        assertThat(findPost.id).isEqualTo(dto.id)
        assertThat(findPost.nickname).isEqualTo(dto.writer)
        assertThat(findPost.title).isEqualTo(dto.title)
        assertThat(findPost.content).isEqualTo(dto.content)
        assertThat(findPost.viewCount).isEqualTo(dto.viewCount)
        assertThat(findPost.noticeYn).isEqualTo(dto.noticeYn)
        assertThat(findPost.commentIds.size).isEqualTo(dto.commentCount)
        assertThat(findPost.createdDate).isEqualTo(dto.createdDate)
        assertThat(findPost.lastModifiedDate).isEqualTo(dto.lastModifiedDate)
        assertThat(postCache).isNotNull
    }
    
    @Test
    @DisplayName("PK로 게시글 상세 조회, 존재하지 않는 PK")
    fun read_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        
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
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        postMapper.deleteById(id, post.writerId)
        
        //When
        
        //Then
        assertThatThrownBy { postService.read(id) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: $id")
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
        postService.addViewCount(id)
        
        //Then
        val afterViewCount = postMapper.findById(id).viewCount
        
        assertThat(afterViewCount).isEqualTo(beforeViewCount + 1)
    }
    
    @Test
    @DisplayName("조회수 증가, 이미 삭제된 게시글")
    fun addViewCount_alreadyDeleted() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        postMapper.deleteById(id, post.writerId)
        
        //When
        
        //Then
        assertThatThrownBy { postService.read(id) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: $id")
    }
    
    @Test
    @DisplayName("조회수 증가, 캐싱")
    fun addViewCount_caching() {
        //Given
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        val beforeViewCount = postMapper.findById(id).viewCount
        
        //When
        postService.addViewCountWithCaching(id, UUID.randomUUID().toString(), "127.0.0.1")
        
        //Then
        val afterViewCount = postMapper.findById(id).viewCount
        val postCache = cacheManager.getCache("postCache")
        
        assertThat(afterViewCount).isEqualTo(beforeViewCount + 1)
        assertThat(postCache).isNotNull
    }
    
    @Test
    @DisplayName("게시글 정보 수정")
    fun update() {
        //Given
        val writers = dummy.createMembers(1, true)
        memberMapper.update(Member(id = writers[0].id,
                                   username = "",
                                   password = "",
                                   authInfo = AuthInfo(),
                                   nickname = "",
                                   role = Role.ADMIN))  //게시글 작성 시 공지 여부 설정을 위한 계정 '관리자' 권한 부여
        
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        val beforeTitle = post.title
        val beforeContent = post.content
        val beforeNoticeYn = post.noticeYn
        
        var afterTitle: String
        var afterContent: String
        do {
            afterTitle = dummy.generateRandomString(dummy.generateRandomNumber(1, 100))
            afterContent = dummy.generateRandomString(dummy.generateRandomNumber(100, 500))
        } while (beforeTitle == afterTitle || beforeContent == afterContent)
        
        val dto = PostRequestDto(id = id,
                                 writerId = post.writerId,
                                 title = afterTitle,
                                 content = afterContent,
                                 noticeYn = true)
        
        //When
        postService.update(dto)
        
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
    @DisplayName("게시글 정보 수정, 접근 권한 없는 계정")
    fun update_notWriter() {
        //Given
        val writers = dummy.createMembers(2, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        
        var anotherWriterId: Long = 0
        for (writer in writers)
            if (post.writerId != writer.id) {
                anotherWriterId = writer.id!!
                break
            }
        
        val dto = PostRequestDto(id = id,
                                 writerId = anotherWriterId,
                                 title = dummy.generateRandomString(dummy.generateRandomNumber(1, 100)),
                                 content = dummy.generateRandomString(dummy.generateRandomNumber(100, 500)),
                                 noticeYn = false)
        
        //When
        
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
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        postMapper.deleteById(id, post.writerId)
        val dto = PostRequestDto(id = id,
                                 writerId = post.writerId,
                                 title = dummy.generateRandomString(dummy.generateRandomNumber(1, 100)),
                                 content = dummy.generateRandomString(dummy.generateRandomNumber(100, 500)),
                                 noticeYn = false)
        
        //When
        
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
        val writers = dummy.createMembers(1, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        val writerId = post.writerId
        
        //When
        postService.delete(id, writerId)
        
        //Then
        val deletedPost = postMapper.findById(id)
        
        assertThat(deletedPost.deleteYn).isTrue
    }
    
    @Test
    @DisplayName("게시글 삭제, 접근 권한 없는 계정")
    fun delete_notWriter() {
        //Given
        val writers = dummy.createMembers(2, true)
        val post = dummy.createPosts(writers, 1, true)[0]
        val id = post.id!!
        
        var anotherWriterId: Long = 0
        for (writer in writers)
            if (post.writerId != writer.id) {
                anotherWriterId = writer.id!!
                break
            }
        
        //When
        
        //Then
        assertThatThrownBy { postService.delete(id, anotherWriterId) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_POST)
                .hasMessage("Member do not have access.")
    }
    
    @Test
    @DisplayName("게시글 목록 삭제")
    fun deleteAll() {
        //Given
        val writers = dummy.createMembers(1, true)
        val total = dummy.generateRandomNumber(10, 50)
        val posts = dummy.createPosts(writers, total, true)
        val writerId = writers[0].id!!
        val ids = posts.filter { it.writerId == writerId }.map { it.id!! }
        
        //When
        postService.deleteAll(ids, writerId)
        
        //Then
        val deletedPosts = postMapper.findAll().filter { ids.contains(it.id) && it.writerId == writerId && it.deleteYn }
        
        for (post in deletedPosts)
            assertThat(post.deleteYn).isTrue
    }
    
    @Test
    @DisplayName("게시글 목록 삭제, 접근 권한 없는 계정")
    fun deleteAll_notWriter() {
        //Given
        val writers = dummy.createMembers(2, true)
        val total = dummy.generateRandomNumber(10, 50)
        val posts = dummy.createPosts(writers, total, true)
        val writerId = writers[Random.nextInt(writers.size)].id!!
        val ids = posts.filter { it.writerId != writerId }.map { it.id!! }
        
        //When
        
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
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        val param = SearchParam(null, "")
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postService.findAllPage(param, pageRequest)
        
        //Then
        val posts = postMapper.findAll().sortedByDescending { it.createdDate }.take(10).map { PostResponseDto(it) }
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val postDto = posts[i]
            val findPostDto = findPosts[i]
            
            assertThat(findPostDto.id).isEqualTo(postDto.id)
            assertThat(findPostDto.writer).isEqualTo(postDto.writer)
            assertThat(findPostDto.title).isEqualTo(postDto.title)
            assertThat(findPostDto.content).isEqualTo(postDto.content)
            assertThat(findPostDto.viewCount).isEqualTo(postDto.viewCount)
            assertThat(findPostDto.noticeYn).isEqualTo(postDto.noticeYn)
            assertThat(findPostDto.commentCount).isEqualTo(postDto.commentCount)
            assertThat(findPostDto.createdDate).isEqualTo(postDto.createdDate)
            assertThat(findPostDto.lastModifiedDate).isEqualTo(postDto.lastModifiedDate)
        }
    }
    
    @Test
    @DisplayName("게시글 페이징 목록 조회, 검색 조건 키워드('키워드')")
    fun findAllPage_OneWord() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        val keyword = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val param = SearchParam("titleOrContent", keyword)
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postService.findAllPage(param, pageRequest)
        
        //Then
        val posts = postMapper.findAll()
                .filter { it.title.contains(keyword) || it.content!!.contains(keyword) }
                .sortedByDescending { it.createdDate }
                .take(10)
                .map { PostResponseDto(it) }
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val postDto = posts[i]
            val findPostDto = findPosts[i]
            
            assertThat(findPostDto.id).isEqualTo(postDto.id)
            assertThat(findPostDto.writer).isEqualTo(postDto.writer)
            assertThat(findPostDto.title).isEqualTo(postDto.title)
            assertThat(findPostDto.content).isEqualTo(postDto.content)
            assertThat(findPostDto.title.contains(keyword) || findPostDto.content!!.contains(keyword)).isTrue
            assertThat(findPostDto.viewCount).isEqualTo(postDto.viewCount)
            assertThat(findPostDto.noticeYn).isEqualTo(postDto.noticeYn)
            assertThat(findPostDto.commentCount).isEqualTo(postDto.commentCount)
            assertThat(findPostDto.createdDate).isEqualTo(postDto.createdDate)
            assertThat(findPostDto.lastModifiedDate).isEqualTo(postDto.lastModifiedDate)
        }
    }
    
    @Test
    @DisplayName("게시글 페이징 목록 조회, 검색 조건 키워드('키워드1 키워드2')")
    fun findAllPage_MultiWord() {
        //Given
        val writers = dummy.createMembers(20, true)
        val total = dummy.generateRandomNumber(100, 300)
        dummy.createPosts(writers, total, true)
        val keyword1 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val keyword2 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5))
        val param = SearchParam("content", "$keyword1 $keyword2")
        val pageRequest = PageRequest.of(0, 10)
        
        //When
        val findPosts = postService.findAllPage(param, pageRequest)
        
        //Then
        val posts = postMapper.findAll()
                .filter { it.content!!.contains(keyword1) || it.content!!.contains(keyword2) }
                .sortedByDescending { it.createdDate }
                .take(10)
                .map { PostResponseDto(it) }
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val postDto = posts[i]
            val findPostDto = findPosts[i]
            
            assertThat(findPostDto.id).isEqualTo(postDto.id)
            assertThat(findPostDto.writer).isEqualTo(postDto.writer)
            assertThat(findPostDto.title).isEqualTo(postDto.title)
            assertThat(findPostDto.content).isEqualTo(postDto.content)
            assertThat(findPostDto.content!!.contains(keyword1) || findPostDto.content!!.contains(keyword2)).isTrue
            assertThat(findPostDto.viewCount).isEqualTo(postDto.viewCount)
            assertThat(findPostDto.noticeYn).isEqualTo(postDto.noticeYn)
            assertThat(findPostDto.commentCount).isEqualTo(postDto.commentCount)
            assertThat(findPostDto.createdDate).isEqualTo(postDto.createdDate)
            assertThat(findPostDto.lastModifiedDate).isEqualTo(postDto.lastModifiedDate)
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
        val findPosts = postService.findAllPageByWriter(writerId, pageRequest)
        
        //Then
        val posts = postMapper.findAll()
                .filter { it.writerId == writerId }
                .sortedByDescending { it.createdDate }
                .take(10)
                .map { PostResponseDto(it) }
        
        assertThat(findPosts.size).isEqualTo(posts.size)
        for (i in posts.indices) {
            val postDto = posts[i]
            val findPostDto = findPosts[i]
            
            assertThat(findPostDto.id).isEqualTo(postDto.id)
            assertThat(findPostDto.writer).isEqualTo(postDto.writer)
            assertThat(findPostDto.title).isEqualTo(postDto.title)
            assertThat(findPostDto.content).isEqualTo(postDto.content)
            assertThat(findPostDto.viewCount).isEqualTo(postDto.viewCount)
            assertThat(findPostDto.noticeYn).isEqualTo(postDto.noticeYn)
            assertThat(findPostDto.commentCount).isEqualTo(postDto.commentCount)
            assertThat(findPostDto.createdDate).isEqualTo(postDto.createdDate)
            assertThat(findPostDto.lastModifiedDate).isEqualTo(postDto.lastModifiedDate)
        }
    }
    
}