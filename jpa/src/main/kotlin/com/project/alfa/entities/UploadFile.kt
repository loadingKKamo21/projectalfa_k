package com.project.alfa.entities

import javax.persistence.Column
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class UploadFile internal constructor(
        originalFilename: String,
        storeFilename: String,
        storeFilePath: String,
        fileSize: Long
) : BaseTimeEntity() {
    
    @Column(nullable = false)
    var originalFilename: String = originalFilename //원본 파일명
        protected set
    
    @Column(nullable = false)
    var storeFilename: String = storeFilename       //저장 파일명
        protected set
    
    @Column(nullable = false)
    var storeFilePath: String = storeFilePath       //저장 경로
        protected set
    
    @Column(nullable = false)
    var fileSize: Long = fileSize                   //파일 크기
        protected set
    
}