package com.project.alfa.config

import com.project.alfa.entities.*
import com.project.alfa.repositories.mybatis.AttachmentMapper
import com.project.alfa.repositories.mybatis.CommentMapper
import com.project.alfa.repositories.mybatis.MemberMapper
import com.project.alfa.repositories.mybatis.PostMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

class DummyGenerator {
    
    val CHARACTERS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    
    @Autowired
    lateinit var memberMapper: MemberMapper
    
    @Autowired
    lateinit var postMapper: PostMapper
    
    @Autowired
    lateinit var commentMapper: CommentMapper
    
    @Autowired
    lateinit var attachmentMapper: AttachmentMapper
    
    @Autowired
    lateinit var passwordEncoder: PasswordEncoder
    
    fun generateRandomString(length: Int): String {
        val sb = StringBuilder(length)
        for (i in 1..length)
            sb.append(CHARACTERS[Random.nextInt(CHARACTERS.length)])
        return sb.toString()
    }
    
    fun generateRandomNumber(min: Int, max: Int): Int = Random.nextInt(max - min + 1) + min
    
    fun createMembers(size: Int, save: Boolean): List<Member> {
        val list: MutableList<Member> = ArrayList()
        for (i in 1..size) {
            val num = String.format("%0" + size.toString().length + "d", i)
            val member = Member(username = "user$num@mail.com",
                                password = passwordEncoder.encode("Password$num!@"),
                                authInfo = AuthInfo(auth = false,
                                                    emailAuthToken = UUID.randomUUID().toString(),
                                                    emailAuthExpireTime = LocalDateTime.now().withNano(0)
                                                            .plusMinutes(5)),
                                nickname = "user$num",
                                role = Role.USER)
            if (save)
                memberMapper.save(member)
            list.add(member)
        }
        return list
    }
    
    fun createPosts(writers: List<Member>, size: Int, save: Boolean): List<Post> {
        val list: MutableList<Post> = ArrayList()
        for (i in 1..size) {
            Thread.sleep(1)
            val post = Post(writerId = writers[Random.nextInt(writers.size)].id!!,
                            title = generateRandomString(generateRandomNumber(1, 100)),
                            content = generateRandomString(generateRandomNumber(100, 500)),
                            noticeYn = false)
            if (save)
                postMapper.save(post)
            list.add(post)
        }
        return list
    }
    
    fun randomlyDeletePosts(posts: List<Post>, count: Int) {
        var deleteCount = 0
        while (count > 0) {
            if (count == deleteCount)
                break
            val post = posts[Random.nextInt(posts.size)]
            if (post.deleteYn)
                continue
            val id = post.id!!
            val writerId = post.writerId
            postMapper.deleteById(id, writerId)
            deleteCount++
        }
    }
    
    fun createComments(writers: List<Member>, posts: List<Post>, size: Int, save: Boolean): List<Comment> {
        val list: MutableList<Comment> = ArrayList()
        for (i in 1..size) {
            Thread.sleep(1)
            val comment = Comment(writerId = writers[Random.nextInt(writers.size)].id!!,
                                  postId = posts[Random.nextInt(posts.size)].id!!,
                                  content = generateRandomString(generateRandomNumber(1, 100)))
            if (save)
                commentMapper.save(comment)
            list.add(comment)
        }
        return list
    }
    
    fun randomlyDeleteComments(comments: List<Comment>, count: Int) {
        var deleteCount = 0
        while (count > 0) {
            if (count == deleteCount)
                break
            val comment = comments[Random.nextInt(comments.size)]
            if (comment.deleteYn)
                continue
            val id = comment.id!!
            val writerId = comment.writerId
            commentMapper.deleteById(id, writerId)
            deleteCount++
        }
    }
    
    fun createAttachments(posts: List<Post>, size: Int, save: Boolean): List<Attachment> {
        val list: MutableList<Attachment> = ArrayList()
        for (i in 1..size) {
            Thread.sleep(1)
            val attachment = Attachment(postId = posts[Random.nextInt(posts.size)].id!!,
                                        originalFilename = generateRandomString(generateRandomNumber(1, 10)),
                                        storeFilename = generateRandomString(generateRandomNumber(1, 10)),
                                        storeFilePath = generateRandomString(generateRandomNumber(10, 100)),
                                        fileSize = generateRandomNumber(1, 1000000).toLong())
            if (save)
                attachmentMapper.save(attachment)
            list.add(attachment)
        }
        return list
    }
    
    fun randomlyDeleteAttachments(attachments: List<Attachment>, count: Int) {
        var deleteCount = 0
        while (count > 0) {
            if (count == deleteCount)
                break
            val attachment = attachments[Random.nextInt(attachments.size)]
            if (attachment.deleteYn)
                continue
            val id = attachment.id!!
            val postId = attachment.postId
            attachmentMapper.deleteById(id, postId)
            deleteCount++
        }
    }
    
}