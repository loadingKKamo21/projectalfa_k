package com.project.alfa.services

import com.project.alfa.entities.Post
import com.project.alfa.entities.Role
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.error.exception.InvalidValueException
import com.project.alfa.repositories.dto.SearchParam
import com.project.alfa.repositories.v1.MemberRepositoryV1
import com.project.alfa.repositories.v1.PostRepositoryV1
import com.project.alfa.services.dto.PostRequestDto
import com.project.alfa.services.dto.PostResponseDto
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PostService(
        private val postRepository: PostRepositoryV1,
        //private val postRepository: PostRepositoryV2,
        //private val postRepository: PostRepositoryV3,
        private val memberRepository: MemberRepositoryV1,
        //private val memberRepository: MemberRepositoryV2,
        //private val memberRepository: MemberRepositoryV3,
        private val cacheManager: CacheManager
) {
    
    /**
     * 게시글 작성
     *
     * @param dto - 게시글 작성 정보 DTO
     * @return PK
     */
    @Transactional
    fun create(dto: PostRequestDto): Long {
        val member = memberRepository.findById(dto.writerId!!, false).orElseThrow {
            EntityNotFoundException("Could not found 'Member' by id: " + dto.writerId)
        }
        
        var noticeYn: Boolean;
        
        //공지 여부 적용 확인
        if (dto.noticeYn) {
            if (member.role == Role.ADMIN)
                noticeYn = true
            else
                throw InvalidValueException("Member do not have access.", ErrorCode.HANDLE_ACCESS_DENIED)
        } else
            noticeYn = false
        
        val post = Post(writer = member, title = dto.title, content = dto.content, noticeYn = noticeYn)
        
        postRepository.save(post)
        
        return post.id!!
    }
    
    /**
     * 게시글 정보 조회
     *
     * @param id - PK
     * @return 게시글 정보 DTO
     */
    fun read(id: Long): PostResponseDto = PostResponseDto(
            postRepository.findById(id, false)
                    .orElseThrow { EntityNotFoundException("Could not found 'Post' by id: $id") }
    )
    
    /**
     * 게시글 정보 조회(@CachePut)
     *
     * @param id        - PK
     * @param sessionId - 세션 ID
     * @param ipAddress - IP 주소
     * @return 게시글 정보 DTO
     */
//    @CachePut(value = "postCache",
//              unless = "#id == null || #sessionId == null || #ipAddress == null",
//              key = "{#id, #sessionId, #ipAddress}")
    @CachePut(value = ["postCache"], unless = "#id == null || #sessionId == null || #ipAddress == null",
              keyGenerator = "customKeyGenerator")
    fun readWithCaching(id: Long, sessionId: String, ipAddress: String): PostResponseDto = PostResponseDto(
            postRepository.findById(id, false)
                    .orElseThrow { EntityNotFoundException("Could not found 'Post' by id: $id") }
    )
    
    /**
     * 조회수 증가
     *
     * @param id - PK
     */
    @Transactional
    fun addViewCount(id: Long) {
        val post = postRepository.findById(id, false)
                .orElseThrow { EntityNotFoundException("Could not found 'Post' by id: $id") }
        post.addViewCount()
    }
    
    /**
     * 조회수 증가(@Cacheable)
     *
     * @param id        - PK
     * @param sessionId - 세션 ID
     * @param ipAddress - IP 주소
     */
//    @Cacheable(value = "postCache",
//               unless = "#id == null || #sessionId == null || #ipAddress == null",
//               key = "{#id, #sessionId, #ipAddress}")
    @Cacheable(value = ["postCache"], unless = "#id == null || #sessionId == null || #ipAddress == null",
               keyGenerator = "customKeyGenerator")
    @Transactional
    fun addViewCountWithCaching(id: Long, sessionId: String, ipAddress: String) {
        val post = postRepository.findById(id, false)
                .orElseThrow { EntityNotFoundException("Could not found 'Post' by id: $id") }
        if (!isPostCached(id, sessionId, ipAddress))
            post.addViewCount()
    }
    
    /**
     * 게시글 정보 수정
     *
     * @param dto - 게시글 수정 정보 DTO
     */
    @Transactional
    fun update(dto: PostRequestDto) {
        val post = postRepository.findById(dto.id!!, false)
                .orElseThrow { EntityNotFoundException("Could not found 'Post' by id: " + dto.id) }
        
        //수정 권한 검증
        if (post.writer.id != dto.writerId || post.writer.deleteYn)
            throw InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_POST)
        
        //제목 변경
        if (post.title != dto.title)
            post.updateTitle(dto.title)
        
        //내용 변경
        if (post.content != dto.content)
            post.updateContent(dto.content)
        
        //공지 여부 변경
        if (dto.noticeYn) {
            if (post.writer.role === Role.ADMIN)
                post.updateNoticeYn(dto.noticeYn)
            else
                throw InvalidValueException("Member do not have access.", ErrorCode.HANDLE_ACCESS_DENIED)
        } else if (post.noticeYn != dto.noticeYn)
            post.updateNoticeYn(dto.noticeYn)
    }
    
    /**
     * 게시글 삭제
     *
     * @param id       - PK
     * @param writerId - 작성자 FK
     */
    @Transactional
    fun delete(id: Long, writerId: Long) {
        val post = postRepository.findById(id, false)
                .orElseThrow { EntityNotFoundException("Could not found 'Post' by id: $id") }
        
        //삭제 권한 검증
        if (post.writer.id != writerId || post.writer.deleteYn)
            throw InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_POST)
        
        post.isDelete(true)
    }
    
    /**
     * 게시글 목록 삭제
     *
     * @param ids      - PK 목록
     * @param writerId - 작성자 FK
     */
    @Transactional
    fun deleteAll(ids: List<Long>, writerId: Long) {
        val posts = postRepository.findAll(ids, false)
        
        //삭제 권한 검증
        if (posts.any { it.writer.id != writerId || it.writer.deleteYn })
            throw InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_POST)
        
        posts.forEach { it.isDelete(true) }
    }
    
    /**
     * 게시글 페이징 목록 조회
     *
     * @param searchParam - 검색 조건, 키워드
     * @param pageable    - 페이징 객체
     * @return 게시글 페이징 목록
     */
    fun findAllPage(searchParam: SearchParam, pageable: Pageable): Page<PostResponseDto> {
        return postRepository.findAll(searchParam, false, pageable).map { PostResponseDto(it) }
    }
    
    /**
     * 작성자 기준 게시글 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 게시글 페이징 목록
     */
    fun findAllPageByWriter(writerId: Long, pageable: Pageable): Page<PostResponseDto> {
        return postRepository.findAll(writerId, false, pageable).map { PostResponseDto(it) }
    }
    
    //==================== 검증 메서드 ====================//
    
    /**
     * 게시글 정보 조회 캐싱 여부 확인
     *
     * @param id        - PK
     * @param sessionId - 세션 ID
     * @param ipAddress - IP 주소
     * @return 캐싱 여부
     */
    private fun isPostCached(id: Long, sessionId: String, ipAddress: String): Boolean {
        val postCache = cacheManager.getCache("postCache") ?: return false
        return postCache["$id,$sessionId,$ipAddress"] != null
    }
    
}