package com.project.alfa.services

import com.project.alfa.entities.Post
import com.project.alfa.entities.Role
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.error.exception.InvalidValueException
import com.project.alfa.repositories.MemberRepository
import com.project.alfa.repositories.PostRepository
import com.project.alfa.repositories.dto.SearchParam
import com.project.alfa.services.dto.PostRequestDto
import com.project.alfa.services.dto.PostResponseDto
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PostService(
        private val postRepository: PostRepository,
        private val memberRepository: MemberRepository,
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
        if (!validateMemberExist(dto.writerId!!))
            throw EntityNotFoundException("Could not found 'Member' by id: ${dto.writerId}")
        var flag: Boolean = false
        
        //공지 여부 적용 확인
        if (dto.noticeYn) {
            if (memberRepository.findById(dto.writerId!!, false).orElseThrow {
                        EntityNotFoundException("Could not found 'Member' by id: ${dto.writerId}")
                    }.role == Role.ADMIN)
                flag = true
            else
                throw InvalidValueException("Member do not have access.", ErrorCode.HANDLE_ACCESS_DENIED)
        }
        
        val post = Post(writerId = dto.writerId!!,
                        title = dto.title,
                        content = dto.content,
                        noticeYn = dto.noticeYn && flag)
        postRepository.save(post)
        
        return post.id!!
    }
    
    /**
     * 게시글 정보 조회
     *
     * @param id - PK
     * @return 게시글 정보 DTO
     */
    fun read(id: Long): PostResponseDto = PostResponseDto(postRepository.findById(id, false).orElseThrow {
        EntityNotFoundException("Could not found 'Post' by id: $id")
    })
    
    /**
     * 게시글 정보 조회(@CachePut)
     *
     * @param id        - PK
     * @param sessionId - 세션 ID
     * @param ipAddress - IP 주소
     * @return 게시글 정보 DTO
     */
//    @CachePut(value = ["postCache"],
//              unless = "#id == null || #sessionId == null || #ipAddress == null",
//              key = "{#id, #sessionId, #ipAddress}")
    @CachePut(value = ["postCache"],
              unless = "#id == null || #sessionId == null || #ipAddress == null",
              keyGenerator = "customKeyGenerator")
    fun readWithCaching(id: Long, sessionId: String, ipAddress: String): PostResponseDto = PostResponseDto(
            postRepository.findById(id, false).orElseThrow {
                EntityNotFoundException("Could not found 'Post' by id: $id")
            })
    
    /**
     * 조회수 증가
     *
     * @param id - PK
     */
    @Transactional
    fun addViewCount(id: Long) {
        if (!postRepository.existsById(id, false))
            throw EntityNotFoundException("Could not found 'Post' by id: $id")
        postRepository.addViewCount(id)
    }
    
    /**
     * 조회수 증가(@Cacheable)
     *
     * @param id        - PK
     * @param sessionId - 세션 ID
     * @param ipAddress - IP 주소
     */
