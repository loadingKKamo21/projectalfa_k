package com.project.alfa.security.oauth2

import com.project.alfa.entities.AuthInfo
import com.project.alfa.entities.Member
import com.project.alfa.entities.Role
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.repositories.MemberRepository
import com.project.alfa.security.CustomUserDetails
import com.project.alfa.security.oauth2.provider.GoogleUserInfo
import com.project.alfa.security.oauth2.provider.OAuth2UserInfo
import com.project.alfa.utils.RandomGenerator.Companion.randomPassword
import com.project.alfa.utils.RandomGenerator.Companion.randomString
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CustomOAuth2UserService(
        val memberRepository: MemberRepository,
        val passwordEncoder: PasswordEncoder
) : DefaultOAuth2UserService() {
    
    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        
        val clientName = userRequest!!.clientRegistration.clientName
        var oAuth2UserInfo: OAuth2UserInfo? = null
        
        //OAuth 2.0 Provider 구분
        when (clientName) {
            "Google" -> oAuth2UserInfo = GoogleUserInfo(oAuth2User.attributes)
            else -> {}
        }
        
        println("==================================================")
        println("clientName: $clientName")
        println("oAuth2UserInfo: $oAuth2UserInfo")
        println("==================================================")
        
        val username = "${oAuth2UserInfo!!.getProvider()}_${oAuth2UserInfo.getProviderId()}"
        
        val member: Member?
        if (!memberRepository.existsByUsername(username, false)) {
            var nickname: String
            do {
                nickname = "${oAuth2User.getAttribute<String>("name")}_${randomString(10)}"
            } while (memberRepository.existsByNickname(nickname))
            
            member = Member(username = username.lowercase(),
                            password = passwordEncoder.encode(randomPassword(20)),
                            authInfo = AuthInfo(oAuthProvider = oAuth2UserInfo.getProvider(),
                                                oAuthProviderId = oAuth2UserInfo.getProviderId()),
                            nickname = nickname,
                            role = Role.USER)
            
            memberRepository.save(member)
            memberRepository.authenticateOAuth(member.username,
                                               member.authInfo.oAuthProvider!!,
                                               member.authInfo.oAuthProviderId!!,
                                               LocalDateTime.now())
        } else
            member = memberRepository.findByUsername(username, false).orElseThrow {
                EntityNotFoundException("Could not found 'Member' by username: $username")
            }
        
        return CustomUserDetails(member!!.id!!,
                                 member.username,
                                 member.password,
                                 member.authInfo.auth,
                                 member.nickname,
                                 member.role!!.value,
                                 oAuth2User.attributes)
    }
    
}