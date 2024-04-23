package com.project.alfa.repositories.mybatis

import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository
import java.util.*

open class MyBatisTokenRepositoryImpl(
        private val persistentTokenMapper: PersistentTokenMapper
) : PersistentTokenRepository {
    
    override fun createNewToken(token: PersistentRememberMeToken?) = persistentTokenMapper.createNewToken(token!!)
    
    override fun updateToken(series: String?, tokenValue: String?, lastUsed: Date?) =
            persistentTokenMapper.updateToken(series!!, tokenValue!!, lastUsed!!)
    
    override fun getTokenForSeries(seriesId: String?): PersistentRememberMeToken =
            persistentTokenMapper.getTokenForSeries(seriesId!!)
    
    override fun removeUserTokens(username: String?) = persistentTokenMapper.removeUserTokens(username!!)
    
}