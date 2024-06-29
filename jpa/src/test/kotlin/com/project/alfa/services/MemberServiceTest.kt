package com.project.alfa.services

import com.icegreen.greenmail.configuration.GreenMailConfiguration
import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.ServerSetup
import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.entities.Member
import com.project.alfa.entities.Role
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.error.exception.InvalidValueException
import com.project.alfa.services.dto.MemberJoinRequestDto
import com.project.alfa.services.dto.MemberUpdateRequestDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import javax.mail.Message
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Import(TestConfig::class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class MemberServiceTest {
    
    companion object {
        @RegisterExtension
        val greenMailExtension: GreenMailExtension = GreenMailExtension(ServerSetup(3025, null, "smtp"))
                .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
                .withPerMethodLifecycle(true)
    }
    
    @Autowired
    lateinit var memberService: MemberService
    
    @Autowired
    lateinit var passwordEncoder: PasswordEncoder
    
    @PersistenceContext
    lateinit var em: EntityManager
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @AfterEach
    fun clear() {
        em.flush()
        em.clear()
    }
    
    @Test
    @DisplayName("회원 가입")
    fun join() {
        //Given
        val dto = MemberJoinRequestDto("user1@mail.com", "Password1!@", "Password1!@", "user1")
        
        //When
        val id = memberService.join(dto)
        clear()
        
        //Then
        greenMailExtension.waitForIncomingEmail(5000, 1)
        val findMember = em.find(Member::class.java, id)
        val receivedMessages = greenMailExtension.receivedMessages
        
        assertThat(dto.username.lowercase()).isEqualTo(findMember.username)
        assertThat(passwordEncoder.matches(dto.password, findMember.password)).isTrue
        assertThat(dto.nickname).isEqualTo(findMember.nickname)
        assertThat(findMember.role).isEqualTo(Role.USER)
        
        assertThat(receivedMessages).hasSize(1)
        assertThat(findMember.username)
                .isEqualTo(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0].toString())
    }
    
    @Test
    @DisplayName("회원 가입, 비밀번호 확인 불일치")
    fun join_wrongPassword() {
        //Given
        val dto = MemberJoinRequestDto("user1@mail.com", "Password1!@", "Password2!@", "user1")
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { memberService.join(dto) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_DO_NOT_MATCH)
                .hasMessage("Invalid input value, Password do not match.")
    }
    
    @Test
    @DisplayName("회원 가입, 아이디 중복")
    fun join_duplicateUsername() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val username = member.username
        val dto = MemberJoinRequestDto(username, "Password2!@", "Password2!@", "user2")
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { memberService.join(dto) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_DUPLICATION)
                .hasMessage("Invalid input value: $username")
    }
    
    @Test
    @DisplayName("회원 가입, 닉네임 중복")
    fun join_duplicateNickname() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val nickname = member.nickname
        val dto = MemberJoinRequestDto("user2@mail.com", "Password2!@", "Password2!@", nickname)
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { memberService.join(dto) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NICKNAME_DUPLICATION)
                .hasMessage("Invalid input value: $nickname")
    }
    
    @Test
    @DisplayName("이메일 인증")
    fun verifyEmailAuth() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        val before = member.authInfo.auth
        
        //When
        memberService.verifyEmailAuth(member.username,
                                      member.authInfo.emailAuthToken!!,
                                      LocalDateTime.now())
        clear()
        
        //Then
        val after = em.find(Member::class.java, id).authInfo.auth
        
        assertThat(before).isFalse
        assertThat(after).isTrue
    }
    
    @Test
    @DisplayName("이메일 인증, 존재하지 않는 계정")
    fun verifyEmailAuth_unknown() {
        //Given
        val username = "user1@mail.com"
        val authToken = UUID.randomUUID().toString()
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { memberService.verifyEmailAuth(username, authToken, LocalDateTime.now()) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Member' by username: $username")
    }
    
    @Test
    @DisplayName("이메일 인증, 이미 인증된 계정")
    fun verifyEmailAuth_completed() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        val authToken = member.authInfo.emailAuthToken!!
        val expireTime = member.authInfo.emailAuthExpireTime
        member.authenticate() //이메일 인증
        
        //When
        Thread.sleep(1000)
        memberService.verifyEmailAuth(member.username, authToken, LocalDateTime.now())
        clear()
        
        //Then
        val findMember = em.find(Member::class.java, id)
        
        //이미 인증된 계정의 경우 인증 토큰, 인증 만료 제한 시간은 변경되지 않음
        assertThat(findMember.authInfo.emailAuthToken).isEqualTo(authToken)
        assertThat(findMember.authInfo.emailAuthExpireTime).isEqualTo(expireTime)
    }
    
    @Test
    @DisplayName("이메일 인증, 잘못된 토큰")
    fun verifyEmailAuth_wrongAuthToken() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        val authToken = member.authInfo.emailAuthToken
        val expireTime = member.authInfo.emailAuthExpireTime
        
        var otherAuthToken: String
        do {
            otherAuthToken = UUID.randomUUID().toString()
        } while (authToken == otherAuthToken)
        
        //When
        Thread.sleep(1000)
        memberService.verifyEmailAuth(member.username, otherAuthToken, LocalDateTime.now())
        clear()
        
        //Then
        greenMailExtension.waitForIncomingEmail(5000, 1)
        val findMember = em.find(Member::class.java, id)
        val receivedMessages = greenMailExtension.receivedMessages
        
        //잘못된 토큰으로 인증을 시도한 경우 새로운 토큰 및 만료 제한 시간으로 인증 메일이 재전송됨
        assertThat(findMember.authInfo.auth).isFalse
        assertThat(findMember.authInfo.emailAuthToken).isNotEqualTo(authToken)
        assertThat(findMember.authInfo.emailAuthExpireTime).isNotEqualTo(expireTime)
        
        assertThat(receivedMessages).hasSize(1)
        assertThat(findMember.username)
                .isEqualTo(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0].toString())
    }
    
    @Test
    @DisplayName("이메일 인증, 만료 제한 시간 초과")
    fun verifyEmailAuth_timeout() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        val authToken = member.authInfo.emailAuthToken!!
        val expireTime = member.authInfo.emailAuthExpireTime!!
        
        //When
        Thread.sleep(1000)
        memberService.verifyEmailAuth(member.username, authToken, expireTime.plusSeconds(1))
        clear()
        
        //Then
        greenMailExtension.waitForIncomingEmail(5000, 1)
        val findMember = em.find(Member::class.java, id)
        val receivedMessages = greenMailExtension.receivedMessages
        
        //인증 만료 제한 시간 초과 후 시도한 경우 새로운 토큰 및 만료 제한 시간으로 인증 메일이 재전송됨
        assertThat(findMember.authInfo.auth).isFalse
        assertThat(findMember.authInfo.emailAuthToken).isNotEqualTo(authToken)
        assertThat(findMember.authInfo.emailAuthExpireTime).isNotEqualTo(expireTime)
        
        assertThat(receivedMessages).hasSize(1)
        assertThat(findMember.username)
                .isEqualTo(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0].toString())
    }
    
    @Test
    @DisplayName("비밀번호 찾기")
    fun findPassword() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        member.authenticate() //이메일 인증
        
        //When
        memberService.findPassword(member.username)
        clear()
        
        //Then
        greenMailExtension.waitForIncomingEmail(5000, 1)
        val findMember = em.find(Member::class.java, id)
        val receivedMessages = greenMailExtension.receivedMessages
        
        //비밀번호 찾기를 시도하면 20자리 임시 비밀번호로 변경되고, 메일로 전송됨
        assertThat(findMember.password).isNotEqualTo(passwordEncoder.encode(member.password))
        assertThat(passwordEncoder.matches("Password1!@", findMember.password)).isFalse
        
        assertThat(receivedMessages).hasSize(1)
        assertThat(findMember.username)
                .isEqualTo(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0].toString())
    }
    
    @Test
    @DisplayName("비밀번호 찾기, 미인증 상태")
    fun findPassword_unauth() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        val authToken = member.authInfo.emailAuthToken
        val expireTime = member.authInfo.emailAuthExpireTime
        
        //When
        Thread.sleep(1000)
        clear()
        
        //Then
        assertThatThrownBy { memberService.findPassword(member.username) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_NOT_COMPLETED)
                .hasMessage("Email is not verified.")
        
        greenMailExtension.waitForIncomingEmail(5000, 1)
        val findMember = em.find(Member::class.java, id)
        val receivedMessages = greenMailExtension.receivedMessages
        
        //인증되지 않은 계정에 비밀번호 찾기를 시도하면 임시 비밀번호는 발급되지 않고 인증 메일이 전송됨
        assertThat(findMember.authInfo.auth).isFalse
        assertThat(findMember.authInfo.emailAuthToken).isNotEqualTo(authToken)
        assertThat(findMember.authInfo.emailAuthExpireTime).isNotEqualTo(expireTime)
        assertThat(passwordEncoder.matches("Password1!@", findMember.password)).isTrue //임시 비밀번호로 변경되지 않음
        
        assertThat(receivedMessages).hasSize(1)
        assertThat(findMember.username)
                .isEqualTo(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0].toString())
    }
    
    @Test
    @DisplayName("PK로 조회")
    fun findById() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id!!
        
        //When
        val dto = memberService.findById(id)
        clear()
        
        //Then
        val findMember = em.find(Member::class.java, id)
        
        assertThat(findMember.username).isEqualTo(dto.username)
        assertThat(findMember.nickname).isEqualTo(dto.nickname)
        assertThat(findMember.signature).isEqualTo(dto.signature)
        assertThat(findMember.role).isEqualTo(dto.role)
        assertThat(findMember.createdDate).isEqualTo(dto.createdDate)
        assertThat(findMember.lastModifiedDate).isEqualTo(dto.lastModifiedDate)
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    fun findById_unknown() {
        //Given
        val id = Random().nextLong()
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { memberService.findById(id) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Member' by id: $id")
    }
    
    @Test
    @DisplayName("아이디로 조회")
    fun findByUsername() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        val username = member.username
        
        //When
        val dto = memberService.findByUsername(username)
        clear()
        
        //Then
        val findMember = em.find(
                Member::class.java, id)
        assertThat(findMember.username).isEqualTo(dto.username)
        assertThat(findMember.nickname).isEqualTo(dto.nickname)
        assertThat(findMember.signature).isEqualTo(dto.signature)
        assertThat(findMember.role).isEqualTo(dto.role)
        assertThat(findMember.createdDate).isEqualTo(dto.createdDate)
        assertThat(findMember.lastModifiedDate).isEqualTo(dto.lastModifiedDate)
    }
    
    @Test
    @DisplayName("아이디로 조회, 존재하지 않는 아이디")
    fun findByUsername_unknown() {
        //Given
        val username = "user1@mail.com"
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { memberService.findByUsername(username) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Member' by username: $username")
    }
    
    @Test
    @DisplayName("정보 수정")
    fun update() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        member.authenticate() //이메일 인증
        val beforeNickname = member.nickname
        val beforeSignature = member.signature
        
        val dto = MemberUpdateRequestDto(id, "Password1!@", "user2", "Signature",
                                         "Password2!@", "Password2!@")
        
        //When
        memberService.update(dto)
        clear()
        
        //Then
        val afterMember = em.find(Member::class.java, id)
        
        assertThat(passwordEncoder.matches(dto.newPassword, afterMember.password)).isTrue
        assertThat(passwordEncoder.matches(dto.password, afterMember.password)).isFalse
        assertThat(afterMember.nickname).isEqualTo(dto.nickname)
        assertThat(beforeNickname).isNotEqualTo(dto.nickname)
        assertThat(afterMember.signature).isEqualTo(dto.signature)
        assertThat(beforeSignature).isNotEqualTo(dto.signature)
    }
    
    @Test
    @DisplayName("정보 수정, 미인증 상태")
    fun update_unauth() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        val authToken = member.authInfo.emailAuthToken
        val expireTime = member.authInfo.emailAuthExpireTime
        val beforeNickname = member.nickname
        val beforeSignature = member.signature
        
        val dto = MemberUpdateRequestDto(id, "Password1!@", "user2", "Signature",
                                         "Password2!@", "Password2!@")
        
        //When
        Thread.sleep(1000)
        clear()
        
        //Then
        assertThatThrownBy { memberService.update(dto) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_NOT_COMPLETED)
                .hasMessage("Email is not verified.")
        
        greenMailExtension.waitForIncomingEmail(5000, 1)
        val afterMember = em.find(Member::class.java, id)
        val receivedMessages = greenMailExtension.receivedMessages
        
        //인증되지 않은 계정에 정보 수정을 시도하면 정보는 수정되지 않고 인증 메일이 전송됨
        assertThat(afterMember.authInfo.auth).isFalse
        assertThat(afterMember.authInfo.emailAuthToken).isNotEqualTo(authToken)
        assertThat(afterMember.authInfo.emailAuthExpireTime).isNotEqualTo(expireTime)
        assertThat(passwordEncoder.matches(dto.newPassword, afterMember.password)).isFalse
        assertThat(passwordEncoder.matches(dto.password, afterMember.password)).isTrue
        assertThat(afterMember.nickname).isNotEqualTo(dto.nickname)
        assertThat(beforeNickname).isEqualTo(afterMember.nickname)
        assertThat(afterMember.signature).isNotEqualTo(dto.signature)
        assertThat(beforeSignature).isEqualTo(afterMember.signature)
        
        assertThat(receivedMessages).hasSize(1)
        assertThat(afterMember.username)
                .isEqualTo(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0].toString())
    }
    
    @Test
    @DisplayName("정보 수정, 비밀번호 불일치")
    fun update_wrongPassword() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        member.authenticate() //이메일 인증
        val beforeNickname = member.nickname
        val beforeSignature = member.signature
        
        val dto = MemberUpdateRequestDto(id, "Password2!@", "user2", "Signature",
                                         "Password2!@", "Password2!@")
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { memberService.update(dto) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_DO_NOT_MATCH)
                .hasMessage("Invalid input value, Password do not match.")
        
        val afterMember = em.find(Member::class.java, id)
        
        assertThat(passwordEncoder.matches(dto.newPassword, afterMember.password)).isFalse
        assertThat(afterMember.nickname).isNotEqualTo(dto.nickname)
        assertThat(beforeNickname).isEqualTo(afterMember.nickname)
        assertThat(afterMember.signature).isNotEqualTo(dto.signature)
        assertThat(beforeSignature).isEqualTo(afterMember.signature)
    }
    
    @Test
    @DisplayName("정보 수정, 닉네임 중복")
    fun update_duplicateNickname() {
        //Given
        val members = dummy.createMembers(2)
        for (member in members) em.persist(member)
        val member1 = members[0]
        val member2 = members[1]
        val id = member1.id
        member1.authenticate() //이메일 인증
        member2.authenticate() //이메일 인증
        val beforeNickname = member1.nickname
        val beforeSignature = member1.signature
        
        val dto = MemberUpdateRequestDto(id, "Password1!@", "user2", "Signature",
                                         "Password2!@", "Password2!@")
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { memberService.update(dto) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NICKNAME_DUPLICATION)
                .hasMessage("Invalid input value: " + dto.nickname)
        
        val afterMember = em.find(Member::class.java, id)
        
        assertThat(passwordEncoder.matches(dto.newPassword, afterMember.password)).isFalse
        assertThat(afterMember.nickname).isNotEqualTo(dto.nickname)
        assertThat(beforeNickname).isEqualTo(afterMember.nickname)
        assertThat(afterMember.signature).isNotEqualTo(dto.signature)
        assertThat(beforeSignature).isEqualTo(afterMember.signature)
    }
    
    @Test
    @DisplayName("회원 탈퇴")
    fun delete() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id!!
        val password = "Password1!@"
        
        //When
        memberService.delete(id, password)
        clear()
        
        //Then
        assertThat(em.find(Member::class.java, id).deleteYn).isTrue
    }
    
    @Test
    @DisplayName("회원 탈퇴, 존재하지 않는 계정")
    fun delete_unknown() {
        //Given
        val id = Random().nextLong()
        val password = "Password1!@"
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { memberService.delete(id, password) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Member' by id: $id")
    }
    
    @Test
    @DisplayName("회원 탈퇴, 비밀번호 불일치")
    fun delete_wrongPassword() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        val password = "Password2!@"
        
        //When
        clear()
        
        //Then
        assertThatThrownBy { memberService.delete(id!!, password) }
                .isInstanceOf(InvalidValueException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_DO_NOT_MATCH)
                .hasMessage("Invalid input value, Password do not match.")
    }
    
}