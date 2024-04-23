package com.project.alfa.repositories.mybatis

import com.github.pagehelper.PageHelper
import com.project.alfa.entities.Post
import com.project.alfa.repositories.PostRepository
import com.project.alfa.repositories.dto.SearchParam
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PostRepositoryImpl(private val postMapper: PostMapper) : PostRepository {
    
    /**
     * 게시글 저장
     *
     * @param post - 게시글 정보
     * @return 게시글 정보
     */
    override fun save(post: Post): Post {
        postMapper.save(post)
        return post
    }
    
    /**
     * 게시글 정보 조회
     *
     * @param id - PK
     * @return 게시글 정보
     */
    override fun findById(id: Long): Optional<Post> = Optional.ofNullable(postMapper.findById(id))
    
    /**
     * 게시글 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보
     */
    override fun findById(id: Long, deleteYn: Boolean): Optional<Post> = Optional.ofNullable(
            postMapper.findByIdAndDeleteYn(id, deleteYn))
    
    /**
     * 게시글 정보 목록 조회
     *
     * @return 게시글 정보 목록
     */
    override fun findAll(): List<Post> = postMapper.findAll()
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    override fun findAll(deleteYn: Boolean): List<Post> = postMapper.findAllByDeleteYn(deleteYn)
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 게시글 정보 목록
     */
    override fun findAll(ids: List<Long>): List<Post> {
        if (ids.isEmpty())
            return emptyList()
        return postMapper.findAllByIds(ids)
    }
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    override fun findAll(ids: List<Long>, deleteYn: Boolean): List<Post> {
        if (ids.isEmpty())
            return emptyList()
        return postMapper.findAllByIdsAndDeleteYn(ids, deleteYn)
    }
    
    /**
     * 작성자 기준 게시글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @return 게시글 정보 목록
     */
    override fun findAll(writerId: Long): List<Post> = postMapper.findAllByWriter(writerId)
    
    /**
     * 작성자 기준 게시글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    override fun findAll(writerId: Long, deleteYn: Boolean): List<Post> = postMapper.findAllByWriterAndDeleteYn(
            writerId, deleteYn)
    
    /**
     * 게시글 정보 페이징 목록 조회
     *
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    override fun findAll(pageable: Pageable): List<Post> {
        pagingAndSorting(pageable)
        return postMapper.findAll()
    }
    
    /**
     * 게시글 정보 페이징 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    override fun findAll(deleteYn: Boolean, pageable: Pageable): List<Post> {
        pagingAndSorting(pageable)
        return postMapper.findAllByDeleteYn(deleteYn)
    }
    
    /**
     * 작성자 기준 게시글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    override fun findAll(writerId: Long, pageable: Pageable): List<Post> {
        pagingAndSorting(pageable)
        return postMapper.findAllByWriter(writerId)
    }
    
    /**
     * 작성자 기준 게시글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    override fun findAll(writerId: Long, deleteYn: Boolean, pageable: Pageable): List<Post> {
        pagingAndSorting(pageable)
        return postMapper.findAllByWriterAndDeleteYn(writerId, deleteYn)
    }
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    override fun findAll(param: SearchParam, pageable: Pageable): List<Post> {
        pagingAndSorting(pageable)
        return postMapper.findAllBySearchParam(param)
    }
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    override fun findAll(param: SearchParam, deleteYn: Boolean, pageable: Pageable): List<Post> {
        pagingAndSorting(pageable)
        return postMapper.findAllBySearchParamAndDeleteYn(param, deleteYn)
    }
    
    /**
     * 조회수 증가
     *
     * @param id - PK
     */
    override fun addViewCount(id: Long) = postMapper.addViewCount(id)
    
    /**
     * 게시글 정보 수정
     *
     * @param param - 게시글 수정 정보
     */
    override fun update(param: Post) = postMapper.update(param)
    
    /**
     * 게시글 엔티티 존재 확인
     *
     * @param id - PK
     * @return 존재 여부
     */
    override fun existsById(id: Long): Boolean = postMapper.existsById(id)
    
    /**
     * 게시글 엔티티 존재 확인
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 존재 여부
     */
    override fun existsById(id: Long, deleteYn: Boolean): Boolean = postMapper.existsByIdAndDeleteYn(id, deleteYn)
    
    /**
     * 게시글 삭제
     *
     * @param id       - PK
     * @param writerId - 작성자 FK
     */
    override fun deleteById(id: Long, writerId: Long) = postMapper.deleteById(id, writerId)
    
    /**
     * 게시글 정보 영구 삭제
     *
     * @param id - PK
     */
    override fun permanentlyDeleteById(id: Long) = postMapper.permanentlyDeleteById(id)
    
    /**
     * 게시글 목록 삭제
     *
     * @param ids      - PK 목록
     * @param writerId - 작성자 FK
     */
    override fun deleteAllByIds(ids: List<Long>, writerId: Long) {
        if (ids.isEmpty())
            return
        postMapper.deleteAllByIds(ids, writerId)
    }
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    override fun permanentlyDeleteAllByIds(ids: List<Long>) {
        if (ids.isEmpty())
            return
        postMapper.permanentlyDeleteAllByIds(ids)
    }
    
    /**
     * 페이징 및 정렬 적용
     *
     * @param pageable - 페이징 객체
     */
    private fun pagingAndSorting(pageable: Pageable): Unit {
        val sb = StringBuilder()
        val prefix = "post."
        val regex = "^(?!\\s*$)(?!.* (asc|desc)$).+$"
        
        val sort = pageable.sort
        if (!sort.isEmpty) {
            for (order in sort) {
                if (sb.isNotEmpty())
                    sb.append(", ")
                val property = order.property
                val direction = order.direction
                
                when (property) {
                    "viewCount" -> sb.append("${prefix}view_count")
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
        
        PageHelper.startPage<Post>(pageable.pageNumber, pageable.pageSize)
        PageHelper.orderBy(sb.toString())
    }
    
}