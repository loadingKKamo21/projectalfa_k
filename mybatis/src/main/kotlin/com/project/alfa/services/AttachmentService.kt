package com.project.alfa.services

import com.project.alfa.entities.Attachment
import com.project.alfa.entities.UploadFile
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.repositories.AttachmentRepository
import com.project.alfa.repositories.PostRepository
import com.project.alfa.services.dto.AttachmentResponseDto
import com.project.alfa.utils.FileUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class AttachmentService(
        private val postRepository: PostRepository,
        private val attachmentRepository: AttachmentRepository,
        private val fileUtil: FileUtil
) {
    
    /**
     * 첨부파일 다중 저장
     *
     * @param postId         - 게시글 FK
     * @param multipartFiles
     */
    @Transactional
    fun saveAllFiles(postId: Long, multipartFiles: List<MultipartFile>): List<Long> {
        if (multipartFiles.isEmpty())
            return emptyList()
        
        if (!validatePostExist(postId))
            throw EntityNotFoundException("Could not found 'Post' by id: $postId")
        
        val uploadFiles = fileUtil.storeFiles(multipartFiles)
        val attachments = uploadfilesToAttachments(postId, uploadFiles)
        
        return attachmentRepository.saveAll(attachments).map { it.id!! }
    }
    
    /**
     * PK로 첨부파일 상세 정보 조회
     *
     * @param id - PK
     * @return 첨부파일 정보 DTO
     */
    fun findFileById(id: Long): AttachmentResponseDto =
            AttachmentResponseDto(attachmentRepository.findById(id, false).orElseThrow {
                EntityNotFoundException("Could not found 'Attachment' by id: $id")
            })
    
    /**
     * 게시글 기준 첨부파일 정보 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 첨부 파일 목록
     */
    fun findAllFilesByPost(postId: Long): List<AttachmentResponseDto> {
        if (!validatePostExist(postId))
            throw EntityNotFoundException("Could not found 'Post' by id: $postId")
        
        return attachmentRepository.findAll(postId).map { AttachmentResponseDto(it) }
    }
    
    /**
     * 첨부파일 다중 삭제
     *
     * @param ids    - PK 목록
     * @param postId - 게시글 FK
     */
    @Transactional
    fun deleteAllFilesByIds(ids: List<Long>, postId: Long) {
        val uploadFiles = ArrayList(attachmentRepository.findAll(ids))
        
        if (uploadFiles.isNotEmpty()) {
            fileUtil.deleteFiles(uploadFiles)
            attachmentRepository.deleteAllByIds(ids, postId)
        }
    }
    
    //==================== 변환 메서드 ====================//
    
    /**
     * 업로드 파일 목록 -> 첨부파일 목록 변환
     *
     * @param postId      - 게시글 FK
     * @param uploadFiles - 업로드 파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    private fun uploadfilesToAttachments(postId: Long, uploadFiles: List<UploadFile>): List<Attachment> {
        val attachments: MutableList<Attachment> = ArrayList()
        for (uploadFile in uploadFiles)
            attachments.add(uploadFileToAttachment(postId, uploadFile))
        return attachments
    }
    
    /**
     * 업로드 파일 -> 첨부파일 변환
     *
     * @param postId     - 게시글 FK
     * @param uploadFile - 업로드 파일 정보
     * @return 첨부파일 정보
     */
    private fun uploadFileToAttachment(postId: Long, uploadFile: UploadFile): Attachment = Attachment(postId = postId,
                                                                                                      originalFilename = uploadFile.originalFilename,
                                                                                                      storeFilename = uploadFile.storeFilename,
                                                                                                      storeFilePath = uploadFile.storeFilePath,
                                                                                                      fileSize = uploadFile.fileSize)
    
    //==================== 검증 메서드 ====================//
    
    /**
     * 게시글 FK로 게시글 엔티티 존재 검증
     *
     * @param postId - 게시글 FK
     * @return 존재 여부
     */
    private fun validatePostExist(postId: Long) = postRepository.existsById(postId, false)
    
}