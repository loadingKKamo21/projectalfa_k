package com.project.alfa

import com.project.alfa.entities.*
import com.project.alfa.repositories.v1.CommentRepositoryV1
import com.project.alfa.repositories.v1.MemberRepositoryV1
import com.project.alfa.repositories.v1.PostRepositoryV1
import com.project.alfa.repositories.v2.CommentRepositoryV2
import com.project.alfa.repositories.v2.MemberRepositoryV2
import com.project.alfa.repositories.v2.PostRepositoryV2
import com.project.alfa.repositories.v3.CommentRepositoryV3
import com.project.alfa.repositories.v3.MemberRepositoryV3
import com.project.alfa.repositories.v3.PostRepositoryV3
import com.project.alfa.utils.RandomGenerator.Companion.randomHangul
import com.project.alfa.utils.RandomGenerator.Companion.randomNumber
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.random.Random

//@Component
class InitDb(
        private val memberRepository: MemberRepositoryV1,
        //private val memberRepository: MemberRepositoryV2,
        //private val memberRepository: MemberRepositoryV3,
        private val postRepository: PostRepositoryV1,
        //private val postRepository: PostRepositoryV2,
        //private val postRepository: PostRepositoryV3,
        private val commentRepository: CommentRepositoryV1,
        //private val commentRepository: CommentRepositoryV2,
        //private val commentRepository: CommentRepositoryV3,
        private val passwordEncoder: PasswordEncoder
) {
    
    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun init() {
        createMembers(1000)
        createPosts(memberRepository.findAll(), 5000)
        createComments(memberRepository.findAll(), postRepository.findAll(), 10000)
    }
    
    private fun createMembers(size: Int) {
        for (i in 1..size) {
            Thread.sleep(1)
            val num = String.format("%0" + size.toString().length + "d", i)
            val member = Member(username = "user$num@mail.com",
                                password = passwordEncoder.encode("Password$num!@"),
                                authInfo = AuthInfo(emailAuthToken = UUID.randomUUID().toString()),
                                nickname = "user$num",
                                role = Role.USER)
            memberRepository.save(member)
            member.authenticate()
        }
    }
    
    private fun createPosts(writers: List<Member>, size: Int) {
        for (i in 1..size) {
            Thread.sleep(1)
            val post = Post(writer = writers[Random.nextInt(writers.size)],
                            title = randomHangul(randomNumber(1, 100)),
                            content = randomHangul(randomNumber(100, 500)),
                            noticeYn = false)
            postRepository.save(post)
        }
    }
    
    private fun createComments(writers: List<Member>, posts: List<Post>, size: Int) {
        for (i in 1..size) {
            Thread.sleep(1)
            val comment = Comment(writer = writers[Random.nextInt(writers.size)],
                                  post = posts[Random.nextInt(posts.size)],
                                  content = randomHangul(randomNumber(1, 100)))
            commentRepository.save(comment)
        }
    }
    
}