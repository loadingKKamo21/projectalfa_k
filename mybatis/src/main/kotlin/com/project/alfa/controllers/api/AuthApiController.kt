package com.project.alfa.controllers.api

import com.project.alfa.services.JwtService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping(
    value = ["/api/auth"],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@Tag(name = "Auth API", description = "계정 인증 API 입니다.")
class AuthApiController(private val jwtService: JwtService) {
    
    /**
     * POST: JWT Access 토큰 Refresh
     *
     * @param request
     * @param response
     * @param body
     * @param userDetails
     * @return
     */
    @PostMapping("/refresh")
    @Tag(name = "Auth API")
    @Operation(summary = "JWT Token refresh", description = "JWT 토큰을 리프레쉬합니다.")
    fun refreshToken(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestBody(required = false) body: Map<String, String>?,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<String> {
        var refreshToken = getRefreshToken(request, body)
        if (!refreshToken.isNullOrBlank()) {
            val accessToken = jwtService.refreshAccessToken(refreshToken, userDetails)
            refreshToken = jwtService.generateRefreshToken(userDetails)
            response.setHeader("Authorization", "Bearer $accessToken")
            
            //1. RefreshToken 쿠키로 전달
            val cookie = Cookie("refreshToken", refreshToken)
            cookie.isHttpOnly = true
            cookie.path = "/"
            cookie.maxAge = jwtService.getExpirationFromToken(refreshToken).toInt()
            response.addCookie(cookie)
            
            return ResponseEntity.ok("Access Token refresh complete.")
            
            //2. RefreshToken JSON으로 전달
//            return ResponseEntity.ok(Gson().toJson(refreshToken))
        }
        return ResponseEntity.badRequest().body("Refresh Token is missing.")
    }
    
    /**
     * JWT Refresh 토큰 추출
     *
     * @param request
     * @param body
     * @return JWT Refresh 토큰
     */
    private fun getRefreshToken(request: HttpServletRequest, body: Map<String, String>?): String? {
        //1. 쿠키에서 RefreshToken 추출
        request.cookies?.forEach {
            if (it.name == "refreshToken") return it.value
        }
        
        //2. 헤더에서 RefreshToken 추출
        request.getHeader("Authorization")?.let {
            if (it.startsWith("Refresh ")) return it.substring(8)
        }
        
        //3. JSON에서 RefreshToken 추출
        body?.get("refreshToken")?.let { return it }
        
        return null
    }
}
