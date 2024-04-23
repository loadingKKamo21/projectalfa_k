package com.project.alfa.repositories.v2.specification

import com.project.alfa.entities.Member
import com.project.alfa.entities.Post
import com.project.alfa.repositories.dto.SearchParam
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.util.StringUtils
import java.time.LocalDateTime
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Order
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class PostSpecification {
    
    companion object {
        
        /**
         * 검색 조건, 키워드에 따라 게시글 Specification 생성
         *
         * @param param    - 검색 조건, 키워드
         * @param pageable - 페이징 객체
         * @return
         */
        fun searchAndSortSpecification(param: SearchParam, pageable: Pageable): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                val predicates: MutableList<Predicate> = ArrayList()
                
                val searchCondition = getSearchCondition(param, root, criteriaBuilder)
                if (searchCondition != null)
                    predicates.add(searchCondition)
                
                query.orderBy(getSortCondition(pageable, root, criteriaBuilder))
                
                return@Specification criteriaBuilder.and(*predicates.toTypedArray())
            }
        }
        
        /**
         * 검색 조건, 키워드, 삭제 여부에 따라 게시글 Specification 생성
         *
         * @param param    - 검색 조건, 키워드
         * @param deleteYn - 삭제 여부
         * @param pageable - 페이징 객체
         * @return
         */
        fun searchAndSortSpecification(param: SearchParam,
                                       deleteYn: Boolean,
                                       pageable: Pageable): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                val predicates: MutableList<Predicate> = ArrayList()
                
                val searchCondition = getSearchCondition(param, root, criteriaBuilder)
                if (searchCondition != null)
                    predicates.add(searchCondition)
                
                predicates.add(criteriaBuilder.equal(root.get<Boolean>("deleteYn"), deleteYn))
                
                query.orderBy(getSortCondition(pageable, root, criteriaBuilder))
                
                return@Specification criteriaBuilder.and(*predicates.toTypedArray())
            }
        }
        
        /**
         * 검색 조건, 키워드에 따라 Predicate 생성
         *
         * @param param           - 검색 조건, 키워드
         * @param root
         * @param criteriaBuilder
         * @return
         */
        private fun getSearchCondition(param: SearchParam,
                                       root: Root<Post>,
                                       criteriaBuilder: CriteriaBuilder): Predicate? {
            val searchKeyword = param.searchKeyword
            val searchCondition = param.searchCondition
            val keywords = param.keywords
            
            if (!StringUtils.hasText(searchKeyword) || keywords.isEmpty())
                return null
            
            val predicates: MutableList<Predicate> = ArrayList()
            
            if (keywords.size == 1) when (searchCondition) {
                "title" -> predicates.add(criteriaBuilder.like(root.get("title"), "%$searchKeyword%"))
                "content" -> predicates.add(criteriaBuilder.like(root.get("content"), "%$searchKeyword%"))
                "titleOrContent" -> {
                    predicates.add(criteriaBuilder.like(root.get("title"), "%$searchKeyword%"))
                    predicates.add(criteriaBuilder.like(root.get("content"), "%$searchKeyword%"))
                }
                "writer" -> predicates.add(criteriaBuilder.like(root.get<Member>("writer").get("nickname"), "%$searchKeyword%"))
                else -> {
                    predicates.add(criteriaBuilder.like(root.get("title"), "%$searchKeyword%"))
                    predicates.add(criteriaBuilder.like(root.get("content"), "%$searchKeyword%"))
                    predicates.add(criteriaBuilder.like(root.get<Member>("writer").get("nickname"), "%$searchKeyword%"))
                }
            } else if (keywords.size >= 2)
                for (i in 1..keywords.size)
                    when (searchCondition) {
                        "title" -> predicates.add(criteriaBuilder.like(root.get("title"), "%${keywords[i - 1]}%"))
                        "content" -> predicates.add(criteriaBuilder.like(root.get("content"), "%${keywords[i - 1]}%"))
                        "titleOrContent" -> {
                            predicates.add(criteriaBuilder.like(root.get("title"), "%${keywords[i - 1]}%"))
                            predicates.add(criteriaBuilder.like(root.get("content"), "%${keywords[i - 1]}%"))
                        }
                        "writer" -> predicates.add(criteriaBuilder.like(root.get<Member>("writer").get("nickname"), "%${keywords[i - 1]}%"))
                        else -> {
                            predicates.add(criteriaBuilder.like(root.get("title"), "%${keywords[i - 1]}%"))
                            predicates.add(criteriaBuilder.like(root.get("content"), "%${keywords[i - 1]}%"))
                            predicates.add(criteriaBuilder.like(root.get<Member>("writer").get("nickname"), "%${keywords[i - 1]}%"))
                        }
                    }
            
            return criteriaBuilder.or(*predicates.toTypedArray())
        }
        
        /**
         * 페이징 객체에 포함된 정렬 조건에 따라 Order 목록 생성
         *
         * @param pageable        - 페이징 객체
         * @param root
         * @param criteriaBuilder
         * @return
         */
        private fun getSortCondition(pageable: Pageable, root: Root<Post>,
                                     criteriaBuilder: CriteriaBuilder): List<Order> {
            val orders: MutableList<Order> = ArrayList()
            
            if (!pageable.sort.isEmpty)
                pageable.sort.forEach { order ->
                    when (order.property) {
                        "viewCount" -> {
                            if (order.direction.isAscending)
                                orders.add(criteriaBuilder.asc(root.get<Int>("viewCount")))
                            else
                                orders.add(criteriaBuilder.desc(root.get<Int>("viewCount")))
                        }
                        "createdDate" -> {
                            if (order.direction.isAscending)
                                orders.add(criteriaBuilder.asc(root.get<LocalDateTime>("createdDate")))
                            else
                                orders.add(criteriaBuilder.desc(root.get<LocalDateTime>("createdDate")))
                        }
                        "lastModifiedDate" -> {
                            if (order.direction.isAscending)
                                orders.add(criteriaBuilder.asc(root.get<LocalDateTime>("lastModifiedDate")))
                            else
                                orders.add(criteriaBuilder.asc(root.get<LocalDateTime>("lastModifiedDate")))
                        }
                        else -> {
                        }
                    }
                }
            
            if (orders.isEmpty())
                orders.add(criteriaBuilder.desc(root.get<LocalDateTime>("createdDate")))
            
            return orders
        }
        
    }
    
}