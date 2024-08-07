package com.project.alfa.controllers.api

import com.google.gson.Gson
import com.project.alfa.security.CustomUserDetails
import com.project.alfa.services.CommentService
import com.project.alfa.services.dto.CommentRequestDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    value = ["/api"],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@Tag(name = "Comment API", description = "댓글 API 입니다.")
class CommentApiController(private val commentService: CommentService) {
    
    /**
     * GET: 댓글 목록 페이지
     *
     * @param postId   - 게시글 FK
     * @param pageable - 페이징 객체
     * @return
     */
    @GetMapping("/posts/{postId}/comments")
    @Tag(name = "Comment API")
    @Operation(summary = "댓글 목록 페이지", description = "게시글의 댓글 목록을 조회합니다.")
    fun commentsList(@PathVariable postId: Long, pageable: Pageable): ResponseEntity<String> =
        ResponseEntity.ok(Gson().toJson(commentService.findAllPageByPost(postId, pageable)))
    
    /**
     * GET: 작성자 기준 댓글 목록 페이지
     *
     * @param userDetails
     * @param pageable    - 페이징 객체
     * @return
     */
    @GetMapping("/comments/writer")
    @Tag(name = "Comment API")
    @Operation(summary = "작성자 기준 댓글 목록 페이지", description = "작성자(로그인된 계정) 기준으로 댓글 목록을 조회합니다.")
    fun commentsListByWriter(
        @AuthenticationPrincipal userDetails: UserDetails,
        pageable: Pageable
    ): ResponseEntity<String> = ResponseEntity.ok(
        Gson().toJson(commentService.findAllPageByWriter((userDetails as CustomUserDetails).id, pageable))
    )
    
    /**
     * GET: 댓글 작성 페이지
     *
     * @param postId - 게시글 FK
     * @return
     */
    @GetMapping("/posts/{postId}/comments/write")
    @Tag(name = "Comment API")
    @Operation(summary = "댓글 작성 페이지", description = "댓글 작성 페이지를 출력합니다.")
    fun writeCommentPage(@PathVariable postId: Long): ResponseEntity<String> =
        ResponseEntity.ok(Gson().toJson(CommentRequestDto()))
    
    /**
     * POST: 댓글 작성
     *
     * @param userDetails
     * @param postId      - 게시글 FK
     * @param params      - 댓글 작성 정보 DTO
     * @return
     */
    @PostMapping("/posts/{postId}/comments/write")
    @Tag(name = "Comment API")
    @Operation(summary = "댓글 작성", description = "댓글 작성을 수행합니다.")
    fun writeComment(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody params: CommentRequestDto
    ): ResponseEntity<String> {
        params.writerId = (userDetails as CustomUserDetails).id
        params.postId = postId
        commentService.create(params)
        return ResponseEntity.ok("Comment created successfully.")
    }
    
    /**
     * GET: 댓글 수정 페이지
     *
     * @param postId    - 게사글 FK
     * @param commentId - 댓글 PK
     * @return
     */
    @GetMapping("/posts/{postId}/comments/write/{commentId}")
    @Tag(name = "Comment API")
    @Operation(summary = "댓글 수정 페이지", description = "댓글 수정 페이지를 출력합니다.")
    fun updateCommentPage(@PathVariable postId: Long, @PathVariable commentId: Long): ResponseEntity<String> {
        val map: MutableMap<String, Any> = HashMap()
        map["comment"] = commentService.read(commentId)
        map["form"] = CommentRequestDto()
        return ResponseEntity(Gson().toJson(map), HttpStatus.OK)
    }
    
    /**
     * POST: 댓글 수정
     *
     * @param postId      - 게시글 FK
     * @param commentId   - 댓글 PK
     * @param userDetails
     * @param params      - 댓글 수정 정보 DTO
     * @return
     */
    @PostMapping("/posts/{postId}/comments/write/{commentId}")
    @Tag(name = "Comment API")
    @Operation(summary = "댓글 수정", description = "댓글 수정을 수행합니다.")
    fun updateComment(
        @PathVariable postId: Long,
        @PathVariable commentId: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody params: CommentRequestDto
    ): ResponseEntity<String> {
        params.writerId = (userDetails as CustomUserDetails).id
        params.postId = postId
        commentService.update(params)
        return ResponseEntity.ok("Comment updated successfully.")
    }
    
    /**
     * POST: 댓글 삭제
     *
     * @param postId      - 게시글 FK
     * @param userDetails
     * @param params      - 댓글 삭제 정보 DTO
     * @return
     */
    @PostMapping("/posts/{postId}/comments/delete")
    @Tag(name = "Comment API")
    @Operation(summary = "댓글 삭제", description = "댓글 삭제를 수행합니다.")
    fun deleteComment(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody params: CommentRequestDto
    ): ResponseEntity<String> {
        commentService.delete(params.id!!, (userDetails as CustomUserDetails).id)
        return ResponseEntity.ok("Comment deleted successfully.")
    }
    
}