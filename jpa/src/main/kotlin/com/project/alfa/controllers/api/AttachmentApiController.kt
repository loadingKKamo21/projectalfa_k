package com.project.alfa.controllers.api

import com.google.gson.Gson
import com.project.alfa.services.AttachmentService
import com.project.alfa.utils.FileUtil
import org.springframework.core.io.Resource
import org.springframework.http.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URLEncoder

@RestController
@RequestMapping(value = ["/api/posts/{postId}/attachments"],
                consumes = [MediaType.APPLICATION_JSON_VALUE])
class AttachmentApiController(
        private val attachmentService: AttachmentService,
        private val fileUtil: FileUtil
) {
    
    /**
     * GET: 첨부파일 목록 조회
     *
     * @param postId - 게시글 FK
     * @return
     */
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findAllFilesByPost(@PathVariable postId: Long): ResponseEntity<String> =
            ResponseEntity.ok(Gson().toJson(attachmentService.findAllFilesByPost(postId)))
    
    /**
     * GET: 첨부파일 다운로드
     *
     * @param postId - 게시글 FK
     * @param fileId - 첨부파일 PK
     * @return
     */
    @GetMapping(value = ["/{fileId}/download"],
                produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun downloadFile(@PathVariable postId: Long, @PathVariable fileId: Long): ResponseEntity<Resource> {
        val file = attachmentService.findFileById(fileId)
        val resource = fileUtil.readAttachmentFileAsResource(file)
        
        val filename = URLEncoder.encode(file.originalFilename, "UTF-8")
        val httpHeaders: HttpHeaders = HttpHeaders().apply {
            contentDisposition = ContentDisposition.attachment().filename(filename).build()
            contentLength = file.fileSize
        }
        
        return ResponseEntity(resource, httpHeaders, HttpStatus.OK)
    }
    
}