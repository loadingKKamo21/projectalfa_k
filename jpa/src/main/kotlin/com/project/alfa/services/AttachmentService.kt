package com.project.alfa.services

import com.project.alfa.entities.Attachment
import com.project.alfa.entities.Post
import com.project.alfa.entities.UploadFile
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.error.exception.InvalidValueException
import com.project.alfa.repositories.v1.AttachmentRepositoryV1
import com.project.alfa.repositories.v1.PostRepositoryV1
import com.project.alfa.services.dto.AttachmentResponseDto
import com.project.alfa.utils.FileUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class AttachmentService(
        private val attachmentRepository: AttachmentRepositoryV1,
        //private val attachmentRepository: AttachmentRepositoryV2,
        //private val attachmentRepository: AttachmentRepositoryV3,
        private val postRepository: PostRepositoryV1,
        //private val postRepository: PostRepositoryV2,
        //private val postRepository: PostRepositoryV3,
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
        
        val post = postRepository.findById(postId, false).orElseThrow {
            EntityNotFoundException("Could not found 'Post' by id: $postId")
        }
        
        val uploadFiles = fileUtil.storeFiles(multipartFiles)
        val attachments = uploadFilesToAttachments(post, uploadFiles)
        
        return attachmentRepository.saveAll(attachments).map { it.id!! }
    }
    
    /**
     * PK로 첨부파일 상세 정보 조회
     *
     * @param id - PK
     * @return 첨부파일 정보 DTO
     */
    fun findFileById(id: Long): AttachmentResponseDto = AttachmentResponseDto(
            attachmentRepository.findById(id, false).orElseThrow {
                EntityNotFoundException("Could not found 'Attachment' by id: $id")
            }
    )
    
    /**
     * 게시글 기준 첨부파일 정보 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 첨부 파일 목록
     */
    fun findAllFilesByPost(postId: Long): List<AttachmentResponseDto> {
        val post = postRepository.findById(postId, false).orElseThrow {
            EntityNotFoundException("Could not found 'Post' by id: $postId")
        }
        return attachmentRepository.findAll(post.id!!).map { AttachmentResponseDto(it) }
    }
    
    /**
     * 첨부파일 다중 삭제
     *
     * @param ids    - PK 목록
     * @param postId - 게시글 FK
     */
    @Transactional
    fun deleteAllFilesByIds(ids: List<Long>, postId: Long) {
        val post = postRepository.findById(postId, false).orElseThrow {
            EntityNotFoundException("Could not found 'Post' by id: $postId")
        }
        
        if (!post.attachments.all { ids.contains(it.id) })
            throw InvalidValueException("Not the file attached on this post.", ErrorCode.NOT_ATTACHMENT_ON_POST)
        
        val attachments = attachmentRepository.findAll(ids)
        if (attachments.isNotEmpty()) {
            fileUtil.deleteFiles(ArrayList<UploadFile>(attachments))
            attachments.forEach { it.isDelete(true) }
        }
    }
    
    //==================== 변환 메서드 ====================//
    
    /**
     * 업로드 파일 목록 -> 첨부파일 목록 변환
     *
     * @param post        - 게시글
     * @param uploadFiles - 업로드 파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    private fun uploadFilesToAttachments(post: Post, uploadFiles: List<UploadFile>): List<Attachment> {
        val attachments: MutableList<Attachment> = ArrayList()
        for (uploadFile in uploadFiles)
            attachments.add(uploadFileToAttachment(post, uploadFile))
        return attachments
    }
    
    /**
     * 업로드 파일 -> 첨부파일 변환
     *
     * @param post       - 게시글
     * @param uploadFile - 업로드 파일 정보
     * @return 첨부파일 정보
     */
    private fun uploadFileToAttachment(post: Post, uploadFile: UploadFile): Attachment =
            Attachment(post = post,
                       originalFilename = uploadFile.originalFilename,
                       storeFilename = uploadFile.storeFilename,
                       storeFilePath = uploadFile.storeFilePath,
                       fileSize = uploadFile.fileSize)
    
}