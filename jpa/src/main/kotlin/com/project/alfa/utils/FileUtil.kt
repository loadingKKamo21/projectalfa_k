package com.project.alfa.utils

import com.project.alfa.entities.UploadFile
import com.project.alfa.services.dto.AttachmentResponseDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class FileUtil {
    
    @Value("\${file.upload.location}")
    lateinit var fileDir: String
    
    /**
     * 다중 파일 업로드
     *
     * @param multipartFiles
     * @return 업로드 파일 정보 목록
     */
    fun storeFiles(multipartFiles: List<MultipartFile>): List<UploadFile> {
        val uploadFiles: MutableList<UploadFile> = ArrayList()
        for (multipartFile in multipartFiles)
            if (!multipartFile.isEmpty)
                storeFile(multipartFile)?.let { uploadFiles.add(it) }
        return uploadFiles
    }
    
    /**
     * 단일 파일 업로드
     *
     * @param multipartFile
     * @return 업로드 파일 정보
     */
    fun storeFile(multipartFile: MultipartFile): UploadFile? {
        if (multipartFile.isEmpty)
            return null
        
        val originalFilename = multipartFile.originalFilename
        val storeFilename = generateStoreFilename(originalFilename!!)
        val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val storeFilePath = getUploadPath(today) + File.separator + storeFilename
        val uploadFile = File(storeFilePath)
        
        multipartFile.transferTo(uploadFile)
        
        return object : UploadFile(originalFilename, storeFilename, storeFilePath, multipartFile.size) {}
    }
    
    /**
     * 업로드 파일 다중 삭제
     *
     * @param uploadFiles - 업로드 파일 정보 목록
     */
    fun deleteFiles(uploadFiles: List<UploadFile>) {
        if (uploadFiles.isEmpty())
            return
        for (uploadFile in uploadFiles) {
            val uploadedDate = uploadFile.createdDate!!.toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            deleteFile(uploadedDate, uploadFile.storeFilename)
        }
    }
    
    /**
     * 업로드 파일 단일 삭제
     *
     * @param uploadFile - 업로드 파일 정보
     */
    fun deleteFile(uploadFile: UploadFile) {
        if (uploadFile.storeFilePath.isBlank())
            return
        val uploadedDate = uploadFile.createdDate!!.toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        deleteFile(uploadedDate, uploadFile.storeFilename)
    }
    
    /**
     * 첨부파일 정보 DTO 리소스 변환
     *
     * @param dto - 업로드 파일 정보
     * @return
     */
    fun readAttachmentFileAsResource(dto: AttachmentResponseDto): Resource {
        val uploadedDate = dto.createdDate.toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val storeFilename = dto.storeFilename
        val filePath = Paths.get(fileDir, uploadedDate, storeFilename)
        
        val resource = UrlResource(filePath.toUri())
        if (!resource.exists() || !resource.isFile)
            throw RuntimeException("File not found: $filePath")
        return resource
    }
    
    /**
     * 저장 파일명 생성
     *
     * @param filename - 원본 파일명
     * @return 저장 파일명
     */
    private fun generateStoreFilename(filename: String): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        val ext = StringUtils.getFilenameExtension(filename)
        return "$uuid.$ext"
    }
    
    /**
     * 업로드 경로
     *
     * @return 업로드 경로
     */
    private fun getUploadPath(): String = makeDirectories(fileDir)
    
    /**
     * 업로드 경로
     *
     * @param addPath - 추가 경로
     * @return 업로드 경로
     */
    private fun getUploadPath(addPath: String) = makeDirectories(fileDir + File.separator + addPath)
    
    /**
     * 업로드 경로 폴더 생성
     *
     * @param path - 대상 업로드 경로
     * @return 업로드 경로
     */
    private fun makeDirectories(path: String): String {
        val dir = File(path)
        if (!dir.exists())
            dir.mkdirs()
        return dir.path
    }
    
    /**
     * 파일 삭제
     *
     * @param addPath  - 추가 경로
     * @param filename - 저장 파일명
     */
    private fun deleteFile(addPath: String, filename: String) {
        val filePath = Paths.get(fileDir, addPath, filename).toString()
        deleteFile(filePath)
    }
    
    /**
     * 파일 삭제
     *
     * @param filePath - 업로드 파일 경로
     */
    private fun deleteFile(filePath: String) {
        val file = File(filePath)
        if (file.exists())
            file.delete()
    }
    
}
