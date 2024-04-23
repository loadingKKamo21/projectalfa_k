package com.project.alfa.repositories.v2

import com.project.alfa.entities.Post
import com.project.alfa.repositories.dto.SearchParam
import com.project.alfa.repositories.v2.specification.PostSpecification.Companion.searchAndSortSpecification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PostRepositoryV2(private val postJpaRepository: PostJpaRepository) {
    
    /**
     * 게시글 저장
     *
     * @param post - 게시글 정보
     * @return 게시글 정보
     */
    fun save(post: Post): Post = postJpaRepository.save(post)
    
    /**
     * 게시글 정보 조회
     *
     * @param id - PK
     * @return 게시글 정보
     */
    fun findById(id: Long): Optional<Post> = postJpaRepository.findById(id)
    
    /**
     * 게시글 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보
     */
    fun findById(id: Long, deleteYn: Boolean): Optional<Post> = postJpaRepository.findByIdAndDeleteYn(id, deleteYn)
    
    /**
     * 게시글 정보 목록 조회
     *
     * @return 게시글 정보 목록
     */
    fun findAll(): List<Post> = postJpaRepository.findAll()
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    fun findAll(deleteYn: Boolean): List<Post> = postJpaRepository.findAllByDeleteYn(deleteYn)
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 게시글 정보 목록
     */
    fun findAll(ids: List<Long>): List<Post> = postJpaRepository.findAllByIdIn(ids)
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    fun findAll(ids: List<Long>, deleteYn: Boolean): List<Post> =
            postJpaRepository.findAllByIdInAndDeleteYn(ids, deleteYn)
    
    /**
     * 작성자 기준 게시글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @return 게시글 정보 목록
     */
    fun findAll(writerId: Long): List<Post> = postJpaRepository.findAllByWriter_Id(writerId)
    
    /**
     * 작성자 기준 게시글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    fun findAll(writerId: Long, deleteYn: Boolean): List<Post> =
            postJpaRepository.findAllByWriter_IdAndDeleteYn(writerId, deleteYn)
    
    /**
     * 게시글 정보 페이징 목록 조회
     *
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    fun findAll(pageable: Pageable): Page<Post> = postJpaRepository.findAllByOrderByCreatedDateDesc(pageable)
    
    /**
     * 게시글 정보 페이징 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    fun findAll(deleteYn: Boolean, pageable: Pageable): Page<Post> =
            postJpaRepository.findAllByDeleteYnOrderByCreatedDateDesc(deleteYn, pageable)
    
    /**
     * 작성자 기준 게시글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    fun findAll(writerId: Long, pageable: Pageable): Page<Post> =
            postJpaRepository.findAllByWriter_IdOrderByCreatedDateDesc(writerId, pageable)
    
    /**
     * 작성자 기준 게시글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    fun findAll(writerId: Long, deleteYn: Boolean, pageable: Pageable): Page<Post> =
            postJpaRepository.findAllByWriter_IdAndDeleteYnOrderByCreatedDateDesc(writerId, deleteYn, pageable)
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    fun findAll(param: SearchParam, pageable: Pageable): Page<Post> =
            postJpaRepository.findAll(searchAndSortSpecification(param, pageable), pageable)
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    fun findAll(param: SearchParam, deleteYn: Boolean, pageable: Pageable): Page<Post> =
            postJpaRepository.findAll(searchAndSortSpecification(param, deleteYn, pageable), pageable)
    
    /**
     * 게시글 정보 영구 삭제
     *
     * @param post - 게시글 정보
     */
    fun delete(post: Post) = postJpaRepository.delete(post)
    
    /**
     * 게시글 정보 영구 삭제
     *
     * @param id - PK
     */
    fun deleteById(id: Long) = postJpaRepository.deleteById(id)
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param posts - 게시글 정보 목록
     */
    fun deleteAll(posts: List<Post>) = postJpaRepository.deleteAll(posts)
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param posts - 게시글 정보 목록
     */
    fun deleteAllInBatch(posts: List<Post>) = postJpaRepository.deleteAllInBatch(posts)
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    fun deleteAllById(ids: List<Long>) = postJpaRepository.deleteAllById(ids)
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    fun deleteAllByIdInBatch(ids: List<Long>) = postJpaRepository.deleteAllByIdInBatch(ids)
    
}