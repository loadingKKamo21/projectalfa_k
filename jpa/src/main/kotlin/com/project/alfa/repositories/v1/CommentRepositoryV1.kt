package com.project.alfa.repositories.v1

import com.project.alfa.entities.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
class CommentRepositoryV1 {
    
    @PersistenceContext
    private lateinit var em: EntityManager
    
    /**
     * 댓글 저장
     *
     * @param comment - 댓글 정보
     * @return 댓글 정보
     */
    fun save(comment: Comment): Comment {
        em.persist(comment)
        return comment
    }
    
    /**
     * 댓글 정보 조회
     *
     * @param id - PK
     * @return 댓글 정보
     */
    fun findById(id: Long): Optional<Comment> = Optional.ofNullable(
            em.createQuery("SELECT c FROM Comment c WHERE c.id = :id", Comment::class.java)
                    .setParameter("id", id)
                    .resultList.stream().findFirst().orElse(null)
    )
    
    /**
     * 댓글 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보
     */
    fun findById(id: Long, deleteYn: Boolean): Optional<Comment> = Optional.ofNullable(
            em.createQuery("SELECT c FROM Comment c WHERE c.id = :id AND c.deleteYn = :deleteYn",
                           Comment::class.java)
                    .setParameter("id", id)
                    .setParameter("deleteYn", deleteYn)
                    .resultList.stream().findFirst().orElse(null)
    )
    
    /**
     * 댓글 정보 목록 조회
     *
     * @return 댓글 정보 목록
     */
    fun findAll(): List<Comment> = em.createQuery("SELECT c FROM Comment c", Comment::class.java).resultList
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    fun findAll(deleteYn: Boolean): List<Comment> =
            em.createQuery("SELECT c FROM Comment c WHERE c.deleteYn = :deleteYn", Comment::class.java)
                    .setParameter("deleteYn", deleteYn)
                    .resultList
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 댓글 정보 목록
     */
    fun findAll(ids: List<Long>): List<Comment> =
            if (ids.isEmpty())
                emptyList()
            else em.createQuery("SELECT c FROM Comment c WHERE c.id IN :ids", Comment::class.java)
                    .setParameter("ids", ids)
                    .resultList
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    fun findAll(ids: List<Long>, deleteYn: Boolean): List<Comment> =
            if (ids.isEmpty())
                emptyList()
            else em.createQuery("SELECT c FROM Comment c WHERE c.id IN :ids AND c.deleteYn = :deleteYn",
                                Comment::class.java)
                    .setParameter("ids", ids)
                    .setParameter("deleteYn", deleteYn)
                    .resultList
    
    /**
     * 작성자 기준 댓글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @return 댓글 정보 목록
     */
    fun findAllByWriter(writerId: Long): List<Comment> =
            em.createQuery("SELECT c FROM Comment c WHERE c.writer.id = :writerId", Comment::class.java)
                    .setParameter("writerId", writerId)
                    .resultList
    
    /**
     * 작성자 기준 댓글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    fun findAllByWriter(writerId: Long, deleteYn: Boolean): List<Comment> =
            em.createQuery("SELECT c FROM Comment c WHERE c.writer.id = :writerId AND c.deleteYn = :deleteYn",
                           Comment::class.java)
                    .setParameter("writerId", writerId)
                    .setParameter("deleteYn", deleteYn)
                    .resultList
    
    /**
     * 게시글 기준 댓글 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 댓글 정보 목록
     */
    fun findAllByPost(postId: Long): List<Comment> =
            em.createQuery("SELECT c FROM Comment c WHERE c.post.id = :postId", Comment::class.java)
                    .setParameter("postId", postId)
                    .resultList
    
    /**
     * 게시글 기준 댓글 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    fun findAllByPost(postId: Long, deleteYn: Boolean): List<Comment> =
            em.createQuery("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.deleteYn = :deleteYn",
                           Comment::class.java)
                    .setParameter("postId", postId)
                    .setParameter("deleteYn", deleteYn)
                    .resultList
    
    /**
     * 작성자 기준 댓글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 댓글 정보 페이징 목록
     */
    fun findAllByWriter(writerId: Long, pageable: Pageable): Page<Comment> {
        val contentJpql = "SELECT c FROM Comment c WHERE c.writer.id = :writerId ${getSortingJpql(pageable)}"
        val countJpql = "SELECT COUNT(c) FROM Comment c WHERE c.writer.id = :writerId"
        
        val contentQuery = em.createQuery(contentJpql, Comment::class.java)
        val countQuery = em.createQuery(countJpql, Long::class.javaObjectType)
        contentQuery.setParameter("writerId", writerId)
        countQuery.setParameter("writerId", writerId)
        
        val count = countQuery.singleResult
        
        return PageImpl(contentQuery.setFirstResult(pageable.offset.toInt())
                                .setMaxResults(pageable.pageSize)
                                .resultList, pageable, count)
    }
    
