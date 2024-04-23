package com.project.alfa.repositories.v1

import com.project.alfa.entities.Post
import com.project.alfa.repositories.dto.SearchParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.util.StringUtils
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
class PostRepositoryV1 {
    
    @PersistenceContext
    private lateinit var em: EntityManager
    
    /**
     * 게시글 저장
     *
     * @param post - 게시글 정보
     * @return 게시글 정보
     */
    fun save(post: Post): Post {
        em.persist(post)
        return post
    }
    
    /**
     * 게시글 정보 조회
     *
     * @param id - PK
     * @return 게시글 정보
     */
    fun findById(id: Long): Optional<Post> = Optional.ofNullable(
            em.createQuery("SELECT p FROM Post p WHERE p.id = :id", Post::class.java)
                    .setParameter("id", id)
                    .resultList.firstOrNull()
    )
    
    /**
     * 게시글 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보
     */
    fun findById(id: Long, deleteYn: Boolean): Optional<Post> = Optional.ofNullable(
            em.createQuery("SELECT p FROM Post p WHERE p.id = :id AND p.deleteYn = :deleteYn", Post::class.java)
                    .setParameter("id", id)
                    .setParameter("deleteYn", deleteYn)
                    .resultList.firstOrNull()
    )
    
    /**
     * 게시글 정보 목록 조회
     *
     * @return 게시글 정보 목록
     */
    fun findAll(): List<Post> = em.createQuery("SELECT p FROM Post p", Post::class.java).resultList
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    fun findAll(deleteYn: Boolean): List<Post> =
            em.createQuery("SELECT p FROM Post p WHERE p.deleteYn = :deleteYn", Post::class.java)
                    .setParameter("deleteYn", deleteYn)
                    .resultList
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 게시글 정보 목록
     */
    fun findAll(ids: List<Long>): List<Post> =
            if (ids.isEmpty())
                emptyList()
            else em.createQuery("SELECT p FROM Post p WHERE p.id IN :ids", Post::class.java)
                    .setParameter("ids", ids)
                    .resultList
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    fun findAll(ids: List<Long>, deleteYn: Boolean): List<Post> =
            if (ids.isEmpty())
                emptyList()
            else em.createQuery("SELECT p FROM Post p WHERE p.id IN :ids AND p.deleteYn = :deleteYn", Post::class.java)
                    .setParameter("ids", ids)
                    .setParameter("deleteYn", deleteYn)
                    .resultList
    
    /**
     * 작성자 기준 게시글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @return 게시글 정보 목록
     */
    fun findAll(writerId: Long): List<Post> =
            em.createQuery("SELECT p FROM Post p WHERE p.writer.id = :writerId", Post::class.java)
                    .setParameter("writerId", writerId)
                    .resultList
    
