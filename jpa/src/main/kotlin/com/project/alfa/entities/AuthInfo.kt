package com.project.alfa.entities

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Embeddable

const val MAX_EXPIRE_TIME: Long = 5L    //이메일 인증 만료 제한 시간

@Embeddable
class AuthInfo(emailAuthToken: String? = null, oAuthProvider: String? = null, oAuthProviderId: String? = null) {
    
    @Column(nullable = false)
    var auth: Boolean = false                                               //인증 여부
        protected set
    var authenticatedTime: LocalDateTime? = null                            //계정 인증 시각
        protected set
    var emailAuthToken: String? = emailAuthToken                            //이메일 인증 토큰
        protected set
    var emailAuthExpireTime: LocalDateTime? = if (emailAuthToken != null)   //이메일 인증 만료 시간
        LocalDateTime.now().withNano(0).plusMinutes(MAX_EXPIRE_TIME) else null
        protected set
    
    @Column(updatable = false, nullable = true)
    var oAuthProvider: String? = oAuthProvider                              //OAuth 2.0 Provider
        protected set
    
    @Column(updatable = false, nullable = true)
    var oAuthProviderId: String? = oAuthProviderId                          //OAuth 2.0 Provider Id
        protected set
    
    //==================== 인증 관련 메서드 ====================//
    
    /**
     * 인증 완료
     */
    internal fun authComplete(): Unit {
        auth = true
        authenticatedTime = LocalDateTime.now()
    }
    
    /**
     * 이메일 인증 토큰 변경
     *
     * @param newEmailAuthToken - 새로운 이메일 인증 토큰
     */
    internal fun updateEmailAuthToken(newEmailAuthToken: String) {
        auth = false
        authenticatedTime = null
        emailAuthToken = newEmailAuthToken
        emailAuthExpireTime = LocalDateTime.now().withNano(0).plusMinutes(MAX_EXPIRE_TIME)
    }
    
}