    /**
     * 작성자 기준 댓글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 댓글 정보 페이징 목록
     */
    fun findAllByWriter(writerId: Long, deleteYn: Boolean, pageable: Pageable): Page<Comment> {
        val contentJpql = "SELECT c FROM Comment c WHERE c.writer.id = :writerId AND c.deleteYn = :deleteYn ${getSortingJpql(pageable)}"
        val countJpql = "SELECT COUNT(c) FROM Comment c WHERE c.writer.id = :writerId AND c.deleteYn = :deleteYn"
        
        val contentQuery = em.createQuery(contentJpql, Comment::class.java)
        val countQuery = em.createQuery(countJpql, Long::class.javaObjectType)
        contentQuery.setParameter("writerId", writerId)
        contentQuery.setParameter("deleteYn", deleteYn)
        countQuery.setParameter("writerId", writerId)
        countQuery.setParameter("deleteYn", deleteYn)
        
        val count = countQuery.singleResult
        
        return PageImpl(contentQuery.setFirstResult(pageable.offset.toInt())
                                .setMaxResults(pageable.pageSize)
                                .resultList, pageable, count)
    }
    
    /**
     * 게시글 기준 댓글 정보 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param pageable - 페이징 목록
     * @return 댓글 정보 페이징 목록
     */
    fun findAllByPost(postId: Long, pageable: Pageable): Page<Comment> {
        val contentJpql = "SELECT c FROM Comment c WHERE c.post.id = :postId ${getSortingJpql(pageable)}"
        val countJpql = "SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId"
        
        val contentQuery = em.createQuery(contentJpql, Comment::class.java)
        val countQuery = em.createQuery(countJpql, Long::class.javaObjectType)
        contentQuery.setParameter("postId", postId)
        countQuery.setParameter("postId", postId)
        
        val count = countQuery.singleResult
        
        return PageImpl(contentQuery.setFirstResult(pageable.offset.toInt())
                                .setMaxResults(pageable.pageSize)
                                .resultList, pageable, count)
    }
    
    /**
     * 게시글 기준 댓글 정보 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 목록
     * @return 댓글 정보 페이징 목록
     */
    fun findAllByPost(postId: Long, deleteYn: Boolean, pageable: Pageable): Page<Comment> {
        val contentJpql = "SELECT c FROM Comment c WHERE c.post.id = :postId AND c.deleteYn = :deleteYn ${getSortingJpql(pageable)}"
        val countJpql = "SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.deleteYn = :deleteYn"
        
        val contentQuery = em.createQuery(contentJpql, Comment::class.java)
        val countQuery = em.createQuery(countJpql, Long::class.javaObjectType)
        contentQuery.setParameter("postId", postId)
        contentQuery.setParameter("deleteYn", deleteYn)
        countQuery.setParameter("postId", postId)
        countQuery.setParameter("deleteYn", deleteYn)
        
        val count = countQuery.singleResult
        
        return PageImpl(contentQuery.setFirstResult(pageable.offset.toInt())
                                .setMaxResults(pageable.pageSize)
                                .resultList, pageable, count)
    }
    
    /**
     * 댓글 정보 영구 삭제
     *
     * @param comment - 댓글 정보
     */
    fun delete(comment: Comment) = em.remove(em.find(Comment::class.java, comment.id))
    
    /**
     * 댓글 정보 영구 삭제
     *
     * @param id - PK
     */
    fun deleteById(id: Long) = em.remove(em.find(Comment::class.java, id))
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param comments - 댓글 정보 목록
     */
    fun deleteAll(comments: List<Comment>) {
        for (comment in comments)
            em.remove(comment)
    }
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param comments - 댓글 정보 목록
     */
    fun deleteAllInBatch(comments: List<Comment>) {
        val ids = comments.stream().map<Long>(Comment::id).collect(Collectors.toList())
        em.createQuery("DELETE FROM Comment c WHERE c.id IN :ids").setParameter("ids", ids).executeUpdate()
    }
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    fun deleteAllById(ids: List<Long>) {
        for (id in ids)
            em.remove(em.find(Comment::class.java, id))
    }
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    fun deleteAllByIdInBatch(ids: List<Long>) =
            em.createQuery("DELETE FROM Comment c WHERE c.id IN :ids").setParameter("ids", ids).executeUpdate()
    
    //==================== 조건문 생성 메서드 ====================//
    /**
     * 정렬 조건에 따른 조건문 생성
     *
     * @param pageable - 페이징 객체
     * @return JPQL
     */
    private fun getSortingJpql(pageable: Pageable): String {
        val sb = StringBuilder()
        val prefix = " c."
        val regex = "^(?!\\s*$)(?!.* (ASC|DESC)$).+$"
        
        val sort = pageable.sort
        sb.append(" ORDER BY")
        if (!sort.isEmpty) {
            for (order in sort) {
                val property = order.property
                val direction = order.direction
                
                when (property) {
                    "createdDate" -> sb.append("${prefix}createdDate")
                    "lastModifiedDate" -> sb.append("${prefix}lastModifiedDate")
                    else -> {
                    }
                }
                
                if (Pattern.compile(regex).matcher(sb.toString()).matches())
                    if (direction.isAscending)
                        sb.append(" ASC")
                    else
                        sb.append(" DESC")
            }
        } else
            sb.append("${prefix}createdDate DESC")
        
        return sb.toString()
    }
    
}