    /**
     * 작성자 기준 게시글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    fun findAll(writerId: Long, deleteYn: Boolean): List<Post> =
            em.createQuery("SELECT p FROM Post p WHERE p.writer.id = :writerId AND p.deleteYn = :deleteYn",
                           Post::class.java)
                    .setParameter("writerId", writerId)
                    .setParameter("deleteYn", deleteYn)
                    .resultList
    
    /**
     * 게시글 정보 페이징 목록 조회
     *
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    fun findAll(pageable: Pageable): Page<Post> {
        val contentJpql = "SELECT p FROM Post p ${getSortingJpql(pageable)}"
        val countJpql = "SELECT COUNT(p) FROM Post p"
        
        val contentQuery = em.createQuery(contentJpql, Post::class.java)
        val countQuery = em.createQuery(countJpql, Long::class.javaObjectType)
        
        val count = countQuery.singleResult
        
        return PageImpl(contentQuery.setFirstResult(pageable.offset.toInt())
                                .setMaxResults(pageable.pageSize).resultList, pageable, count)
    }
    
    /**
     * 게시글 정보 페이징 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    fun findAll(deleteYn: Boolean, pageable: Pageable): Page<Post> {
        val contentJpql = "SELECT p FROM Post p WHERE p.deleteYn = :deleteYn ${getSortingJpql(pageable)}"
        val countJpql = "SELECT COUNT(p) FROM Post p WHERE p.deleteYn = :deleteYn"
        
        val contentQuery = em.createQuery(contentJpql, Post::class.java)
        val countQuery = em.createQuery(countJpql, Long::class.javaObjectType)
        contentQuery.setParameter("deleteYn", deleteYn)
        countQuery.setParameter("deleteYn", deleteYn)
        
        val count = countQuery.singleResult
        
        return PageImpl(contentQuery.setFirstResult(pageable.offset.toInt())
                                .setMaxResults(pageable.pageSize).resultList, pageable, count)
    }
    
    /**
     * 작성자 기준 게시글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    fun findAll(writerId: Long, pageable: Pageable): Page<Post> {
        val contentJpql = "SELECT p FROM Post p WHERE p.writer.id = :writerId ${getSortingJpql(pageable)}"
        val countJpql = "SELECT COUNT(p) FROM Post p WHERE p.writer.id = :writerId"
        
        val contentQuery = em.createQuery(contentJpql, Post::class.java)
        val countQuery = em.createQuery(countJpql, Long::class.javaObjectType)
        contentQuery.setParameter("writerId", writerId)
        countQuery.setParameter("writerId", writerId)
        
        val count = countQuery.singleResult
        
        return PageImpl(contentQuery.setFirstResult(pageable.offset.toInt())
                                .setMaxResults(pageable.pageSize).resultList, pageable, count)
    }
    
    /**
     * 작성자 기준 게시글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    fun findAll(writerId: Long, deleteYn: Boolean, pageable: Pageable): Page<Post> {
        val contentJpql = "SELECT p FROM Post p WHERE p.writer.id = :writerId AND p.deleteYn = :deleteYn ${getSortingJpql(pageable)}"
        val countJpql = "SELECT COUNT(p) FROM Post p WHERE p.writer.id = :writerId AND p.deleteYn = :deleteYn"
        
        val contentQuery = em.createQuery(contentJpql, Post::class.java)
        val countQuery = em.createQuery(countJpql, Long::class.javaObjectType)
        contentQuery.setParameter("writerId", writerId)
        contentQuery.setParameter("deleteYn", deleteYn)
        countQuery.setParameter("writerId", writerId)
        countQuery.setParameter("deleteYn", deleteYn)
        
        val count = countQuery.singleResult
        
        return PageImpl(contentQuery.setFirstResult(pageable.offset.toInt())
                                .setMaxResults(pageable.pageSize).resultList, pageable, count)
    }
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    fun findAll(param: SearchParam, pageable: Pageable): Page<Post> {
        val contentJpql = "SELECT p FROM Post p ${getSearchingJpql(param)} ${getSortingJpql(pageable)}"
        val countJpql = "SELECT COUNT(p) FROM Post p ${getSearchingJpql(param)}"
        
        val contentQuery = em.createQuery(contentJpql, Post::class.java)
        val countQuery = em.createQuery(countJpql, Long::class.javaObjectType)
        if (StringUtils.hasText(param.searchKeyword)) {
            val keywords = param.keywords
            if (keywords.size == 1) {
                contentQuery.setParameter("keyword", param.searchKeyword)
                countQuery.setParameter("keyword", param.searchKeyword)
            } else if (keywords.size >= 2)
                for (i in 1..keywords.size) {
                    contentQuery.setParameter("keyword$i", keywords[i - 1])
                    countQuery.setParameter("keyword$i", keywords[i - 1])
                }
        }
        
        val count = countQuery.singleResult
        
        return PageImpl(contentQuery.setFirstResult(pageable.offset.toInt())
                                .setMaxResults(pageable.pageSize).resultList, pageable, count)
    }
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    fun findAll(param: SearchParam, deleteYn: Boolean, pageable: Pageable): Page<Post> {
        val contentJpql = "SELECT p FROM Post p ${getSearchingJpql(param)} ${deleteYnJpql(param)} ${
            getSortingJpql(pageable)
        }"
        val countJpql = "SELECT COUNT(p) FROM Post p ${getSearchingJpql(param)} ${deleteYnJpql(param)}"
        
        val contentQuery = em.createQuery(contentJpql, Post::class.java)
        val countQuery = em.createQuery(countJpql, Long::class.javaObjectType)
        if (StringUtils.hasText(param.searchKeyword)) {
            val keywords = param.keywords
            if (keywords.size == 1) {
                contentQuery.setParameter("keyword", param.searchKeyword)
                countQuery.setParameter("keyword", param.searchKeyword)
            } else if (keywords.size >= 2)
                for (i in 1..keywords.size) {
                    contentQuery.setParameter("keyword$i", keywords[i - 1])
                    countQuery.setParameter("keyword$i", keywords[i - 1])
                }
        }
        contentQuery.setParameter("deleteYn", deleteYn)
        countQuery.setParameter("deleteYn", deleteYn)
        
        val count = countQuery.singleResult
        
        return PageImpl(contentQuery.setFirstResult(pageable.offset.toInt())
                                .setMaxResults(pageable.pageSize).resultList, pageable, count)
    }
    
    /**
     * 게시글 정보 영구 삭제
     *
     * @param post - 게시글 정보
     */
    fun delete(post: Post) = em.remove(em.find(Post::class.java, post.id))
    
