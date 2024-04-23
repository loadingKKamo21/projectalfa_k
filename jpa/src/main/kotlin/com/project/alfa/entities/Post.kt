package com.project.alfa.entities

import javax.persistence.*
import javax.validation.constraints.Size

@Entity
@Table(name = "tbl_posts")
class Post(writer: Member, title: String, content: String?, noticeYn: Boolean) : BaseTimeEntity() {
    
    init {
        setRelationshipWithMember(writer)
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    var id: Long? = null                                            //PK
        protected set
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var writer: Member = writer                                     //작성자
        protected set
    
    @Column(nullable = false)
    @Size(min = 1, max = 100)
    var title: String = title                                       //제목
        protected set
    
    @Lob
    var content: String? = content                                  //내용
        protected set
    
    @Column(nullable = false)
    var viewCount: Int = 0                                          //조회수
        protected set
    
    @Column(nullable = false)
    var noticeYn: Boolean = writer.role == Role.ADMIN && noticeYn   //공지 여부
        protected set
    
    @Column(nullable = false)
    var deleteYn: Boolean = false                                   //삭제 여부
        protected set
    
    @OneToMany(mappedBy = "post")
    var comments: MutableList<Comment> = ArrayList()                //댓글 목록
        protected set
    
    @OneToMany(mappedBy = "post")
    var attachments: MutableList<Attachment> = ArrayList()          //첨부파일 목록
        protected set
    
    //==================== 연관관계 메서드 ====================//
    
    private fun setRelationshipWithMember(member: Member) {
        writer = member
        member.posts.add(this)
    }
    
    //==================== 게시글 정보 수정 메서드 ====================//
    
    /**
     * 제목 변경
     *
     * @param newTitle - 새로운 제목
     */
    fun updateTitle(newTitle: String) {
        if (newTitle.isNotBlank() && title != newTitle)
            title = newTitle
    }
    
    /**
     * 내용 변경
     *
     * @param newContent - 새로운 내용
     */
    fun updateContent(newContent: String?) {
        if (content == null) {
            if (newContent?.isNotBlank() == true)
                content = newContent
        } else if (content != newContent)
            content = newContent
    }
    
    /**
     * 공지 여부 변경
     *
     * @param newNoticeYn - 새로운 공지 여부
     */
    fun updateNoticeYn(newNoticeYn: Boolean) {
        if (writer.role == Role.ADMIN && noticeYn != newNoticeYn)
            noticeYn = newNoticeYn
    }
    
    /**
     * 삭제 여부 변경
     *
     * @param newDeleteYn - 새로운 삭제 여부
     */
    fun isDelete(newDeleteYn: Boolean) {
        if (deleteYn != newDeleteYn)
            deleteYn = newDeleteYn
    }
    
    //==================== 조회수 증가 메서드 ====================//
    
    /**
     * 조회수 증가
     */
    fun addViewCount() {
        viewCount += 1
    }
    
    //==================== 댓글/첨부파일 개수 조회 메서드 ====================//
    
    /**
     * 댓글 개수 조회
     *
     * @return 댓글 개수
     */
    fun getCommentsCount(): Int = comments.count { !it.deleteYn }
    
    /**
     * 첨부파일 개수 조회
     *
     * @return 첨부파일 개수
     */
    fun getAttachmentsCount(): Int = attachments.count { !it.deleteYn }
    
}