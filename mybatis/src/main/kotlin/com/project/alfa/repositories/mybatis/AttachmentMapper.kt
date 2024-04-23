package com.project.alfa.repositories.mybatis

import com.project.alfa.entities.Attachment
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface AttachmentMapper {
    
    fun save(attachment: Attachment): Unit
    
    fun saveAll(@Param("params") params: List<Attachment>): Unit
    
    fun findById(id: Long): Attachment
    
    fun findByIdAndDeleteYn(@Param("id") id: Long, @Param("deleteYn") deleteYn: Boolean): Attachment
    
    fun findAll(): List<Attachment>
    
    fun findAllByDeleteYn(deleteYn: Boolean): List<Attachment>
    
    fun findAllByIds(@Param("ids") ids: List<Long>): List<Attachment>
    
    fun findAllByIdsAndDeleteYn(@Param("ids") ids: List<Long>, @Param("deleteYn") deleteYn: Boolean): List<Attachment>
    
    fun findAllByPost(postId: Long): List<Attachment>
    
    fun findAllByPostAndDeleteYn(@Param("postId") postId: Long, @Param("deleteYn") deleteYn: Boolean): List<Attachment>
    
    fun deleteById(@Param("id") id: Long, @Param("postId") postId: Long): Unit
    
    fun permanentlyDeleteById(id: Long): Unit
    
    fun deleteAllByIds(@Param("ids") ids: List<Long>, @Param("postId") postId: Long): Unit
    
    fun permanentlyDeleteAllByIds(@Param("ids") ids: List<Long>): Unit
    
}