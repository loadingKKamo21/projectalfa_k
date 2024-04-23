package com.project.alfa.utils

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component
import java.util.*

@Component
@EnableAsync
class EmailSender(private val mailSender: JavaMailSender) {
    
    private val log: KLogger = KotlinLogging.logger { }
    
    /**
     * 이메일 전송
     *
     * @param email   - 이메일 주소
     * @param subject - 제목
     * @param content - 내용
     */
    @Async
    fun send(email: String, subject: String, content: String) {
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        
        log.info { "[$uuid] ==> Start sending mail" }
        log.info { "[$uuid] To: $email" }
        log.info { "[$uuid] Subject: $subject" }
        log.info { "[$uuid] Content: $content" }
        log.info { "[$uuid] Content.length: ${content.length}" }
        
        val smm = SimpleMailMessage().apply {
            setTo(email)
            this.subject = subject
            text = content
        }
        
        mailSender.send(smm)
        
        log.info { "[$uuid] <== Complete sending mail" }
    }
}