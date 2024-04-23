package com.project.alfa.repositories.v2

import com.project.alfa.entities.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CommentRepositoryV2(private val commentJpaRepository: CommentJpaRepository) {
    
    /**
     * 댓글 저장
     *
     * @param comment - 댓글 정보
     * @return 댓글 정보
     */
    fun save(comment: Comment): Comment = commentJpaRepository.save(comment)
    
    /**
     * 댓글 정보 조회
     *
     * @param id - PK
     * @return 댓글 정보
     */
    fun findById(id: Long): Optional<Comment> = commentJpaRepository.findById(id)
    
    /**
     * 댓글 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보
     */
    fun findById(id: Long, deleteYn: Boolean): Optional<Comment> = commentJpaRepository.findByIdAndDeleteYn(id,
                                                                                                            deleteYn)
    
    /**
     * 댓글 정보 목록 조회
     *
     * @return 댓글 정보 목록
     */
    fun findAll(): List<Comment> = commentJpaRepository.findAll()
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    fun findAll(deleteYn: Boolean): List<Comment> = commentJpaRepository.findAllByDeleteYn(deleteYn)
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 댓글 정보 목록
     */
    fun findAll(ids: List<Long>): List<Comment> = commentJpaRepository.findAllByIdIn(ids)
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    fun findAll(ids: List<Long>, deleteYn: Boolean): List<Comment> = commentJpaRepository.findAllByIdInAndDeleteYn(ids,
                                                                                                                   deleteYn)
    
    /**
     * 작성자 기준 댓글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @return 댓글 정보 목록
     */
    fun findAllByWriter(writerId: Long): List<Comment> = commentJpaRepository.findAllByWriter_Id(writerId)
    
    /**
     * 작성자 기준 댓글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    fun findAllByWriter(writerId: Long, deleteYn: Boolean): List<Comment> =
            commentJpaRepository.findAllByWriter_IdAndDeleteYn(writerId, deleteYn)
    
    /**
     * 게시글 기준 댓글 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 댓글 정보 목록
     */
    fun findAllByPost(postId: Long): List<Comment> = commentJpaRepository.findAllByPost_Id(postId)
    
    /**
     * 게시글 기준 댓글 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    fun findAllByPost(postId: Long, deleteYn: Boolean): List<Comment> =
            commentJpaRepository.findAllByPost_IdAndDeleteYn(postId, deleteYn)
    
    /**
     * 작성자 기준 댓글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 댓글 정보 페이징 목록
     */
    fun findAllByWriter(writerId: Long, pageable: Pageable): Page<Comment> =
            commentJpaRepository.findAllByWriter_IdOrderByCreatedDateDesc(writerId, pageable)
    
    /**
     * 작성자 기준 댓글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 댓글 정보 페이징 목록
     */
    fun findAllByWriter(writerId: Long, deleteYn: Boolean, pageable: Pageable): Page<Comment> =
            commentJpaRepository.findAllByWriter_IdAndDeleteYnOrderByCreatedDateDesc(writerId,
                                                                                     deleteYn,
                                                                                     pageable)
    
    /**
     * 게시글 기준 댓글 정보 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param pageable - 페이징 목록
     * @return 댓글 정보 페이징 목록
     */
    fun findAllByPost(postId: Long, pageable: Pageable): Page<Comment> =
            commentJpaRepository.findAllByPost_IdOrderByCreatedDateDesc(postId, pageable)
    
    /**
     * 게시글 기준 댓글 정보 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 목록
     * @return 댓글 정보 페이징 목록
     */
    fun findAllByPost(postId: Long, deleteYn: Boolean, pageable: Pageable): Page<Comment> =
            commentJpaRepository.findAllByPost_IdAndDeleteYnOrderByCreatedDateDesc(postId, deleteYn, pageable)
    
    /**
     * 댓글 정보 영구 삭제
     *
     * @param comment - 댓글 정보
     */
    fun delete(comment: Comment) = commentJpaRepository.delete(comment)
    
    /**
     * 댓글 정보 영구 삭제
     *
     * @param id - PK
     */
    fun deleteById(id: Long) = commentJpaRepository.deleteById(id)
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param comments - 댓글 정보 목록
     */
    fun deleteAll(comments: List<Comment>) = commentJpaRepository.deleteAll(comments)
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param comments - 댓글 정보 목록
     */
    fun deleteAllInBatch(comments: List<Comment>) = commentJpaRepository.deleteAllInBatch(comments)
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    fun deleteAllById(ids: List<Long>) = commentJpaRepository.deleteAllById(ids)
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    fun deleteAllByIdInBatch(ids: List<Long>) = commentJpaRepository.deleteAllByIdInBatch(ids)
    
}