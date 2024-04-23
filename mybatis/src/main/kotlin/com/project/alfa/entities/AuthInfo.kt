package com.project.alfa.entities

import java.time.LocalDateTime

class AuthInfo {
    
    private constructor() : this(false, null, null, null, null)
    
    constructor(
            auth: Boolean = false,
            emailAuthToken: String? = null,
            emailAuthExpireTime: LocalDateTime? = null,
            oAuthProvider: String? = null,
            oAuthProviderId: String? = null
    ) {
        this.auth = auth
        this.authenticatedTime = null
        this.emailAuthToken = emailAuthToken
        this.emailAuthExpireTime = emailAuthExpireTime
        this.oAuthProvider = oAuthProvider
        this.oAuthProviderId = oAuthProviderId
    }
    
    var auth: Boolean                       //인증 여부
        private set
    var authenticatedTime: LocalDateTime?   //계정 인증 시각
        private set
    var emailAuthToken: String?             //이메일 인증 토큰
        private set
    var emailAuthExpireTime: LocalDateTime? //이메일 인증 만료 시간
        private set
    var oAuthProvider: String?              //OAuth 2.0 Provider
        private set
    var oAuthProviderId: String?            //OAuth 2.0 Provider Id
        private set
    
}
