package com.project.alfa.repositories.mybatis

import com.github.pagehelper.PageHelper
import com.project.alfa.entities.Comment
import com.project.alfa.repositories.CommentRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CommentRepositoryImpl(private val commentMapper: CommentMapper) : CommentRepository {
    
    /**
     * 댓글 저장
     *
     * @param comment - 댓글 정보
     * @return 댓글 정보
     */
    override fun save(comment: Comment): Comment {
        commentMapper.save(comment)
        return comment
    }
    
    /**
     * 댓글 정보 조회
     *
     * @param id - PK
     * @return 댓글 정보
     */
    override fun findById(id: Long): Optional<Comment> = Optional.ofNullable(commentMapper.findById(id))
    
    /**
     * 댓글 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보
     */
    override fun findById(id: Long, deleteYn: Boolean): Optional<Comment> = Optional.ofNullable(
            commentMapper.findByIdAndDeleteYn(id, deleteYn))
    
    /**
     * 댓글 정보 목록 조회
     *
     * @return 댓글 정보 목록
     */
    override fun findAll(): List<Comment> = commentMapper.findAll()
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    override fun findAll(deleteYn: Boolean): List<Comment> = commentMapper.findAllByDeleteYn(deleteYn)
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 댓글 정보 목록
     */
    override fun findAll(ids: List<Long>): List<Comment> {
        if (ids.isEmpty())
            return emptyList()
        return commentMapper.findAllByIds(ids)
    }
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    override fun findAll(ids: List<Long>, deleteYn: Boolean): List<Comment> {
        if (ids.isEmpty())
            return emptyList()
        return commentMapper.findAllByIdsAndDeleteYn(ids, deleteYn)
    }
    
    /**
     * 작성자 기준 댓글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @return 댓글 정보 목록
     */
    override fun findAllByWriter(writerId: Long): List<Comment> = commentMapper.findAllByWriter(writerId)
    
    /**
     * 작성자 기준 댓글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    override fun findAllByWriter(writerId: Long,
                                 deleteYn: Boolean): List<Comment> = commentMapper.findAllByWriterAndDeleteYn(writerId,
                                                                                                              deleteYn)
    
    /**
     * 게시글 기준 댓글 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 댓글 정보 목록
     */
    override fun findAllByPost(postId: Long): List<Comment> = commentMapper.findAllByPost(postId)
    
    /**
     * 게시글 기준 댓글 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    override fun findAllByPost(postId: Long, deleteYn: Boolean): List<Comment> = commentMapper.findAllByPostAndDeleteYn(
            postId, deleteYn)
    
    /**
     * 작성자 기준 댓글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 댓글 정보 페이징 목록
     */
    override fun findAllByWriter(writerId: Long, pageable: Pageable): List<Comment> {
        pagingAndSorting(pageable)
        return commentMapper.findAllByWriter(writerId)
    }
    
    /**
     * 작성자 기준 댓글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 댓글 정보 페이징 목록
     */
    override fun findAllByWriter(writerId: Long, deleteYn: Boolean, pageable: Pageable): List<Comment> {
        pagingAndSorting(pageable)
        return commentMapper.findAllByWriterAndDeleteYn(writerId, deleteYn)
    }
    
    /**
     * 게시글 기준 댓글 정보 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param pageable - 페이징 목록
     * @return 댓글 정보 페이징 목록
     */
    override fun findAllByPost(postId: Long, pageable: Pageable): List<Comment> {
        pagingAndSorting(pageable)
        return commentMapper.findAllByPost(postId)
    }
    
    /**
     * 게시글 기준 댓글 정보 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 목록
     * @return 댓글 정보 페이징 목록
     */
    override fun findAllByPost(postId: Long, deleteYn: Boolean, pageable: Pageable): List<Comment> {
        pagingAndSorting(pageable)
        return commentMapper.findAllByPostAndDeleteYn(postId, deleteYn)
    }
    
    /**
     * 댓글 정보 수정
     *
     * @param param - 댓글 수정 정보
     */
    override fun update(param: Comment) = commentMapper.update(param)
    
    /**
     * 댓글 삭제
     *
     * @param id       - PK
     * @param writerId - 작성자 FK
     */
    override fun deleteById(id: Long, writerId: Long) = commentMapper.deleteById(id, writerId)
    
    /**
     * 댓글 정보 영구 삭제
     *
     * @param id - PK
     */
    override fun permanentlyDeleteById(id: Long) = commentMapper.permanentlyDeleteById(id)
    
    /**
     * 댓글 목록 삭제
     *
     * @param ids      - PK 목록
     * @param writerId - 작성자 FK
     */
    override fun deleteAllByIds(ids: List<Long>, writerId: Long) {
        if (ids.isEmpty())
            return
        commentMapper.deleteAllByIds(ids, writerId)
    }
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    override fun permanentlyDeleteAllByIds(ids: List<Long>) {
        if (ids.isEmpty())
            return
        commentMapper.permanentlyDeleteAllByIds(ids)
    }
    
    /**
     * 페이징 및 정렬 적용
     *
     * @param pageable - 페이징 객체
     */
    private fun pagingAndSorting(pageable: Pageable): Unit {
        val sb = StringBuilder()
        val prefix = "comment."
        val regex = "^(?!\\s*$)(?!.* (asc|desc)$).+$"
        
        val sort = pageable.sort
        if (!sort.isEmpty) {
            for (order in sort) {
                if (sb.isNotEmpty())
                    sb.append(", ")
                val property = order.property
                val direction = order.direction
                
                when (property) {
                    "createdDate" -> sb.append("${prefix}created_date")
                    "lastModifiedDate" -> sb.append("${prefix}last_modified_date")
                    else -> {}
                }
                
                if (Regex(regex).matches(sb.toString())) {
                    if (direction.isAscending)
                        sb.append(" asc")
                    else
                        sb.append(" desc")
                }
            }
        } else
            sb.append("${prefix}created_date desc")
        
        PageHelper.startPage<Comment>(pageable.pageNumber, pageable.pageSize)
        PageHelper.orderBy(sb.toString())
    }
    
}