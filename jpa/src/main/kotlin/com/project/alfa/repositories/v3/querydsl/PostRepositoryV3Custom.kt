package com.project.alfa.repositories.v3.querydsl

import com.project.alfa.entities.Post
import com.project.alfa.repositories.dto.SearchParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PostRepositoryV3Custom {
    
    fun findAll(param: SearchParam, pageable: Pageable): Page<Post>
    
    fun findAll(param: SearchParam, deleteYn: Boolean, pageable: Pageable): Page<Post>
    
}