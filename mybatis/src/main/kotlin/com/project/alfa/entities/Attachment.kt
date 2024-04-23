package com.project.alfa.entities

class Attachment : UploadFile {
    
    private constructor() : this(null, 0, "", "", "", 0)
    
    constructor(
            id: Long? = null,
            postId: Long,
            originalFilename: String,
            storeFilename: String,
            storeFilePath: String,
            fileSize: Long
    ) : super(originalFilename, storeFilename, storeFilePath, fileSize) {
        this.id = id
        this.postId = postId
        this.deleteYn = false
    }
    
    var id: Long?           //PK
        private set
    var postId: Long        //게시글 FK
        private set
    var deleteYn: Boolean   //삭제 여부
        private set
    
}