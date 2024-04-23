package com.project.alfa.entities

import java.time.LocalDateTime

abstract class UploadFile protected constructor(
        originalFilename: String,
        storeFilename: String,
        storeFilePath: String,
        fileSize: Long
) {
    
    var originalFilename: String = originalFilename //원본 파일명
        private set
    var storeFilename: String = storeFilename       //저장 파일명
        private set
    var storeFilePath: String = storeFilePath       //저장 경로
        private set
    var fileSize: Long = fileSize                   //파일 크기
        private set
    var createdDate: LocalDateTime? = null          //생성일시
        private set
    var lastModifiedDate: LocalDateTime? = null     //최종 수정일시
        private set
    
    init {
        this.createdDate = null
        this.lastModifiedDate = null
    }
    
}