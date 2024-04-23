package com.project.alfa.repositories.v3.querydsl

import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.project.alfa.entities.Post
import com.project.alfa.entities.QPost
import com.project.alfa.repositories.dto.SearchParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import org.springframework.util.StringUtils

@Repository
class PostRepositoryV3Impl(private val jpaQueryFactory: JPAQueryFactory) : PostRepositoryV3Custom {
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    override fun findAll(param: SearchParam, pageable: Pageable): Page<Post> {
        val content = jpaQueryFactory.selectFrom(QPost.post)
                .where(getSearchCondition(param))
                .orderBy(*getSortCondition(pageable))
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
        val count = jpaQueryFactory.select(QPost.post.count())
                .from(QPost.post)
                .where(getSearchCondition(param))
        return PageableExecutionUtils.getPage(content, pageable) { count.fetchOne() ?: 0 }
    }
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    override fun findAll(param: SearchParam, deleteYn: Boolean, pageable: Pageable): Page<Post> {
        val content = jpaQueryFactory.selectFrom(QPost.post)
                .where(getSearchCondition(param), QPost.post.deleteYn.eq(deleteYn))
                .orderBy(*getSortCondition(pageable))
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
        val count = jpaQueryFactory.select(QPost.post.count())
                .from(QPost.post)
                .where(getSearchCondition(param))
        return PageableExecutionUtils.getPage(content, pageable) { count.fetchOne() ?: 0 }
    }
    
    /**
     * 검색 조건, 키워드에 따라 BooleanExpression 생성
     *
     * @param param - 검색 조건, 키워드
     * @return
     */
    private fun getSearchCondition(param: SearchParam): BooleanExpression? {
        val searchKeyword = param.searchKeyword
        val searchCondition = param.searchCondition
        val keywords = param.keywords
        
        if (!StringUtils.hasText(searchKeyword) || keywords.isNotEmpty()) {
            if (keywords.size == 1) {
                val titleExpression = QPost.post.title.like("%$searchKeyword%")
                val contentExpression = QPost.post.content.like("%$searchKeyword%")
                val writerExpression = QPost.post.writer.nickname.like("%$searchKeyword%")
                
                return when (searchCondition) {
                    "title" -> titleExpression
                    "content" -> contentExpression
                    "titleOrContent" -> titleExpression.or(contentExpression)
                    "writer" -> writerExpression
                    else -> titleExpression.or(contentExpression).or(writerExpression)
                }
            } else if (keywords.size >= 2) {
                var expression: BooleanExpression? = null
                
                for (i in 1..keywords.size) {
                    val titleExpression = QPost.post.title.like("%${keywords[i - 1]}%")
                    val contentExpression = QPost.post.content.like("%${keywords[i - 1]}%")
                    val writerExpression = QPost.post.writer.nickname.like("%${keywords[i - 1]}%")
                    
                    expression = when (searchCondition) {
                        "title" -> if (i == 1) titleExpression else expression!!.or(titleExpression)
                        "content" -> if (i == 1) contentExpression else expression!!.or(contentExpression)
                        "titleOrContent" -> if (i == 1) titleExpression.or(contentExpression) else expression!!.or(titleExpression.or(contentExpression))
                        "writer" -> if (i == 1) writerExpression else expression!!.or(writerExpression)
                        else -> if (i == 1) titleExpression.or(contentExpression).or(writerExpression) else expression!!.or(titleExpression.or(contentExpression).or(writerExpression))
                    }
                }
                
                return expression
            }
        }
        
        return null
    }
    
    /**
     * 페이징 객체에 포함된 정렬 조건에 따라 OrderSpecifier 목록 생성
     *
     * @param pageable - 페이징 객체
     * @return
     */
    private fun getSortCondition(pageable: Pageable): Array<OrderSpecifier<*>?> {
        val orderSpecifiers: MutableList<OrderSpecifier<*>> = ArrayList()
        
        if (!pageable.sort.isEmpty)
            pageable.sort.forEach { order ->
                val direction = if (order.direction.isAscending) Order.ASC else Order.DESC
                
                when (order.property) {
                    "viewCount" -> orderSpecifiers.add(OrderSpecifier(direction, QPost.post.viewCount))
                    "createdDate" -> orderSpecifiers.add(OrderSpecifier(direction, QPost.post.createdDate))
                    "lastModifiedDate" -> orderSpecifiers.add(OrderSpecifier(direction, QPost.post.lastModifiedDate))
                    else -> {
                    }
                }
            }
        
        if (orderSpecifiers.isEmpty())
            orderSpecifiers.add(OrderSpecifier(Order.DESC, QPost.post.createdDate))
        
        return orderSpecifiers.toTypedArray()
    }
    
}