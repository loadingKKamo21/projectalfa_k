package com.project.alfa.repositories.v2

import com.project.alfa.entities.Attachment
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class AttachmentRepositoryV2(private val attachmentJpaRepository: AttachmentJpaRepository) {
    
    /**
     * 첨부파일 저장
     *
     * @param attachment - 첨부파일 정보
     * @return 첨부파일 정보
     */
    fun save(attachment: Attachment): Attachment = attachmentJpaRepository.save(attachment)
    
    /**
     * 첨부파일 다중 저장
     *
     * @param attachments - 첨부파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    fun saveAll(attachments: List<Attachment>): List<Attachment> = attachmentJpaRepository.saveAll(attachments)
    
    /**
     * 첨부파일 다중 저장
     *
     * @param attachments - 첨부파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    fun saveAllAndFlush(attachments: List<Attachment>): List<Attachment> = attachmentJpaRepository.saveAllAndFlush(
            attachments)
    
    /**
     * 첨부파일 정보 조회
     *
     * @param id - PK
     * @return 첨부파일 정보
     */
    fun findById(id: Long): Optional<Attachment> = attachmentJpaRepository.findById(id)
    
    /**
     * 첨부파일 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보
     */
    fun findById(id: Long,
                 deleteYn: Boolean): Optional<Attachment> = attachmentJpaRepository.findByIdAndDeleteYn(id, deleteYn)
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @return 첨부파일 정보 목록
     */
    fun findAll(): List<Attachment> = attachmentJpaRepository.findAll()
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    fun findAll(deleteYn: Boolean): List<Attachment> = attachmentJpaRepository.findAllByDeleteYn(deleteYn)
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 첨부파일 정보 목록
     */
    fun findAll(ids: List<Long>): List<Attachment> = attachmentJpaRepository.findAllById(ids)
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    fun findAll(ids: List<Long>,
                deleteYn: Boolean): List<Attachment> = attachmentJpaRepository.findAllByIdInAndDeleteYn(ids, deleteYn)
    
    /**
     * 게시글 기준 첨부파일 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 첨부파일 정보 목록
     */
    fun findAll(postId: Long): List<Attachment> = attachmentJpaRepository.findAllByPost_Id(postId)
    
    /**
     * 게시글 기준 첨부파일 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    fun findAll(postId: Long, deleteYn: Boolean): List<Attachment> =
            attachmentJpaRepository.findAllByPost_IdAndDeleteYn(postId, deleteYn)
    
    /**
     * 첨부파일 정보 영구 삭제
     *
     * @param attachment - 첨부파일 정보
     */
    fun delete(attachment: Attachment) = attachmentJpaRepository.delete(attachment)
    
    /**
     * 첨부파일 정보 영구 삭제
     *
     * @param id - PK
     */
    fun deleteById(id: Long) = attachmentJpaRepository.deleteById(id)
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param attachments - 첨부파일 정보 목록
     */
    fun deleteAll(attachments: List<Attachment>) = attachmentJpaRepository.deleteAll(attachments)
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param attachments - 첨부파일 정보 목록
     */
    fun deleteAllInBatch(attachments: List<Attachment>) = attachmentJpaRepository.deleteAllInBatch(attachments)
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    fun deleteAllById(ids: List<Long>) = attachmentJpaRepository.deleteAllById(ids)
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    fun deleteAllByIdInBatch(ids: List<Long>) = attachmentJpaRepository.deleteAllByIdInBatch(ids)
    
}