    /**
     * 게시글 정보 영구 삭제
     *
     * @param id - PK
     */
    fun deleteById(id: Long) = em.remove(em.find(Post::class.java, id))
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param posts - 게시글 정보 목록
     */
    fun deleteAll(posts: List<Post>) {
        for (post in posts)
            em.remove(post)
    }
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param posts - 게시글 정보 목록
     */
    fun deleteAllInBatch(posts: List<Post>) {
        val ids = posts.stream().map<Long>(Post::id).collect(Collectors.toList())
        em.createQuery("DELETE FROM Post p WHERE p.id IN :ids").setParameter("ids", ids).executeUpdate()
    }
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    fun deleteAllById(ids: List<Long>) {
        for (id in ids)
            em.remove(em.find(Post::class.java, id))
    }
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    fun deleteAllByIdInBatch(ids: List<Long>) =
            em.createQuery("DELETE FROM Post p WHERE p.id IN :ids").setParameter("ids", ids).executeUpdate()
    
    //==================== 조건문 생성 메서드 ====================//
    
    /**
     * 정렬 조건에 따른 조건문 생성
     *
     * @param pageable - 페이징 객체
     * @return JPQL
     */
    private fun getSortingJpql(pageable: Pageable): String {
        val sb = StringBuilder()
        val prefix = " p."
        val regex = "^(?!\\s*$)(?!.* (ASC|DESC)$).+$"
        
        val sort = pageable.sort
        sb.append(" ORDER BY")
        if (!sort.isEmpty) {
            for (order in sort) {
                val property = order.property
                val direction = order.direction
                
                when (property) {
                    "viewCount" -> sb.append("${prefix}viewCount")
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
    
    /**
     * 검색 조건, 키워드에 따른 조건문 생성
     *
     * @param param - 검색 조건, 키워드
     * @return JPQL
     */
    private fun getSearchingJpql(param: SearchParam): String {
        val sb = StringBuilder()
        val searchCondition = param.searchCondition
        val keywords = param.keywords
        
        val prefix = " p."
        val title = "${prefix}title LIKE CONCAT('%%', %s, '%%')"
        val content = "${prefix}content LIKE CONCAT('%%', %s, '%%')"
        val writer = "${prefix}writer.nickname LIKE CONCAT('%%', %s, '%%')"
        
        if (StringUtils.hasText(param.searchKeyword)) {
            sb.append(" WHERE")
            if (keywords.size == 1) {
                when (searchCondition) {
                    "title" -> sb.append(String.format(title, ":keyword"))
                    "content" -> sb.append(String.format(content, ":keyword"))
                    "titleOrContent" -> {
                        sb.append(" (")
                        sb.append(String.format(title, ":keyword")).append(" OR")
                                .append(String.format(content, ":keyword"))
                        sb.append(")")
                    }
                    "writer" -> sb.append(String.format(writer, ":keyword"))
                    else -> {
                        sb.append(" (")
                        sb.append(String.format(title, ":keyword")).append(" OR")
                                .append(String.format(content, ":keyword")).append(" OR")
                                .append(String.format(writer, ":keyword"))
                        sb.append(")")
                    }
                }
            } else if (keywords.size >= 2)
                for (i in 1..keywords.size) {
                    sb.append(" (")
                    when (searchCondition) {
                        "title" -> sb.append(String.format(title, ":keyword$i"))
                        "content" -> sb.append(String.format(content, ":keyword$i"))
                        "titleOrContent" -> {
                            sb.append(" (")
                            sb.append(String.format(title, ":keyword$i")).append(" OR")
                                    .append(String.format(content, ":keyword$i"))
                            sb.append(")")
                        }
                        "writer" -> sb.append(String.format(writer, ":keyword$i"))
                        else -> {
                            sb.append(" (")
                            sb.append(String.format(title, ":keyword$i")).append(" OR")
                                    .append(String.format(content, ":keyword$i")).append(" OR")
                                    .append(String.format(writer, ":keyword$i"))
                            sb.append(")")
                        }
                    }
                    sb.append(") OR")
                }
        }
        
        if (sb.toString().endsWith(" OR"))
            sb.delete(sb.length - 3, sb.length)
        
        return sb.toString()
    }
    
    /**
     * 검색 조건, 키워드에 따라 삭제 여부 조건문 생성
     *
     * @param param - 검색 조건, 키워드
     * @return JPQL
     */
    private fun deleteYnJpql(param: SearchParam): String {
        val sb = StringBuilder()
        val prefix = " p."
        
        if (StringUtils.hasText(param.searchKeyword))
            sb.append(" AND ${prefix}deleteYn = :deleteYn")
        else
            sb.append(" WHERE ${prefix}deleteYn = :deleteYn")
        
        return sb.toString()
    }
    
}