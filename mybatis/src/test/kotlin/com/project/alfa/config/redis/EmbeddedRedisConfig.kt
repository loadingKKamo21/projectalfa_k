package com.project.alfa.config.redis

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import redis.embedded.RedisServer
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Profile("test")
@TestConfiguration
class EmbeddedRedisConfig {
    
    private var port: Int = 6379
    private var redisServer: RedisServer? = null
    private val log: KLogger = KotlinLogging.logger { }
    
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory = LettuceConnectionFactory(
            RedisStandaloneConfiguration("localhost", port)
    )
    
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
    
    @PostConstruct
    fun startRedis() {
        port = RandomPort.getRandomAvailablePort(1024, 49151)
        redisServer = RedisServer(port)
        redisServer?.start()
        log.info { "Start EmbeddedRedis($port)" }
    }
    
    @PreDestroy
    fun stopRedis() {
        redisServer?.stop()
        log.info { "Stop EmbeddedRedis($port)" }
    }
    
}