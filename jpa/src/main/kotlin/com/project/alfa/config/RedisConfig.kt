package com.project.alfa.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Profile("default")
@Configuration
class RedisConfig {
    
    @Value("\${spring.redis.host}")
    private lateinit var host: String
    
    @Value("\${spring.redis.port}")
    private var port: Int = 6379
    
    @Value("\${spring.redis.password}")
    private lateinit var password: String
    
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration(host, port)
        redisStandaloneConfiguration.setPassword(password)
        return LettuceConnectionFactory(redisStandaloneConfiguration)
    }
    
    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.setConnectionFactory(redisConnectionFactory())
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
        redisTemplate.hashKeySerializer = StringRedisSerializer()
        redisTemplate.hashValueSerializer = GenericJackson2JsonRedisSerializer()
        return redisTemplate
    }
    
    @Bean
    fun stringRedisTemplate(): StringRedisTemplate {
        val stringRedisTemplate = StringRedisTemplate()
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory())
        stringRedisTemplate.keySerializer = StringRedisSerializer()
        stringRedisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
        stringRedisTemplate.hashKeySerializer = StringRedisSerializer()
        stringRedisTemplate.hashValueSerializer = GenericJackson2JsonRedisSerializer()
        return stringRedisTemplate
    }
    
}