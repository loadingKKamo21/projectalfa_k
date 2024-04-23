package com.project.alfa.entities

import javax.persistence.*

@Entity
@Table(name = "tbl_post_attachments")
class Attachment(
        post: Post,
        originalFilename: String,
        storeFilename: String,
        storeFilePath: String,
        fileSize: Long
) : UploadFile(originalFilename, storeFilename, storeFilePath, fileSize) {
    
    init {
        setRelationshipWithPost(post)
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_attachment_id")
    var id: Long? = null
        protected set
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    var post: Post = post
        protected set
    
    @Column(nullable = false)
    var deleteYn: Boolean = false
        protected set
    
    //==================== 연관관계 메서드 ====================//
    
    private fun setRelationshipWithPost(post: Post) {
        this.post = post
        post.attachments.add(this)
    }
    
    //==================== 첨부파일 정보 수정 메서드 ====================//
    
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