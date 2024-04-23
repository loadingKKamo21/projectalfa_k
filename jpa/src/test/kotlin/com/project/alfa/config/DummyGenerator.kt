package com.project.alfa.config

import com.project.alfa.entities.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
import kotlin.random.Random

class DummyGenerator {
    
    val CHARACTERS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    
    @Autowired
    lateinit var passwordEncoder: PasswordEncoder
    
    fun generateRandomString(length: Int): String {
        val sb = StringBuilder(length)
        for (i in 1..length)
            sb.append(CHARACTERS[Random.nextInt(CHARACTERS.length)])
        return sb.toString()
    }
    
    fun generateRandomNumber(min: Int, max: Int): Int = Random.nextInt(max - min + 1) + min
    
    fun createMembers(size: Int): List<Member> {
        val list: MutableList<Member> = ArrayList()
        for (i in 1..size) {
            val num = String.format("%0" + size.toString().length + "d", i)
            val member = Member(username = "user$num@mail.com",
                                password = passwordEncoder.encode("Password$num!@"),
                                authInfo = AuthInfo(emailAuthToken = UUID.randomUUID().toString()),
                                nickname = "user$num",
                                role = Role.USER)
            list.add(member)
        }
        return list
    }
    
    fun createPosts(writers: List<Member>, size: Int): List<Post> {
        val list: MutableList<Post> = ArrayList()
        for (i in 1..size) {
            val post = Post(writer = writers[Random.nextInt(writers.size)],
                            title = generateRandomString(generateRandomNumber(1, 100)),
                            content = generateRandomString(generateRandomNumber(100, 500)),
                            noticeYn = false)
            list.add(post)
        }
        return list
    }
    
    fun createComments(writers: List<Member>, posts: List<Post>, size: Int): List<Comment> {
        val list: MutableList<Comment> = ArrayList()
        for (i in 1..size) {
            val comment = Comment(writer = writers[Random.nextInt(writers.size)],
                                  post = posts[Random.nextInt(posts.size)],
                                  content = generateRandomString(generateRandomNumber(1, 100)))
            list.add(comment)
        }
        return list
    }
    
    fun createAttachments(posts: List<Post>, size: Int): List<Attachment> {
        val list: MutableList<Attachment> = ArrayList()
        for (i in 1..size) {
            val attachment = Attachment(post = posts[Random.nextInt(posts.size)],
                                        originalFilename = generateRandomString(generateRandomNumber(1, 10)),
                                        storeFilename = generateRandomString(generateRandomNumber(1, 10)),
                                        storeFilePath = generateRandomString(generateRandomNumber(10, 100)),
                                        fileSize = generateRandomNumber(1, 1000000).toLong())
            list.add(attachment)
        }
        return list
    }
    
}