package com.project.alfa.controllers.api

import com.google.gson.Gson
import com.project.alfa.error.ErrorResponse
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.security.CustomUserDetails
import com.project.alfa.services.MemberService
import com.project.alfa.services.dto.MemberJoinRequestDto
import com.project.alfa.services.dto.MemberUpdateRequestDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    value = ["/api/members"],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@Tag(name = "Member API", description = "회원 API 입니다.")
class MemberApiController(private val memberService: MemberService) {
    
    /**
     * GET: 회원 가입 페이지
     *
     * @return
     */
    @GetMapping
    @Tag(name = "Member API")
    @Operation(summary = "회원 가입 페이지", description = "회원 가입 페이지를 출력합니다.")
    fun joinPage(): ResponseEntity<String> = ResponseEntity.ok(Gson().toJson(MemberJoinRequestDto()))
    
    /**
     * POST: 회원 가입
     *
     * @param params - 회원 가입 정보 DTO
     * @return
     */
    @PostMapping
    @Tag(name = "Member API")
    @Operation(summary = "회원 가입", description = "회원 가입을 수행합니다.")
    fun join(@Valid @RequestBody params: MemberJoinRequestDto): ResponseEntity<String> {
        memberService.join(params)
        return ResponseEntity.ok("Member joined successfully.")
    }
    
    /**
     * POST: 비밀번호 찾기
     *
     * @param username - 아이디(이메일)
     * @return
     */
    @PostMapping("/forgot-password")
    @Tag(name = "Member API")
    @Operation(summary = "비밀번호 찾기", description = "비밀번호 찾기를 수행합니다.")
    fun forgotPassword(@RequestBody username: String): ResponseEntity<String> {
        //입력된 아이디(이메일) 검사
        if (!Regex("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$").matches(username))
            return ResponseEntity(
                Gson().toJson(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE)),
                HttpStatus.BAD_REQUEST
            )
        memberService.findPassword(username)
        return ResponseEntity.ok("Successfully sending of \"Find Password\" email.")
    }
    
    /**
     * GET: 프로필 조회 페이지
     *
     * @param userDetails
     * @return
     */
    @GetMapping("/profile")
    @Tag(name = "Member API")
    @Operation(summary = "프로필 조회", description = "프로필 조회 페이지를 출력합니다.")
    fun profilePage(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<String> =
        ResponseEntity.ok(Gson().toJson(memberService.findByUsername(userDetails.username)))
    
    /**
     * GET: 프로필 수정 페이지
     *
     * @param userDetails
     * @return
     */
    @GetMapping("/profile-update")
    @Tag(name = "Member API")
    @Operation(summary = "프로필 수정 페이지", description = "프로필 수정 페이지를 출력합니다.")
    fun profileUpdatePage(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<String> {
        val map: MutableMap<String, Any> = HashMap()
        map["member"] = memberService.findByUsername(userDetails.username)
        map["form"] = MemberUpdateRequestDto()
        return ResponseEntity(Gson().toJson(map), HttpStatus.OK)
    }
    
    /**
     * POST: 프로필 수정
     *
     * @param userDetails
     * @param params      - 계정 수정 정보 DTO
     * @return
     */
    @PostMapping("/profile-update")
    @Tag(name = "Member API")
    @Operation(summary = "프로필 수정", description = "프로필 수정을 수행합니다.")
    fun profileUpdate(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody params: MemberUpdateRequestDto
    ): ResponseEntity<String> {
        //로그인 정보(UserDetails)의 ID와 계정 수정 정보(MemberUpdateRequestDto)의 ID 비교
        return if (params.id == (userDetails as CustomUserDetails).id) {
            memberService.update(params)
            ResponseEntity.ok("Member updated successfully.")
        } else
            ResponseEntity("Member update denied.", HttpStatus.UNAUTHORIZED)
    }
    
    /**
     * POST: 회원 탈퇴
     *
     * @param userDetails
     * @param params      - 회원 탈퇴 정보 DTO
     * @return
     */
    @PostMapping("/delete")
    @Tag(name = "Member API")
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 수행합니다.")
    fun deleteAccount(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody params: MemberUpdateRequestDto
    ): ResponseEntity<String> {
        //로그인 정보(UserDetails)의 ID와 계정 탈퇴 정보(MemberUpdateRequestDto)의 ID 비교
        return if (params.id == (userDetails as CustomUserDetails).id) {
            memberService.delete(params.id!!, params.password)
            val httpHeaders = HttpHeaders()
            httpHeaders.add("Location", "/logout")
            ResponseEntity("Member deleted successfully.", httpHeaders, HttpStatus.FOUND)
        } else
            ResponseEntity("Member delete denied.", HttpStatus.UNAUTHORIZED)
    }
    
}