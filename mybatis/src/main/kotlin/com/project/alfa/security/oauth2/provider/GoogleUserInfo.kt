package com.project.alfa.security.oauth2.provider

class GoogleUserInfo(val attributes: Map<String, Any>) : OAuth2UserInfo {
    
    override fun getProvider(): String = "google"
    
    override fun getProviderId(): String = attributes["sub"].toString()
    
    override fun getEmail(): String = attributes["email"].toString()
    
}