package com.project.alfa.entities

import javax.persistence.*
import javax.validation.constraints.Size

@Entity
@Table(name = "tbl_comments")
class Comment(writer: Member, post: Post, content: String) : BaseTimeEntity() {
    
    init {
        setRelationshipWithMember(writer)
        setRelationshipWithPost(post)
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    var id: Long? = null            //PK
        protected set
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var writer: Member = writer     //작성자
        protected set
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    var post: Post = post           //게시글
        protected set
    
    @Column(nullable = false)
    @Size(min = 1, max = 100)
    var content: String = content   //내용
        protected set
    
    @Column(nullable = false)
    var deleteYn: Boolean = false   //삭제 여부
        protected set
    
    //==================== 연관관계 메서드 ====================//
    
    private fun setRelationshipWithMember(member: Member) {
        writer = member
        member.comments.add(this)
    }
    
    private fun setRelationshipWithPost(post: Post) {
        this.post = post
        post.comments.add(this)
    }
    
    //==================== 댓글 정보 수정 메서드 ====================//
    
    /**
     * 내용 변경
     *
     * @param newContent - 새로운 내용
     */
    fun updateContent(newContent: String) {
        if (newContent.isNotBlank() && content != newContent)
            content = newContent
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
    
}