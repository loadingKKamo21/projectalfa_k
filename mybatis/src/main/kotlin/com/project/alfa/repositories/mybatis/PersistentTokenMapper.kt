package com.project.alfa.repositories.mybatis

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken
import java.util.*

@Mapper
interface PersistentTokenMapper {
    
    fun createNewToken(token: PersistentRememberMeToken): Unit
    
    fun updateToken(@Param("series") series: String,
                    @Param("tokenValue") tokenValue: String,
                    @Param("lastUsed") lastUsed: Date): Unit
    
    fun getTokenForSeries(series: String): PersistentRememberMeToken
    
    fun removeUserTokens(username: String): Unit
    
}