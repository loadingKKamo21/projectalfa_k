package com.project.alfa.utils

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Component
@EnableAsync
class EmailSender(private val mailSender: JavaMailSender) {
    
    @Value("\${email.from}")
    private lateinit var fromAddress: String
    
    @Value("\${app.frontend.url}")
    private lateinit var url: String
    
    /**
     * 인증 메일 전송
     *
     * @param email      - 메일 주소
     * @param authToken  - 인증 토큰
     * @param expireTime - 인증 만료 제한 시간
     */
    @Async
    fun sendVerificationEmail(email: String, authToken: String, expireTime: LocalDateTime): Unit {
        val smm = createMailMessage(email)
        smm.subject = "이메일 인증"
        smm.text = "계정 인증을 완료하기 위해 제한 시간 내 다음 링크를 클릭해주세요.\n" +
                "인증 만료 제한 시간: ${expireTime.format(DateTimeFormatter.ofPattern(" yyyy -MM - dd hh : mm : ss "))}\n" +
                "$url/verify-email?email=$email&authToken=$authToken"
        
        mailSender.send(smm)
    }
    
    /**
     * 비밀번호 찾기 결과 메일 전송
     *
     * @param email        - 메일 주소
     * @param tempPassword - 임시 비밀번호
     */
    @Async
    fun sendPasswordResetEmail(email: String, tempPassword: String): Unit {
        val smm = createMailMessage(email)
        smm.subject = "비밀번호 찾기 결과"
        smm.text = "입력하신 정보로 찾은 계정의 임시 비밀번호는 다음과 같습니다.\n" +
                "임시 비밀번호: $tempPassword\n" +
                "임시 비밀번호로 로그인한 다음 비밀번호를 변경해주세요."
        
        mailSender.send(smm)
    }
    
    /**
     * 메일 객체(SimpleMailMessage) 생성
     *
     * @param email - 메일 주소
     * @return 메일 객체
     */
    private fun createMailMessage(email: String): SimpleMailMessage {
        val smm = SimpleMailMessage()
        smm.from = fromAddress
        smm.setTo(email)
        return smm
    }
    
}