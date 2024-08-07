package com.project.alfa.controllers.api

import com.google.gson.Gson
import com.project.alfa.repositories.dto.SearchParam
import com.project.alfa.security.CustomUserDetails
import com.project.alfa.services.AttachmentService
import com.project.alfa.services.PostService
import com.project.alfa.services.dto.PostRequestDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping(
    value = ["/api/posts"],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@Tag(name = "Post API", description = "게시글 API 입니다.")
class PostApiController(
    private val postService: PostService,
    private val attachmentService: AttachmentService
) {
    
    /**
     * GET: 게시글 목록 페이지
     *
     * @param searchCondition - 검색 조건
     * @param searchKeyword   - 검색 키워드
     * @param pageable        - 페이징 객체
     * @return
     */
    @GetMapping
    @Tag(name = "Post API")
    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 조회합니다.")
    fun postsList(
        @RequestParam(required = false, value = "condition") searchCondition: String?,
        @RequestParam(required = false, value = "keyword") searchKeyword: String?,
        pageable: Pageable
    ): ResponseEntity<String> = ResponseEntity.ok(
        Gson().toJson(postService.findAllPage(SearchParam(searchCondition, searchKeyword), pageable))
    )
    
    /**
     * GET: 작성자 기준 게시글 목록 페이지
     *
     * @param userDetails
     * @param pageable    - 페이징 객체
     * @return
     */
    @GetMapping("/writer")
    @Tag(name = "Post API")
    @Operation(summary = "작성자 기준 게시글 목록 조회", description = "작성자(로그인된 계정) 기준으로 게시글 목록을 조회합니다.")
    fun postsListByWriter(
        @AuthenticationPrincipal userDetails: UserDetails,
        pageable: Pageable
    ): ResponseEntity<String> = ResponseEntity.ok(
        Gson().toJson(postService.findAllPageByWriter((userDetails as CustomUserDetails).id, pageable))
    )
    
    /**
     * GET: 게시글 상세 조회 페이지
     *
     * @param postId  - 게시글 PK
     * @param request
     * @return
     */
    @GetMapping("/{postId}")
    @Tag(name = "Post API")
    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 조회 페이지를 출력합니다.")
    fun readPostPage(@PathVariable postId: Long, request: HttpServletRequest): ResponseEntity<String> {
        postService.addViewCountWithCaching(postId, request.session.id, request.remoteAddr)
        val map: MutableMap<String, Any> = HashMap()
        map["post"] = postService.readWithCaching(postId, request.session.id, request.remoteAddr)
        map["files"] = attachmentService.findAllFilesByPost(postId)
        return ResponseEntity.ok(Gson().toJson(map))
    }
    
    /**
     * GET: 게시글 작성 페이지
     *
     * @return
     */
    @GetMapping("/write")
    @Tag(name = "Post API")
    @Operation(summary = "게시글 작성 페이지", description = "게시글 작성 페이지를 출력합니다.")
    fun writePostPage(): ResponseEntity<String> = ResponseEntity.ok(Gson().toJson(PostRequestDto()))
    
    /**
     * POST: 게시글 작성
     *
     * @param userDetails
     * @param params      - 게시글 작성 정보 DTO
     * @return
     */
    @PostMapping("/write")
    @Tag(name = "Post API")
    @Operation(summary = "게시글 작성", description = "게시글 작성을 수행합니다.")
    fun writePost(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody params: PostRequestDto
    ): ResponseEntity<String> {
        params.writerId = (userDetails as CustomUserDetails).id
        val id = postService.create(params)
        attachmentService.saveAllFiles(id, params.files)
        return ResponseEntity.ok("Post created successfully.")
    }
    
    /**
     * GET: 게시글 수정 페이지
     *
     * @param postId - 게시글 PK
     * @return
     */
    @GetMapping("/write/{postId}")
    @Tag(name = "Post API")
    @Operation(summary = "게시글 수정 페이지", description = "게시글 수정 페이지를 출력합니다.")
    fun updatePostPage(@PathVariable postId: Long): ResponseEntity<String> {
        val map: MutableMap<String, Any> = HashMap()
        map["post"] = postService.read(postId)
        map["form"] = PostRequestDto()
        map["files"] = attachmentService.findAllFilesByPost(postId)
        return ResponseEntity(Gson().toJson(map), HttpStatus.OK)
    }
    
    /**
     * POST: 게시글 수정
     *
     * @param postId      - 게시글 PK
     * @param userDetails
     * @param params      - 게시글 수정 정보 DTO
     * @return
     */
    @PostMapping("/write/{postId}")
    @Tag(name = "Post API")
    @Operation(summary = "게시글 수정", description = "게시글 수정을 수행합니다.")
    fun updatePost(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody params: PostRequestDto
    ): ResponseEntity<String> {
        params.writerId = (userDetails as CustomUserDetails).id
        postService.update(params)
        attachmentService.deleteAllFilesByIds(params.removeFileIds, postId)
        attachmentService.saveAllFiles(postId, params.files)
        return ResponseEntity.ok("Post updated successfully.")
    }
    
    /**
     * 게시글 삭제
     *
     * @param userDetails
     * @param params      - 게시글 삭제 정보 DTO
     * @return
     */
    @PostMapping("/delete")
    @Tag(name = "Post API")
    @Operation(summary = "게시글 삭제", description = "게시글 삭제를 수행합니다.")
    fun deletePost(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody params: PostRequestDto
    ): ResponseEntity<String> {
        postService.delete(params.id!!, (userDetails as CustomUserDetails).id)
        attachmentService.deleteAllFilesByIds(params.removeFileIds, params.id!!)
        return ResponseEntity.ok("Post deleted successfully.")
    }
    
}