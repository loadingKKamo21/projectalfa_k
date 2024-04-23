package com.project.alfa.entities

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity {
    
    @CreatedDate
    @Column(updatable = false)
    var createdDate: LocalDateTime? = null      //생성일시
        protected set
    
    @LastModifiedDate
    var lastModifiedDate: LocalDateTime? = null //최종 수정일시
        protected set
    
}