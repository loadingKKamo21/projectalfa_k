package com.project.alfa.security.oauth2.provider

interface OAuth2UserInfo {
    
    fun getProvider(): String
    
    fun getProviderId(): String
    
    fun getEmail(): String
    
}