package com.project.alfa.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

class CustomUserDetails(
        val id: Long,
        private val username: String,
        private val password: String,
        val auth: Boolean,
        val nickname: String,
        val role: String
) : UserDetails, OAuth2User {
    
    constructor(
            id: Long,
            username: String,
            password: String,
            auth: Boolean,
            nickname: String,
            role: String,
            attributes: MutableMap<String, Any>
    ) : this(id, username, password, auth, nickname, role) {
        this.attributes = attributes
    }
    
    private var attributes: MutableMap<String, Any> = HashMap()
    
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        val authorities: MutableCollection<SimpleGrantedAuthority> = ArrayList()
        authorities.add(SimpleGrantedAuthority(role))
        return authorities
    }
    
    override fun getPassword(): String = password
    
    override fun getUsername(): String = username
    
    override fun isAccountNonExpired(): Boolean = true
    
    override fun isAccountNonLocked(): Boolean = auth
    
    override fun isCredentialsNonExpired(): Boolean = true
    
    override fun isEnabled(): Boolean = true
    
    override fun getName(): String = username
    
    override fun getAttributes(): MutableMap<String, Any> = attributes
    
}