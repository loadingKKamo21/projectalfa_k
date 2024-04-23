package com.project.alfa.repositories.v2

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.entities.AuthInfo
import com.project.alfa.entities.Member
import com.project.alfa.entities.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Import(TestConfig::class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class MemberRepositoryV2Test {
    
    @Autowired
    lateinit var memberRepository: MemberRepositoryV2
    
    @PersistenceContext
    lateinit var em: EntityManager
    
    @Autowired
    lateinit var passwordEncoder: PasswordEncoder
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @AfterEach
    fun clear() {
        em.flush()
        em.clear()
    }
    
    @Test
    @DisplayName("계정 저장")
    fun save() {
        //Given
        val member = dummy.createMembers(1)[0]
        
        //When
        val id = memberRepository.save(member).id
        
        //Then
        val findMember = em.find(Member::class.java, id)
        
        assertThat(findMember).isEqualTo(member)
    }
    
    @Test
    @DisplayName("PK로 조회")
    fun findById() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        
        //When
        val findMember = memberRepository.findById(id!!).get()
        
        //Then
        assertThat(findMember).isEqualTo(member)
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    fun findById_unknown() {
        //Given
        val id = Random().nextLong()
        
        //When
        val unknownMember = memberRepository.findById(id)
        
        //Then
        assertThat(unknownMember.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 조회")
    fun findByIdAndDeleteYn() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        
        //When
        val findMember = memberRepository.findById(id!!, false).get()
        
        //Then
        assertThat(findMember).isEqualTo(member)
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 조회, 존재하지 않는 PK")
    fun findByIdAndDeleteYn_unknown() {
        //Given
        val id = Random().nextLong()
        
        //When
        val unknownMember = memberRepository.findById(id, false)
        
        //Then
        assertThat(unknownMember.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 조회, 이미 탈퇴한 계정")
    fun findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        member.isDelete(true)
        
        //When
        val deletedMember = memberRepository.findById(id!!, false)
        
        //Then
        assertThat(deletedMember.isPresent).isFalse
    }
    
    @Test
    @DisplayName("아이디로 조회")
    fun findByUsername() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val username = member.username
        
        //When
        val findMember = memberRepository.findByUsername(username).get()
        
        //Then
        assertThat(findMember).isEqualTo(member)
    }
    
    @Test
    @DisplayName("아이디로 조회, 존재하지 않는 아이디")
    fun findByUsername_unknown() {
        //Given
        val username = "user1@mail.com"
        
        //When
        val unknownMember = memberRepository.findByUsername(username)
        
        //Then
        assertThat(unknownMember.isPresent).isFalse
    }
    
    @Test
    @DisplayName("아이디, 탈퇴 여부로 조회")
    fun findByUsernameAndDeleteYn() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val username = member.username
        
        //When
        val findMember = memberRepository.findByUsername(username, false).get()
        
        //Then
        assertThat(findMember).isEqualTo(member)
    }
    
    @Test
    @DisplayName("아이디, 탈퇴 여부로 조회, 존재하지 않는 아이디")
    fun findByUsernameAndDeleteYn_unknown() {
        //Given
        val username = "user1@mail.com"
        
        //When
        val unknownMember = memberRepository.findByUsername(username, false)
        
        //Then
        assertThat(unknownMember.isPresent).isFalse
    }
    
    @Test
    @DisplayName("아이디, 탈퇴 여부로 조회, 이미 탈퇴한 계정")
    fun findByUsernameAndDeleteYn_alreadyDeleted() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val username = member.username
        member.isDelete(true)
        
        //When
        val deletedMember = memberRepository.findByUsername(username, false)
        
        //Then
        assertThat(deletedMember.isPresent).isFalse
    }
    
    @Test
    @DisplayName("모든 계정 조회")
    fun findAll() {
        //Given
        val total = dummy.generateRandomNumber(1, 20)
        var members = dummy.createMembers(total)
        for (member in members)
            em.persist(member)
        
        //When
        var findMembers = memberRepository.findAll()
        
        //Then
        members = members.sortedBy { it.id }
        findMembers = findMembers.sortedBy { it.id }
        assertThat(findMembers.size).isEqualTo(total)
        for (i in 0 until total)
            assertThat(findMembers[i]).isEqualTo(members[i])
    }
    
    @Test
    @DisplayName("인증 여부로 모든 계정 조회")
    fun findAllByAuth() {
        //Given
        val total = dummy.generateRandomNumber(1, 20)
        var members = dummy.createMembers(total)
        val random = Random()
        var authenticatedCount = 0
        for (member in members) {
            em.persist(member)
            if (random.nextBoolean() && authenticatedCount < total) {
                member.authenticate()
                authenticatedCount++
            }
        }
        
        //When
        var findMembers = memberRepository.findAllByAuth(true)
        
        //Then
        members = members.filter { it.authInfo.auth }.sortedBy { it.id }
        findMembers = findMembers.sortedBy { it.id }
        assertThat(findMembers.size).isEqualTo(authenticatedCount)
        for (i in 0 until authenticatedCount)
            assertThat(findMembers[i]).isEqualTo(members[i])
    }
    
    @Test
    @DisplayName("탈퇴 여부로 모든 계정 조회")
    fun findAllByDeleteYn() {
        //Given
        val total = dummy.generateRandomNumber(1, 20)
        var members = dummy.createMembers(total)
        val random = Random()
        var deletedCount = 0
        for (member in members) {
            em.persist(member)
            if (random.nextBoolean() && deletedCount < total) {
                member.isDelete(true)
                deletedCount++
            }
        }
        
        //When
        var findMembers = memberRepository.findAllByDeleteYn(false)
        
        //Then
        members = members.filter { !it.deleteYn }.sortedBy { it.id }
        findMembers = findMembers.sortedBy { it.id }
        assertThat(findMembers.size).isEqualTo(total - deletedCount)
        for (i in 0 until total - deletedCount)
            assertThat(findMembers[i]).isEqualTo(members[i])
    }
    
    @Test
    @DisplayName("이메일 인증 정보로 대상 계정 조회")
    fun authenticateEmail() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val username = member.username
        val emailAuthToken = member.authInfo.emailAuthToken
        
        //When
        val findMember = memberRepository.authenticateEmail(username, emailAuthToken!!, LocalDateTime.now()).get()
        
        //Then
        assertThat(findMember).isEqualTo(member)
    }
    
    @Test
    @DisplayName("이메일 인증 정보로 대상 계정 조회, 인증 만료 시간 초과")
    fun authenticateEmail_timeout() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val username = member.username
        val emailAuthToken = member.authInfo.emailAuthToken
        val emailAuthExpireTime = member.authInfo.emailAuthExpireTime
        
        //When
        val findMember = memberRepository.authenticateEmail(username,
                                                            emailAuthToken!!,
                                                            emailAuthExpireTime!!.plusSeconds(1))
        
        //Then
        assertThat(findMember.isPresent).isFalse
    }
    
    @Test
    @DisplayName("OAuth 2.0 인증 정보로 대상 계정 조회")
    fun authenticateOAuth() {
        //Given
        val member: Member = Member(username = "user1@mail.com",
                                    password = passwordEncoder.encode("Password1!@"),
                                    authInfo = AuthInfo(oAuthProvider = "google",
                                                        oAuthProviderId = UUID.randomUUID().toString()),
                                    nickname = "user1",
                                    role = Role.USER)
        em.persist(member)
        val username = member.username
        val oAuthProvider = member.authInfo.oAuthProvider
        val oAuthProviderId = member.authInfo.oAuthProviderId
        
        //When
        val findMember = memberRepository.authenticateOAuth(username, oAuthProvider!!, oAuthProviderId!!).get()
        
        //Then
        assertThat(findMember).isEqualTo(member)
    }
    
    @Test
    @DisplayName("정보 수정")
    fun update() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        clear()
        
        //When
        val newPassword = "Password2!@"
        var newEmailAuthToken: String
        do {
            newEmailAuthToken = UUID.randomUUID().toString()
        } while (member.authInfo.emailAuthToken == newEmailAuthToken)
        val newNickname = "user2"
        val newSignature = "Signature"
        val findMember = memberRepository.findById(id!!).get()
        findMember.updatePassword(passwordEncoder.encode(newPassword))
        findMember.updateEmailAuthToken(newEmailAuthToken)
        findMember.updateNickname(newNickname)
        findMember.updateSignature(newSignature)
        clear()
        
        //Then
        val updatedMember = em.find(Member::class.java, id)
        
        assertThat(passwordEncoder.matches(newPassword, member.password)).isFalse
        assertThat(passwordEncoder.matches(newPassword, updatedMember.password)).isTrue
        assertThat(member.authInfo.emailAuthToken).isNotEqualTo(updatedMember.authInfo.emailAuthToken)
        assertThat(newEmailAuthToken).isEqualTo(updatedMember.authInfo.emailAuthToken)
        assertThat(member.signature).isNotEqualTo(updatedMember.signature)
        assertThat(newSignature).isEqualTo(updatedMember.signature)
    }
    
    @Test
    @DisplayName("아이디 사용 확인, 사용 중")
    fun existsByUsername_using() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val username = member.username
        
        //When
        val exists = memberRepository.existsByUsername(username)
        
        //Then
        assertThat(exists).isTrue
    }
    
    @Test
    @DisplayName("아이디 사용 확인, 미사용")
    fun existsByUsername_unused() {
        //Given
        val username = "user1@mail.com"
        
        //When
        val exists = memberRepository.existsByUsername(username)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("아이디, 탈퇴 여부로 사용 확인, 사용 중")
    fun existsByUsernameAndDeleteYn_using() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val username = member.username
        
        //When
        val exists = memberRepository.existsByUsername(username, false)
        
        //Then
        assertThat(exists).isTrue
    }
    
    @Test
    @DisplayName("아이디, 탈퇴 여부로 사용 확인, 미사용")
    fun existsByUsernameAndDeleteYn_unused() {
        //Given
        val username = "user1@mail.com"
        
        //When
        val exists = memberRepository.existsByUsername(username, false)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("아이디, 탈퇴 여부로 사용 확인, 이미 탈퇴한 계정")
    fun existsByUsernameAndDeleteYn_alreadyDeleted() {
        //Given
        val member = dummy.createMembers(1)[0]
        val username = member.username
        member.isDelete(true)
        
        //When
        val exists = memberRepository.existsByUsername(username, false)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("닉네임 사용 확인, 사용 중")
    fun existsByNickname_using() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val nickname = member.nickname
        
        //When
        val exists = memberRepository.existsByNickname(nickname)
        
        //Then
        assertThat(exists).isTrue
    }
    
    @Test
    @DisplayName("닉네임 사용 확인, 미사용")
    fun existsByNickname_unused() {
        //Given
        val nickname = "user1"
        
        //When
        val exists = memberRepository.existsByNickname(nickname)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("닉네임, 탈퇴 여부로 사용 확인, 사용 중")
    fun existsByNicknameAndDeleteYn_using() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val nickname = member.nickname
        
        //When
        val exists = memberRepository.existsByNickname(nickname, false)
        
        //Then
        assertThat(exists).isTrue
    }
    
    @Test
    @DisplayName("닉네임, 탈퇴 여부로 사용 확인, 미사용")
    fun existsByNicknameAndDeleteYn_unused() {
        //Given
        val nickname = "user1"
        
        //When
        val exists = memberRepository.existsByNickname(nickname, false)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("닉네임, 탈퇴 여부로 사용 확인, 이미 탈퇴한 계정")
    fun existsByNicknameAndDeleteYn_alreadyDeleted() {
        //Given
        val member = dummy.createMembers(1)[0]
        val nickname = member.nickname
        member.isDelete(true)
        
        //When
        val exists = memberRepository.existsByNickname(nickname, false)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("엔티티로 계정 정보 영구 삭제")
    fun delete() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        
        //When
        memberRepository.delete(member)
        
        //Then
        val deletedMember = em.find(Member::class.java, id)
        
        assertThat(deletedMember).isNull()
    }
    
    @Test
    @DisplayName("PK로 계정 정보 영구 삭제")
    fun deleteById() {
        //Given
        val member = dummy.createMembers(1)[0]
        em.persist(member)
        val id = member.id
        
        //When
        memberRepository.deleteById(id!!)
        
        //Then
        val deletedMember = em.find(Member::class.java, id)
        
        assertThat(deletedMember).isNull()
    }
    
}