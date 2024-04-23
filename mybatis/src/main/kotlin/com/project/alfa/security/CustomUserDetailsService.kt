package com.project.alfa.security

import com.project.alfa.repositories.MemberRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val memberRepository: MemberRepository) : UserDetailsService {
    
    override fun loadUserByUsername(username: String?): UserDetails {
        val member = memberRepository.findByUsername(username!!.lowercase(), false).orElseThrow {
            UsernameNotFoundException("Could not found 'Member' by username: ${username.lowercase()}")
        }
        return CustomUserDetails(member.id!!,
                                 member.username,
                                 member.password,
                                 member.authInfo.auth,
                                 member.nickname,
                                 member.role!!.value)
    }
    
}