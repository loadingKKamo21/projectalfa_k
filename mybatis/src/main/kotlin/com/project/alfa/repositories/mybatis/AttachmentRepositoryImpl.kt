package com.project.alfa.repositories.mybatis

import com.project.alfa.entities.Attachment
import com.project.alfa.repositories.AttachmentRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class AttachmentRepositoryImpl(private val attachmentMapper: AttachmentMapper) : AttachmentRepository {
    
    /**
     * 첨부파일 저장
     *
     * @param attachment - 첨부파일 정보
     * @return 첨부파일 정보
     */
    override fun save(attachment: Attachment): Attachment {
        attachmentMapper.save(attachment)
        return attachment
    }
    
    /**
     * 첨부파일 다중 저장
     *
     * @param attachments - 첨부파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    override fun saveAll(attachments: List<Attachment>): List<Attachment> {
        attachmentMapper.saveAll(attachments)
//        for (attachment in attachments)
//            attachmentMapper.save(attachment)
        return attachments
    }
    
    /**
     * 첨부파일 정보 조회
     *
     * @param id - PK
     * @return 첨부파일 정보
     */
    override fun findById(id: Long): Optional<Attachment> = Optional.ofNullable(attachmentMapper.findById(id))
    
    /**
     * 첨부파일 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보
     */
    override fun findById(id: Long, deleteYn: Boolean): Optional<Attachment> = Optional.ofNullable(
            attachmentMapper.findByIdAndDeleteYn(id, deleteYn))
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @return 첨부파일 정보 목록
     */
    override fun findAll(): List<Attachment> = attachmentMapper.findAll()
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    override fun findAll(deleteYn: Boolean): List<Attachment> = attachmentMapper.findAllByDeleteYn(deleteYn)
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 첨부파일 정보 목록
     */
    override fun findAll(ids: List<Long>): List<Attachment> {
        if (ids.isEmpty())
            return emptyList()
        return attachmentMapper.findAllByIds(ids)
    }
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    override fun findAll(ids: List<Long>, deleteYn: Boolean): List<Attachment> {
        if (ids.isEmpty())
            return emptyList()
        return attachmentMapper.findAllByIdsAndDeleteYn(ids, deleteYn)
    }
    
    /**
     * 게시글 기준 첨부파일 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 첨부파일 정보 목록
     */
    override fun findAll(postId: Long): List<Attachment> = attachmentMapper.findAllByPost(postId)
    
    /**
     * 게시글 기준 첨부파일 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    override fun findAll(postId: Long, deleteYn: Boolean): List<Attachment> = attachmentMapper.findAllByPostAndDeleteYn(
            postId, deleteYn)
    
    /**
     * 첨부파일 삭제
     *
     * @param id     - PK
     * @param postId - 게시글 FK
     */
    override fun deleteById(id: Long, postId: Long) = attachmentMapper.deleteById(id, postId)
    
    /**
     * 첨부파일 정보 영구 삭제
     *
     * @param id - PK
     */
    override fun permanentlyDeleteById(id: Long) = attachmentMapper.permanentlyDeleteById(id)
    
    /**
     * 첨부파일 목록 삭제
     *
     * @param ids    - PK 목록
     * @param postId - 게시글 FK
     */
    override fun deleteAllByIds(ids: List<Long>, postId: Long) {
        if (ids.isEmpty())
            return
        attachmentMapper.deleteAllByIds(ids, postId)
    }
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    override fun permanentlyDeleteAllByIds(ids: List<Long>) {
        if (ids.isEmpty())
            return
        attachmentMapper.permanentlyDeleteAllByIds(ids)
    }
    
}