//    @Cacheable(value = ["postCache"],
//               unless = "#id == null || #sessionId == null || #ipAddress == null",
//               key = "{#id, #sessionId, #ipAddress}")
    @Cacheable(value = ["postCache"],
               unless = "#id == null || #sessionId == null || #ipAddress == null",
               keyGenerator = "customKeyGenerator")
    @Transactional
    fun addViewCountWithCaching(id: Long, sessionId: String, ipAddress: String) {
        if (!postRepository.existsById(id, false))
            throw EntityNotFoundException("Could not found 'Post' by id: $id")
        
        if (!isPostCached(id, sessionId, ipAddress))
            postRepository.addViewCount(id)
    }
    
    /**
     * 게시글 정보 수정
     *
     * @param dto - 게시글 수정 정보 DTO
     */
    @Transactional
    fun update(dto: PostRequestDto) {
        //수정 권한 검증
        validatePostExist(dto.writerId!!, dto.id!!)
        
        val post = postRepository.findById(dto.id!!, false).orElseThrow {
            EntityNotFoundException("Could not found 'Post' by id: ${dto.id}")
        }
        
        val param = Post(
                id = dto.id,
                writerId = dto.writerId!!,
                title = if (post.title != dto.title) dto.title else "", //제목 변경
                content = if (post.content != dto.content) dto.content else "", //내용 변경
                noticeYn = if (post.noticeYn != dto.noticeYn)   //공지 여부 변경
                    (if (memberRepository.findById(dto.writerId!!, false).orElseThrow {
                                EntityNotFoundException("Could not found 'Member' by id: ${dto.writerId}")
                            }.role == Role.ADMIN)
                        true else throw InvalidValueException("Member do not have access.",
                                                              ErrorCode.HANDLE_ACCESS_DENIED))
                else false
        )
        postRepository.update(param)
    }
    
    /**
     * 게시글 삭제
     *
     * @param id       - PK
     * @param writerId - 작성자 FK
     */
    @Transactional
    fun delete(id: Long, writerId: Long) {
        //삭제 권한 검증
        validatePostExist(writerId, id)
        
        postRepository.deleteById(id, writerId)
    }
    
    /**
     * 게시글 목록 삭제
     *
     * @param ids      - PK 목록
     * @param writerId - 작성자 FK
     */
    @Transactional
    fun deleteAll(ids: List<Long>, writerId: Long) {
        //삭제 권한 검증
        validatePostsExist(writerId, ids)
        
        postRepository.deleteAllByIds(ids, writerId)
    }
    
    /**
     * 게시글 페이징 목록 조회
     *
     * @param searchParam - 검색 조건, 키워드
     * @param pageable    - 페이징 객체
     * @return 게시글 페이징 목록
     */
    fun findAllPage(searchParam: SearchParam, pageable: Pageable): List<PostResponseDto> = postRepository
            .findAll(searchParam, false, pageable).map { PostResponseDto(it) }
    
    /**
     * 작성자 기준 게시글 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 게시글 페이징 목록
     */
    fun findAllPageByWriter(writerId: Long, pageable: Pageable): List<PostResponseDto> = postRepository
            .findAll(writerId, false, pageable).map { PostResponseDto(it) }
    
    //==================== 검증 메서드 ====================//
    
    /**
     * 작성자 FK로 계정 엔티티 존재 검증
     *
     * @param writerId - 작성자 FK
     * @return 존재 여부
     */
    private fun validateMemberExist(writerId: Long) = memberRepository.existsById(writerId, false)
    
    /**
     * 작성자 FK, 게시글 PK로 게시글 엔티티 존재 검증
     * 작성자 FK -> 계정 엔티티 존재 여부 및 게시글 PK의 작성자인지 확인
     * 게시글 PK -> 게시글 엔티티 조회
     * 게시글의 수정 또는 삭제시 사용
     *
     * @param writerId - 작성자 FK
     * @param postId   - PK
     */
    private fun validatePostExist(writerId: Long, postId: Long) {
        if (!validateMemberExist(writerId))
            throw EntityNotFoundException("Could not found 'Member' by id: $writerId")
        
        val post = postRepository.findById(postId, false).orElseThrow {
            EntityNotFoundException("Could not found 'Post' by id: $postId")
        }
        
        if (post.writerId != writerId)
            throw InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_POST)
    }
    
    /**
     * 작성자 FK, 게시글 PK 목록으로 게시글 엔티티 존재 검증
     * 게시글 목록 삭제시 사용
     *
     * @param writerId - 작성자 FK
     * @param postIds  - PK 목록
     */
    private fun validatePostsExist(writerId: Long, postIds: List<Long>) {
        if (!validateMemberExist(writerId))
            throw EntityNotFoundException("Could not found 'Member' by id: $writerId")
        
        val isAccess = postRepository.findAll(writerId, false).map { it.id }.containsAll(postIds)
        
        if (!isAccess)
            throw InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_POST)
    }
    
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
        return postCache.get("$id,$sessionId,$ipAddress") != null
    }
    
}