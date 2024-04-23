package com.project.alfa.services

import com.project.alfa.entities.Comment
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.error.exception.InvalidValueException
import com.project.alfa.repositories.CommentRepository
import com.project.alfa.repositories.MemberRepository
import com.project.alfa.repositories.PostRepository
import com.project.alfa.services.dto.CommentRequestDto
import com.project.alfa.services.dto.CommentResponseDto
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommentService(
        private val commentRepository: CommentRepository,
        private val memberRepository: MemberRepository,
        private val postRepository: PostRepository
) {
    
    /**
     * 댓글 작성
     *
     * @param dto - 댓글 작성 정보 DTO
     * @return PK
     */
    @Transactional
    fun create(dto: CommentRequestDto): Long {
        if (!validateMemberExist(dto.writerId!!))
            throw EntityNotFoundException("Could not found 'Member' by id: ${dto.writerId}")
        
        if (!validatePostExist(dto.postId!!))
            throw EntityNotFoundException("Could not found 'Post' by id: ${dto.postId}")
        
        val comment = Comment(writerId = dto.writerId!!, postId = dto.postId!!, content = dto.content)
        commentRepository.save(comment)
        
        return comment.id!!
    }
    
    /**
     * 댓글 정보 조회
     *
     * @param id - PK
     * @return 댓글 정보 DTO
     */
    fun read(id: Long): CommentResponseDto = CommentResponseDto(commentRepository.findById(id, false).orElseThrow {
        EntityNotFoundException("Could not found 'Comment' by id: $id")
    })
    
    /**
     * 댓글 정보 수정
     *
     * @param dto - 댓글 수정 정보 DTO
     */
    @Transactional
    fun update(dto: CommentRequestDto) {
        //수정 권한 검증
        validateCommentExist(dto.writerId!!, dto.id!!)
        
        val comment = commentRepository.findById(dto.id!!, false).orElseThrow {
            EntityNotFoundException("Could not found 'Comment' by id: ${dto.id}")
        }
        
        val param = Comment(
                id = dto.id,
                writerId = dto.writerId!!,
                postId = dto.postId!!,
                content = if (comment.content != dto.content) dto.content else ""   //내용 변경
        )
        commentRepository.update(param)
    }
    
    /**
     * 댓글 삭제
     *
     * @param id       - PK
     * @param writerId - 작성자 FK
     */
    @Transactional
    fun delete(id: Long, writerId: Long) {
        //삭제 권한 검증
        validateCommentExist(writerId, id)
        
        commentRepository.deleteById(id, writerId)
    }
    
    /**
     * 댓글 목록 삭제
     *
     * @param ids      - PK 목록
     * @param writerId - 작성자 FK
     */
    @Transactional
    fun deleteAll(ids: List<Long>, writerId: Long) {
        //삭제 권한 검증
        validateCommentsExist(writerId, ids)
        
        commentRepository.deleteAllByIds(ids, writerId)
    }
    
    /**
     * 게시글 기준 댓글 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param pageable - 페이징 객체
     * @return 댓글 페이징 목록
     */
    fun findAllPageByPost(postId: Long, pageable: Pageable): List<CommentResponseDto> = commentRepository
            .findAllByPost(postId, false, pageable).map { CommentResponseDto(it) }
    
    /**
     * 작성자 기준 댓글 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 댓글 페이징 목록
     */
    fun findAllPageByWriter(writerId: Long, pageable: Pageable): List<CommentResponseDto> = commentRepository
            .findAllByWriter(writerId, false, pageable).map { CommentResponseDto(it) }
    
    //==================== 검증 메서드 ====================//
    
    /**
     * 작성자 FK로 계정 엔티티 존재 검증
     *
     * @param writerId - 작성자 FK
     * @return 존재 여부
     */
    private fun validateMemberExist(writerId: Long): Boolean = memberRepository.existsById(writerId, false)
    
    /**
     * 게시글 FK로 게시글 엔티티 존재 검증
     *
     * @param postId - 게시글 FK
     * @return 존재 여부
     */
    private fun validatePostExist(postId: Long): Boolean = postRepository.existsById(postId, false)
    
    /**
     * 작성자 FK, 게시글 FK, 댓글 PK로 댓글 엔티티 존재 검증
     * 작성자 FK -> 계정 엔티티 존재 여부 및 댓글 PK의 작성자인지 확인
     * 댓글 PK -> 댓글 엔티티 조회
     * 댓글의 수정 또는 삭제시 사용
     *
     * @param writerId  - 작성자 FK
     * @param commentId - 댓글 PK
     */
    private fun validateCommentExist(writerId: Long, commentId: Long) {
        if (!validateMemberExist(writerId))
            throw EntityNotFoundException("Could not found 'Member' by id: $writerId")
        
        val comment = commentRepository.findById(commentId, false).orElseThrow {
            EntityNotFoundException("Could not found 'Comment' by id: $commentId")
        }
        
        if (comment.writerId != writerId)
            throw InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_COMMENT)
    }
    
    /**
     * 작성자 FK, 댓글 PK 목록으로 댓글 엔티티 존재 검증
     * 댓글 목록 삭제시 사용
     *
     * @param writerId   - 작성자 FK
     * @param commentIds - PK 목록
     */
    private fun validateCommentsExist(writerId: Long, commentIds: List<Long>) {
        if (!validateMemberExist(writerId))
            throw EntityNotFoundException("Could not found 'Member' by id: $writerId")
        
        val isAccess = commentRepository.findAllByWriter(writerId, false).map { it.id }.containsAll(commentIds)
        
        if (!isAccess)
            throw InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_COMMENT)
    }
    
}