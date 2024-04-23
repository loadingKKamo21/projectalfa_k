package com.project.alfa.repositories.mybatis

import com.project.alfa.entities.Post
import com.project.alfa.repositories.dto.SearchParam
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface PostMapper {
    
    fun save(post: Post): Unit
    
    fun findById(id: Long): Post
    
    fun findByIdAndDeleteYn(@Param("id") id: Long, @Param("deleteYn") deleteYn: Boolean): Post
    
    fun findAll(): List<Post>
    
    fun findAllByDeleteYn(deleteYn: Boolean): List<Post>
    
    fun findAllByIds(@Param("ids") ids: List<Long>): List<Post>
    
    fun findAllByIdsAndDeleteYn(@Param("ids") ids: List<Long>, @Param("deleteYn") deleteYn: Boolean): List<Post>
    
    fun findAllByWriter(writerId: Long): List<Post>
    
    fun findAllByWriterAndDeleteYn(@Param("writerId") writerId: Long, @Param("deleteYn") deleteYn: Boolean): List<Post>
    
    fun findAllBySearchParam(param: SearchParam): List<Post>
    
    fun findAllBySearchParamAndDeleteYn(@Param("param") param: SearchParam,
                                        @Param("deleteYn") deleteYn: Boolean): List<Post>
    
    fun addViewCount(id: Long): Unit
    
    fun update(param: Post): Unit
    
    fun existsById(id: Long): Boolean
    
    fun existsByIdAndDeleteYn(@Param("id") id: Long, @Param("deleteYn") deleteYn: Boolean): Boolean
    
    fun deleteById(@Param("id") id: Long, @Param("writerId") writerId: Long): Unit
    
    fun permanentlyDeleteById(id: Long): Unit
    
    fun deleteAllByIds(@Param("ids") ids: List<Long>, @Param("writerId") writerId: Long): Unit
    
    fun permanentlyDeleteAllByIds(@Param("ids") ids: List<Long>): Unit
    
}
