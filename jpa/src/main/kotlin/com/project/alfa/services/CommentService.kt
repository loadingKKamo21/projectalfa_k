package com.project.alfa.services

import com.project.alfa.entities.Comment
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.error.exception.InvalidValueException
import com.project.alfa.repositories.v1.CommentRepositoryV1
import com.project.alfa.repositories.v1.MemberRepositoryV1
import com.project.alfa.repositories.v1.PostRepositoryV1
import com.project.alfa.services.dto.CommentRequestDto
import com.project.alfa.services.dto.CommentResponseDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommentService(
        private val commentRepository: CommentRepositoryV1,
        //private val commentRepository: CommentRepositoryV2,
        //private val commentRepository: CommentRepositoryV3,
        private val memberRepository: MemberRepositoryV1,
        //private val memberRepository: MemberRepositoryV2,
        //private val memberRepository: MemberRepositoryV3,
        private val postRepository: PostRepositoryV1,
        //private val postRepository: PostRepositoryV2,
        //private val postRepository: PostRepositoryV3
) {
    
    /**
     * 댓글 작성
     *
     * @param dto - 댓글 작성 정보 DTO
     * @return PK
     */
    @Transactional
    fun create(dto: CommentRequestDto): Long {
        val member = memberRepository.findById(dto.writerId!!, false)
                .orElseThrow { EntityNotFoundException("Could not found 'Member' by id: ${dto.writerId}") }
        val post = postRepository.findById(dto.postId!!, false)
                .orElseThrow { EntityNotFoundException("Could not found 'Post' by id: ${dto.postId}") }
        
        val comment = Comment(writer = member, post = post, content = dto.content)
        
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
        val comment = commentRepository.findById(dto.id!!, false).orElseThrow {
            EntityNotFoundException("Could not found 'Comment' by id: ${dto.id}")
        }
        
        //수정 권한 검증
        if (comment.writer.id != dto.writerId || comment.writer.deleteYn)
            throw InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_COMMENT)
        
        //내용 변경
        if (comment.content != dto.content)
            comment.updateContent(dto.content)
    }
    
    /**
     * 댓글 삭제
     *
     * @param id       - PK
     * @param writerId - 작성자 FK
     */
    @Transactional
    fun delete(id: Long, writerId: Long) {
        val comment = commentRepository.findById(id, false).orElseThrow {
            EntityNotFoundException("Could not found 'Comment' by id: $id")
        }
        
        //삭제 권한 검증
        if (comment.writer.id != writerId || comment.writer.deleteYn)
            throw InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_COMMENT)
        
        comment.isDelete(true)
    }
    
    /**
     * 댓글 목록 삭제
     *
     * @param ids      - PK 목록
     * @param writerId - 작성자 FK
     */
    @Transactional
    fun deleteAll(ids: List<Long>, writerId: Long) {
        val comments = commentRepository.findAll(ids, false)
        
        //삭제 권한 검증
        if (comments.any { it.writer.id != writerId || it.writer.deleteYn })
            throw InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_COMMENT)
        
        comments.forEach { it.isDelete(true) }
    }
    
    /**
     * 게시글 기준 댓글 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param pageable - 페이징 객체
     * @return 댓글 페이징 목록
     */
    fun findAllPageByPost(postId: Long, pageable: Pageable): Page<CommentResponseDto> =
            commentRepository.findAllByPost(postId, false, pageable).map { CommentResponseDto(it) }
    
    /**
     * 작성자 기준 댓글 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 댓글 페이징 목록
     */
    fun findAllPageByWriter(writerId: Long, pageable: Pageable): Page<CommentResponseDto> =
            commentRepository.findAllByWriter(writerId, false, pageable).map { CommentResponseDto(it) }
    
}