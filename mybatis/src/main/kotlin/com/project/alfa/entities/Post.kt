package com.project.alfa.entities

import java.time.LocalDateTime
import javax.validation.constraints.Size

class Post {
    
    private constructor() : this(null, 0, "", null, false)
    
    constructor(
            id: Long? = null,
            writerId: Long,
            title: String = "",
            content: String? = null,
            noticeYn: Boolean = false
    ) {
        this.id = id
        this.writerId = writerId
        this.nickname = ""
        this.title = title
        this.content = content
        this.viewCount = 0
        this.noticeYn = noticeYn
        this.createdDate = null
        this.lastModifiedDate = null
        this.commentIds = ArrayList()
        this.attachmentIds = ArrayList()
        this.deleteYn = false
    }
    
    var id: Long?                           //PK
        private set
    var writerId: Long                      //작성자 FK
        private set
    var nickname: String                    //닉네임
        private set
    
    @Size(min = 1, max = 100)
    var title: String                       //제목
        private set
    var content: String?                    //내용
        private set
    var viewCount: Int                      //조회수
        private set
    var noticeYn: Boolean                   //공지 여부
        private set
    var createdDate: LocalDateTime?         //생성일시
        private set
    var lastModifiedDate: LocalDateTime?    //최종 수정일시
        private set
    var commentIds: List<Long>              //작성 댓글 FK 목록
        private set
    var attachmentIds: List<Long>           //첨부파일 FK 목록
        private set
    var deleteYn: Boolean                   //삭제 여부
        private set
    
}