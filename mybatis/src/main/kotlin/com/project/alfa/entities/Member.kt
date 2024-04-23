package com.project.alfa.entities

import java.time.LocalDateTime
import javax.validation.constraints.Size

class Member {
    
    private constructor() : this(null, "", "", AuthInfo(), "", null, null)
    
    constructor(
            id: Long? = null,
            username: String,
            password: String,
            authInfo: AuthInfo = AuthInfo(),
            nickname: String,
            signature: String? = null,
            role: Role? = null
    ) {
        this.id = id
        this.username = if (username.isBlank()) "" else username.lowercase()
        this.password = password
        this.authInfo = authInfo
        this.nickname = nickname
        this.signature = signature
        this.role = role
        this.createdDate = null
        this.lastModifiedDate = null
        this.postIds = ArrayList()
        this.commentIds = ArrayList()
        this.deleteYn = false
    }
    
    var id: Long?                           //PK
        private set
    
    @Size(min = 5)
    var username: String                    //아이디(이메일)
        private set
    var password: String                    //비밀번호
        private set
    var authInfo: AuthInfo                  //인증 정보
        private set
    
    @Size(min = 1, max = 20)
    var nickname: String                    //닉네임
        private set
    
    @Size(max = 100)
    var signature: String?                  //서명
        private set
    var role: Role?                         //계정 유형
        private set
    var createdDate: LocalDateTime?         //생성일시
        private set
    var lastModifiedDate: LocalDateTime?    //최종 수정일시
        private set
    var postIds: List<Long>                 //작성 게시글 FK 목록
        private set
    var commentIds: List<Long>              //작성 댓글 FK 목록
        private set
    var deleteYn: Boolean                  //탈퇴 여부
        private set
    
}