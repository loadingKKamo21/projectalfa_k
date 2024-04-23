package com.project.alfa

import com.project.alfa.entities.*
import com.project.alfa.repositories.mybatis.CommentMapper
import com.project.alfa.repositories.mybatis.MemberMapper
import com.project.alfa.repositories.mybatis.PostMapper
import com.project.alfa.utils.RandomGenerator.Companion.randomHangul
import com.project.alfa.utils.RandomGenerator.Companion.randomNumber
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

//@Component
class InitDb(
        private val memberMapper: MemberMapper,
        private val postMapper: PostMapper,
        private val commentMapper: CommentMapper,
        private val passwordEncoder: PasswordEncoder
) {
    
    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun init() {
        createMembers(1000)
        createPosts(memberMapper.findAll(), 5000)
        createComments(memberMapper.findAll(), postMapper.findAll(), 10000)
    }
    
    private fun createMembers(size: Int) {
        for (i in 1..size) {
            Thread.sleep(1)
            val num = String.format("%0" + size.toString().length + "d", i)
            val member = Member(username = "user$num@mail.com",
                                password = passwordEncoder.encode("Password$num!@"),
                                authInfo = AuthInfo(emailAuthToken = UUID.randomUUID().toString(),
                                                    emailAuthExpireTime = LocalDateTime.now().withNano(0)
                                                            .plusMinutes(5)),
                                nickname = "user$num",
                                role = Role.USER)
            memberMapper.save(member)
            memberMapper.authenticateEmail(member.username,
                                           member.authInfo.emailAuthToken!!,
                                           LocalDateTime.now())
        }
    }
    
    private fun createPosts(writers: List<Member>, size: Int) {
        for (i in 1..size) {
            Thread.sleep(1)
            val post = Post(writerId = writers[Random.nextInt(writers.size)].id!!,
                            title = randomHangul(randomNumber(1, 100)),
                            content = randomHangul(randomNumber(100, 500)),
                            noticeYn = false)
            postMapper.save(post)
        }
    }
    
    private fun createComments(writers: List<Member>, posts: List<Post>, size: Int) {
        for (i in 1..size) {
            Thread.sleep(1)
            val comment = Comment(writerId = writers[Random.nextInt(writers.size)].id!!,
                                  postId = posts[Random.nextInt(posts.size)].id!!,
                                  content = randomHangul(randomNumber(1, 100)))
            commentMapper.save(comment)
        }
    }
    
}