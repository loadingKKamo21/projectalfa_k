package com.project.alfa.config

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.project.alfa.security.jwt.entrypoint.JwtAuthenticationEntryPoint
import com.project.alfa.security.jwt.filter.JwtAuthenticationFilter
import com.project.alfa.security.jwt.filter.JwtRequestFilter
import com.project.alfa.services.JwtService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.GET
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.sql.DataSource

@Configuration
class SecurityConfig(
        private val dataSource: DataSource,
        private val userDetailsService: UserDetailsService,
        private val oAuth2UserService: OAuth2UserService<OAuth2UserRequest, OAuth2User>,
        private val authenticationManager: AuthenticationManager,
        private val authenticationProvider: AuthenticationProvider,
        private val authenticationFailureHandler: AuthenticationFailureHandler,
        private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
        private val jwtService: JwtService
) {
    
    @Bean
    fun tokenRepository(): PersistentTokenRepository {
        val tokenRepository = JdbcTokenRepositoryImpl()
        tokenRepository.setDataSource(dataSource)
        return tokenRepository
    }
    
    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer = WebSecurityCustomizer { web ->
        web.ignoring().antMatchers("/css/**")
    }
    
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        //CSRF 비활성화
        http.csrf().disable()
        
        //URL
        http.authorizeRequests()
                .regexMatchers(GET, "/api/posts/(?:\\d+)?$", "/api/posts/\\d+/attachments$", "/api/posts/\\d+/attachments/\\d+/download$").permitAll()
                .regexMatchers(GET, "/api/posts\\??(&?(?:page=\\d+)?)(&?(?:size=\\d+)?)(&?(?:condition=(title|content|titleOrContent|writer)?)?)(&?(?:keyword=.*)?)$").permitAll()
                .regexMatchers(GET, "/api/posts/\\d+/comments\\??(&?(?:page=\\d+)?)(&?(?:size=\\d+)?)$").permitAll()
                .mvcMatchers("/api/members", "/api/members/forgot-password").permitAll()
                .mvcMatchers("/api/members/**", "/logout", "/api/posts/**", "/api/posts/*/comments/**", "/api/comments/**", "/api/auth/refresh").authenticated()
                .anyRequest().permitAll()
        
        //커스텀 AuthenticationProvider
        http.authenticationProvider(authenticationProvider)
        
        //로그인 설정
//        http.httpBasic()
//                .and()
//                .formLogin()
//                .loginPage("/login")
//                .loginProcessingUrl("/login-process")
//                .defaultSuccessUrl("/", true)
//                .failureHandler(authenticationFailureHandler)
        http.httpBasic().disable().formLogin().disable()
        
        //세션 비활성화
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        
        //JWT 인증 필터
        val jwtRequestFilter = JwtRequestFilter(userDetailsService, jwtService)
        val jwtAuthenticationFilter = JwtAuthenticationFilter(authenticationManager, jwtService)
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        
        //JWT 인증 엔트리포인트
        http.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
        
        //remember-me 설정
//        http.rememberMe()
//                .rememberMeParameter("remember-me")
//                .tokenValiditySeconds(86400 * 14)
//                .alwaysRemember(false)
//                .userDetailsService(userDetailsService)
//                .tokenRepository(tokenRepository())
        http.rememberMe().disable()
        
        //로그아웃
        http.logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout")
                .logoutSuccessHandler { request, response, authentication ->
                    if (request.session != null)
                        request.session.invalidate()
                    
                    SecurityContextHolder.clearContext()
                    
                    val refreshToken = getRefreshToken(request)
                    if (!refreshToken.isNullOrBlank()) {
                        jwtService.deleteRefreshToken(refreshToken)
                        response.status = HttpServletResponse.SC_OK
                        response.writer.write("Logout successful.")
                    } else {
                        response.status = HttpServletResponse.SC_UNAUTHORIZED
                        response.writer.write("Invalid Refresh Token.")
                    }
                    response.writer.flush()

//                    val jSessionIdCookie = Cookie("JSESSIONID", null)
//                    jSessionIdCookie.path = "/"
//                    jSessionIdCookie.isHttpOnly = true
//                    jSessionIdCookie.maxAge = 0
//                    response.addCookie(jSessionIdCookie)
//
//                    val rememberMeCookie = Cookie("remember-me", null)
//                    rememberMeCookie.path = "/"
//                    rememberMeCookie.isHttpOnly = true
//                    rememberMeCookie.maxAge = 0
//                    response.addCookie(rememberMeCookie)
                }
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
        
        //OAuth 2.0 로그인
        http.oauth2Login()
                .loginPage("/login")
                .userInfoEndpoint()
                .userService(oAuth2UserService)
        
        return http.build()
    }
    
    /**
     * JWT Refresh 토큰 추출
     *
     * @param request
     * @return JWT Refresh 토큰
     */
    private fun getRefreshToken(request: HttpServletRequest): String? {
        //1. 쿠키에서 RefreshToken 추출
        request.cookies?.forEach {
            if (it.name == "refreshToken") return it.value
        }
        
        //2. 헤더에서 RefreshToken 추출
        request.getHeader("Authorization")?.let {
            if (it.startsWith("Refresh ")) return it.substring(8)
        }
        
        //3. JSON에서 RefreshToken 추출
        val sb = StringBuilder()
        val gson = Gson()
        request.reader.use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null)
                sb.append(line)
        }
        val body: Map<String, String> = gson.fromJson(sb.toString(), object : TypeToken<Map<String, String>>() {}.type)
        body["refreshToken"]?.let { return it }
        
        return null
    }
    
}