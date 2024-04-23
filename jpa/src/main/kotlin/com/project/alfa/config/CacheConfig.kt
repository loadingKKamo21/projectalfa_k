package com.project.alfa.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachingConfigurerSupport
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.cache.interceptor.SimpleKeyGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@EnableCaching
@Configuration
class CacheConfig : CachingConfigurerSupport() {
    
    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val objectMapper = ObjectMapper().apply {
            registerModule(JavaTimeModule())
            activateDefaultTyping(BasicPolymorphicTypeValidator.builder().allowIfSubType(Any::class.java).build(),
                                  ObjectMapper.DefaultTyping.NON_FINAL)
        }
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(
                        RedisCacheConfiguration.defaultCacheConfig(Thread.currentThread().contextClassLoader)
                                .entryTtl(Duration.ofHours(1))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                        StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                        GenericJackson2JsonRedisSerializer(objectMapper)))
                ).transactionAware().build()
    }
    
    override fun keyGenerator(): KeyGenerator? = SimpleKeyGenerator()
    
    @Bean
    fun customKeyGenerator(): KeyGenerator? = KeyGenerator { target, method, params ->
        val sb = StringBuilder()
        params.forEach {
            if (sb.isNotEmpty()) sb.append(",")
            sb.append(it.toString())
        }
        return@KeyGenerator sb.toString()
    }
    
}