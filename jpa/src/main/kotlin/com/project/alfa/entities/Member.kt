package com.project.alfa.entities

import javax.persistence.*
import javax.validation.constraints.Size

@Entity
@Table(name = "tbl_members")
class Member(username: String, password: String, authInfo: AuthInfo, nickname: String, role: Role?) : BaseTimeEntity() {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    var id: Long? = null                                //PK
        protected set
    
    @Column(nullable = false, updatable = false, unique = true)
    @Size(min = 5)
    var username: String = username.lowercase()         //아이디(이메일)
        protected set
    
    @Column(nullable = false)
    var password: String = password                     //비밀번호
        protected set
    
    @Embedded
    var authInfo: AuthInfo = authInfo                   //인증 정보
        protected set
    
    @Column(nullable = false, unique = true)
    @Size(min = 1, max = 20)
    var nickname: String = nickname                     //닉네임
        protected set
    
    @Size(max = 100)
    var signature: String? = null                       //서명
        protected set
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = role ?: Role.USER                  //계정 유형
        protected set
    
    @Column(nullable = false)
    var deleteYn: Boolean = false                       //탈퇴 여부
        protected set
    
    @OneToMany(mappedBy = "writer")
    var posts: MutableList<Post> = ArrayList()          //작성 게시글 목록
        protected set
    
    @OneToMany(mappedBy = "writer")
    var comments: MutableList<Comment> = ArrayList()    //작성 댓글 목록
        protected set
    
    //==================== 계정 정보 수정 메서드 ====================//
    
    /**
     * 비밀번호 변경
     *
     * @param newPassword - 새로운 비밀번호
     */
    fun updatePassword(newPassword: String) {
        if (newPassword.isNotBlank() && password != newPassword)
            password = newPassword
    }
    
    /**
     * 계정 인증
     */
    fun authenticate() = authInfo.authComplete()
    
    /**
     * 이메일 인증 토큰 업데이트
     *
     * @param newEmailAuthToken - 새로운 이메일 인증 토큰
     */
    fun updateEmailAuthToken(newEmailAuthToken: String) {
        if (newEmailAuthToken.isNotBlank() && authInfo.emailAuthToken != newEmailAuthToken)
            authInfo.updateEmailAuthToken(newEmailAuthToken)
    }
    
    /**
     * 닉네임 변경
     *
     * @param newNickname - 새로운 닉네임
     */
    fun updateNickname(newNickname: String) {
        if (newNickname.isNotBlank() && nickname != newNickname)
            nickname = newNickname
    }
    
    /**
     * 서명 변경
     *
     * @param newSignature - 새로운 서명
     */
    fun updateSignature(newSignature: String?) {
        if (signature == null) {
            if (newSignature?.isNotBlank() == true)
                signature = newSignature
        } else if (signature != newSignature)
            signature = newSignature
    }
    
    /**
     * 계정 유형 변경
     *
     * @param newRole - 새로운 계정 유형
     */
    fun updateRole(newRole: Role) {
        if (role != newRole)
            role = newRole
    }
    
    /**
     * 탈퇴 여부 변경
     *
     * @param newDeleteYn - 새로운 탈퇴 여부
     */
    fun isDelete(newDeleteYn: Boolean) {
        if (deleteYn != newDeleteYn)
            deleteYn = newDeleteYn
    }
    
    //==================== 작성 게시글/댓글 개수 조회 메서드 ====================//
    
    /**
     * 작성 게시글 개수 조회
     *
     * @return 작성 게시글 개수
     */
    fun getPostsCount(): Int = posts.count { !it.deleteYn }
    
    /**
     * 작성 댓글 개수 조회
     *
     * @return 작성 댓글 개수
     */
    fun getCommentsCount(): Int = comments.count { !it.deleteYn }
    
}