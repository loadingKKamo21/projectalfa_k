package com.project.alfa.repositories

import com.project.alfa.config.DummyGenerator
import com.project.alfa.config.TestConfig
import com.project.alfa.entities.AuthInfo
import com.project.alfa.entities.Member
import com.project.alfa.entities.Role
import com.project.alfa.repositories.mybatis.MemberMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

@Import(TestConfig::class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class MemberRepositoryTest {
    
    @Autowired
    lateinit var memberRepository: MemberRepository
    
    @Autowired
    lateinit var memberMapper: MemberMapper
    
    @Autowired
    lateinit var dummy: DummyGenerator
    
    @Test
    @DisplayName("계정 저장")
    fun save() {
        //Given
        val member = dummy.createMembers(1, false)[0]
        
        //When
        val savedMember = memberRepository.save(member)
        val id = savedMember.id!!
        
        //Then
        val findMember = memberMapper.findById(id)
        
        assertThat(findMember.username).isEqualTo(savedMember.username)
        assertThat(findMember.password).isEqualTo(savedMember.password)
        assertThat(findMember.authInfo.auth).isFalse
        assertThat(findMember.authInfo.emailAuthToken).isEqualTo(savedMember.authInfo.emailAuthToken)
        assertThat(findMember.authInfo.emailAuthExpireTime).isEqualTo(savedMember.authInfo.emailAuthExpireTime)
        assertThat(findMember.nickname).isEqualTo(savedMember.nickname)
        assertThat(findMember.role).isEqualTo(savedMember.role)
        assertThat(findMember.deleteYn).isFalse
    }
    
    @Test
    @DisplayName("PK로 조회")
    fun findById() {
        //Given
        val member = dummy.createMembers(1, true)[0]
        val id = member.id!!
        
        //When
        val findMember = memberRepository.findById(id).get()
        
        //Then
        assertThat(findMember.username).isEqualTo(member.username)
        assertThat(findMember.password).isEqualTo(member.password)
        assertThat(findMember.authInfo.emailAuthToken).isEqualTo(member.authInfo.emailAuthToken)
        assertThat(findMember.authInfo.emailAuthExpireTime).isEqualTo(member.authInfo.emailAuthExpireTime)
        assertThat(findMember.nickname).isEqualTo(member.nickname)
        assertThat(findMember.role).isEqualTo(member.role)
        assertThat(findMember.deleteYn).isFalse
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    fun findById_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        val unknownMember = memberRepository.findById(id)
        
        //Then
        assertThat(unknownMember.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 조회")
    fun findByIdAndDeleteYn() {
        //Given
        val member = dummy.createMembers(1, true)[0]
        val id = member.id!!
        
        //When
        val findMember = memberRepository.findById(id, false).get()
        
        //Then
        assertThat(findMember.username).isEqualTo(member.username)
        assertThat(findMember.password).isEqualTo(member.password)
        assertThat(findMember.authInfo.emailAuthToken).isEqualTo(member.authInfo.emailAuthToken)
        assertThat(findMember.authInfo.emailAuthExpireTime).isEqualTo(member.authInfo.emailAuthExpireTime)
        assertThat(findMember.nickname).isEqualTo(member.nickname)
        assertThat(findMember.role).isEqualTo(member.role)
        assertThat(findMember.deleteYn).isFalse
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 조회, 존재하지 않는 PK")
    fun findByIdAndDeleteYn_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        val unknownMember = memberRepository.findById(id, false)
        
        //Then
        assertThat(unknownMember.isPresent).isFalse
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 조회, 이미 탈퇴한 계정")
    fun findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        val member = dummy.createMembers(1, true)[0]
        val id = member.id!!
        memberMapper.deleteById(id)
        
        //When
        val deletedMember = memberRepository.findById(id, false)
        
        //Then
        assertThat(deletedMember.isPresent).isFalse
    }
    
    @Test
    @DisplayName("아이디로 조회")
    fun findByUsername() {
        //Given
        val member = dummy.createMembers(1, true)[0]
        val username = member.username
        
        //When
        val findMember = memberRepository.findByUsername(username).get()
        
        //Then
        assertThat(findMember.username).isEqualTo(member.username)
        assertThat(findMember.password).isEqualTo(member.password)
        assertThat(findMember.authInfo.emailAuthToken).isEqualTo(member.authInfo.emailAuthToken)
        assertThat(findMember.authInfo.emailAuthExpireTime).isEqualTo(member.authInfo.emailAuthExpireTime)
        assertThat(findMember.nickname).isEqualTo(member.nickname)
        assertThat(findMember.role).isEqualTo(member.role)
        assertThat(findMember.deleteYn).isFalse
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
        val member = dummy.createMembers(1, true)[0]
        val username = member.username
        
        //When
        val findMember = memberRepository.findByUsername(username, false).get()
        
        //Then
        assertThat(findMember.username).isEqualTo(member.username)
        assertThat(findMember.password).isEqualTo(member.password)
        assertThat(findMember.authInfo.emailAuthToken).isEqualTo(member.authInfo.emailAuthToken)
        assertThat(findMember.authInfo.emailAuthExpireTime).isEqualTo(member.authInfo.emailAuthExpireTime)
        assertThat(findMember.nickname).isEqualTo(member.nickname)
        assertThat(findMember.role).isEqualTo(member.role)
        assertThat(findMember.deleteYn).isFalse
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
        val member = dummy.createMembers(1, true)[0]
        val username = member.username
        val id = member.id!!
        memberMapper.deleteById(id)
        
        //When
        val deletedMember = memberRepository.findByUsername(username, false)
        
        //Then
        assertThat(deletedMember.isPresent).isFalse
    }
    
    @Test
    @DisplayName("모든 계정 조회")
    fun findAll() {
        //Given
        val size = Random.nextInt(20) + 1
        val members = dummy.createMembers(size, true)
        
        //When
        val findMembers = memberRepository.findAll()
        
        //Then
        assertThat(findMembers.size).isEqualTo(size)
        assertThat(findMembers.stream().allMatch { it is Member }).isTrue
    }
    
    @Test
    @DisplayName("인증 여부로 모든 계정 조회")
    fun findAllByAuth() {
        //Given
        val size = Random.nextInt(20) + 1
        val members = dummy.createMembers(size, true)
        var authenticatedCount = 0
        for (member in members) {
            val username = member.username
            val emailAuthToken = member.authInfo.emailAuthToken!!
            if (Random.nextBoolean()) {
                memberMapper.authenticateEmail(username, emailAuthToken, LocalDateTime.now())
                authenticatedCount++
            }
        }
        
        //When
        val findMembers = memberRepository.findAllByAuth(true)
        
        //Then
        assertThat(findMembers.size).isEqualTo(authenticatedCount)
        assertThat(findMembers.stream().allMatch { it is Member }).isTrue
    }
    
    @Test
    @DisplayName("탈퇴 여부로 모든 계정 조회")
    fun findAllByDeleteYn() {
        //Given
        val size = Random.nextInt(20) + 1
        val members = dummy.createMembers(size, true)
        var deletedCount = 0
        for (member in members) {
            val id = member.id!!
            if (id.toInt() % 2 != 0 && !Random.nextBoolean()) {
                memberMapper.deleteById(id)
                deletedCount++
            }
        }
        val undeletedMemberSize = size - deletedCount
        
        //When
        val findMembers = memberRepository.findAllByDeleteYn(false)
        
        //Then
        assertThat(findMembers.size).isEqualTo(undeletedMemberSize)
        assertThat(findMembers.stream().allMatch { it is Member }).isTrue
    }
    
    @Test
    @DisplayName("이메일 인증")
    fun authenticateEmail() {
        //Given
        val member = dummy.createMembers(1, true)[0]
        val id = member.id!!
        val username = member.username
        val emailAuthToken = member.authInfo.emailAuthToken!!
        
        //When
        memberRepository.authenticateEmail(username, emailAuthToken, LocalDateTime.now())
        
        //Then
        val findMember = memberMapper.findById(id)
        
        assertThat(member.authInfo.auth).isFalse
        assertThat(findMember.authInfo.auth).isTrue
    }
    
    @Test
    @DisplayName("이메일 인증, 인증 만료 시간 초과")
    fun authenticateEmail_timeout() {
        //Given
        val member = dummy.createMembers(1, true)[0]
        val id = member.id!!
        val username = member.username
        val emailAuthToken = member.authInfo.emailAuthToken!!
        val expireTime = member.authInfo.emailAuthExpireTime!!
        
        //When
        memberRepository.authenticateEmail(username, emailAuthToken, expireTime.plusSeconds(1))
        
        //Then
        val findMember = memberMapper.findById(id)
        
        assertThat(member.authInfo.auth).isFalse
        assertThat(findMember.authInfo.auth).isFalse
    }
    
    @Test
    @DisplayName("정보 수정")
    fun update() {
        //Given
        val member = dummy.createMembers(1, true)[0]
        val id = member.id!!
        memberMapper.authenticateEmail(member.username, member.authInfo.emailAuthToken!!, LocalDateTime.now())
        
        val beforeAuth = memberMapper.findById(id).authInfo.auth
        
        Thread.sleep(1000)
        
        val param = Member(id = id,
                           username = "",
                           password = "newPassword",
                           authInfo = AuthInfo(emailAuthToken = UUID.randomUUID().toString(),
                                               emailAuthExpireTime = LocalDateTime.now().withNano(0).plusMinutes(5)),
                           nickname = "newNickname",
                           role = Role.ADMIN)
        
        //When
        memberRepository.update(param)
        
        //Then
        val findMember = memberMapper.findById(id)
        
        val afterAuth = findMember.authInfo.auth
        
        assertThat(findMember.password).isEqualTo(param.password)
        assertThat(member.password).isNotEqualTo(param.password)
        assertThat(beforeAuth).isTrue
        assertThat(afterAuth).isFalse
        assertThat(findMember.authInfo.emailAuthToken!!).isEqualTo(param.authInfo.emailAuthToken!!)
        assertThat(member.authInfo.emailAuthToken!!).isNotEqualTo(param.authInfo.emailAuthToken!!)
        assertThat(findMember.authInfo.emailAuthExpireTime!!).isEqualTo(param.authInfo.emailAuthExpireTime!!)
        assertThat(member.authInfo.emailAuthToken!!).isNotEqualTo(param.authInfo.emailAuthExpireTime!!)
        assertThat(findMember.nickname).isEqualTo(param.nickname)
        assertThat(member.nickname).isNotEqualTo(param.nickname)
        assertThat(findMember.role).isEqualTo(param.role)
        assertThat(member.role).isNotEqualTo(param.role)
    }
    
    @Test
    @DisplayName("PK로 엔티티 존재 여부 확인")
    fun existsById() {
        //Given
        val member = dummy.createMembers(1, true)[0]
        val id = member.id!!
        
        //When
        val exists = memberRepository.existsById(id)
        
        //Then
        assertThat(exists).isTrue
    }
    
    @Test
    @DisplayName("PK로 엔티티 존재 여부 확인, 없음")
    fun existsById_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        val exists = memberRepository.existsById(id)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 엔티티 존재 여부 확인")
    fun existsByIdAndDeleteYn() {
        //Given
        val member = dummy.createMembers(1, true)[0]
        val id = member.id!!
        
        //When
        val exists = memberRepository.existsById(id, false)
        
        //Then
        assertThat(exists).isTrue
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 엔티티 존재 여부 확인, 없음")
    fun existsByIdAndDeleteYn_unknown() {
        //Given
        val id = Random.nextLong()
        
        //When
        val exists = memberRepository.existsById(id, false)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 엔티티 존재 여부 확인, 이미 탈퇴한 계정")
    fun existsByIdAndDeleteYn_alreadyDeleted() {
        //Given
        val member = dummy.createMembers(1, true)[0]
        val id = member.id!!
        memberMapper.deleteById(id)
        
        //When
        val exists = memberRepository.existsById(id, false)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("아이디 사용 확인, 사용 중")
    fun existsByUsername_using() {
        //Given
        val member = dummy.createMembers(1, true)[0]
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
        val member = dummy.createMembers(1, true)[0]
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
        val member = dummy.createMembers(1, true)[0]
        val username = member.username
        val id = member.id!!
        memberMapper.deleteById(id)
        
        //When
        val exists = memberRepository.existsByUsername(username, false)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("닉네임 사용 확인, 사용 중")
    fun existsByNickname_using() {
        //Given
        val member = dummy.createMembers(1, true)[0]
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
        val member = dummy.createMembers(1, true)[0]
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
        val member = dummy.createMembers(1, true)[0]
        val nickname = member.nickname
        val id = member.id!!
        memberMapper.deleteById(id)
        
        //When
        val exists = memberRepository.existsByNickname(nickname, false)
        
        //Then
        assertThat(exists).isFalse
    }
    
    @Test
    @DisplayName("계정 탈퇴")
    fun deleteById() {
        //Given
        val member = dummy.createMembers(1, true)[0]
        val id = member.id!!
        
        //When
        memberRepository.deleteById(id)
        
        //Then
        val deletedMember = memberMapper.findById(id)
        
        assertThat(deletedMember.deleteYn).isTrue
    }
    
    @Test
    @DisplayName("계정 정보 영구 삭제")
    fun permanentlyDeleteById() {
        //Given
        val member = dummy.createMembers(1, true)[0]
        val id = member.id!!
        memberMapper.deleteById(id)
        
        //When
        memberRepository.permanentlyDeleteById(id)
        
        //Then
        val unknownMember = memberMapper.findById(id)
        
        assertThat(unknownMember).isNull()
    }
    
}