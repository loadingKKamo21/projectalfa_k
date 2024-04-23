package com.project.alfa.repositories.mybatis

import com.project.alfa.entities.Comment
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface CommentMapper {
    
    fun save(comment: Comment): Unit
    
    fun findById(id: Long): Comment
    
    fun findByIdAndDeleteYn(@Param("id") id: Long, @Param("deleteYn") deleteYn: Boolean): Comment
    
    fun findAll(): List<Comment>
    
    fun findAllByDeleteYn(deleteYn: Boolean): List<Comment>
    
    fun findAllByIds(@Param("ids") ids: List<Long>): List<Comment>
    
    fun findAllByIdsAndDeleteYn(@Param("ids") ids: List<Long>, @Param("deleteYn") deleteYn: Boolean): List<Comment>
    
    fun findAllByWriter(writerId: Long): List<Comment>
    
    fun findAllByWriterAndDeleteYn(@Param("writerId") writerId: Long,
                                   @Param("deleteYn") deleteYn: Boolean): List<Comment>
    
    fun findAllByPost(postId: Long): List<Comment>
    
    fun findAllByPostAndDeleteYn(@Param("postId") postId: Long,
                                 @Param("deleteYn") deleteYn: Boolean): List<Comment>
    
    fun update(param: Comment): Unit
    
    fun deleteById(@Param("id") id: Long, @Param("writerId") writerId: Long): Unit
    
    fun permanentlyDeleteById(id: Long): Unit
    
    fun deleteAllByIds(@Param("ids") ids: List<Long>, @Param("writerId") writerId: Long): Unit
    
    fun permanentlyDeleteAllByIds(@Param("ids") ids: List<Long>): Unit
    
}