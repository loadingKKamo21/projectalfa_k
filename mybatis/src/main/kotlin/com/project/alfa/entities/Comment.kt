package com.project.alfa.entities

import java.time.LocalDateTime
import javax.validation.constraints.Size

class Comment {
    
    private constructor() : this(null, 0, 0, "")
    
    constructor(
            id: Long? = null,
            writerId: Long,
            postId: Long,
            content: String = ""
    ) {
        this.id = id
        this.writerId = writerId
        this.postId = postId
        this.nickname = ""
        this.content = content
        this.createdDate = null
        this.lastModifiedDate = null
        this.deleteYn = false
    }
    
    var id: Long?                           //PK
        private set
    var writerId: Long                      //작성자 FK
        private set
    var postId: Long                        //게시글 FK
        private set
    var nickname: String                    //닉네임
        private set
    
    @Size(min = 1, max = 100)
    var content: String                     //내용
        private set
    var createdDate: LocalDateTime?         //생성일시
        private set
    var lastModifiedDate: LocalDateTime?    //최종 수정일시
        private set
    var deleteYn: Boolean                   //삭제 여부
        private set
    
}