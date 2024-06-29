package com.project.alfa.services

import com.project.alfa.entities.AuthInfo
import com.project.alfa.entities.Member
import com.project.alfa.entities.Role
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.error.exception.InvalidValueException
import com.project.alfa.repositories.v1.MemberRepositoryV1
import com.project.alfa.services.dto.MemberInfoResponseDto
import com.project.alfa.services.dto.MemberJoinRequestDto
import com.project.alfa.services.dto.MemberUpdateRequestDto
import com.project.alfa.utils.EmailSender
import com.project.alfa.utils.RandomGenerator
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional(readOnly = true)
class MemberService(
        private val memberRepository: MemberRepositoryV1,
        //private val memberRepository: MemberRepositoryV2,
        //private val memberRepository: MemberRepositoryV3,
        private val passwordEncoder: PasswordEncoder,
        private val emailSender: EmailSender
) {
    
    /**
     * 회원 가입
     *
     * @param dto - 계정 가입 정보 DTO
     * @return PK
     */
    @Transactional
    fun join(dto: MemberJoinRequestDto): Long {
        //비밀번호 확인
        if (dto.password != dto.repeatPassword)
            throw InvalidValueException("Invalid input value, Password do not match.", ErrorCode.PASSWORD_DO_NOT_MATCH)
        //아이디(이메일) 중복 확인
        if (memberRepository.existsByUsername(dto.username.lowercase()))
            throw InvalidValueException("Invalid input value: ${dto.username}", ErrorCode.USERNAME_DUPLICATION)
        //닉네임 중복 확인
        if (memberRepository.existsByNickname(dto.nickname))
            throw InvalidValueException("Invalid input value: ${dto.nickname}", ErrorCode.NICKNAME_DUPLICATION)
        
        val member = Member(username = dto.username.lowercase(),
                            password = passwordEncoder.encode(dto.password),
                            authInfo = AuthInfo(emailAuthToken = UUID.randomUUID().toString()),
                            nickname = dto.nickname,
                            role = Role.USER)
        
        memberRepository.save(member)
        
        //가입 인증 메일 전송
        emailSender.sendVerificationEmail(member.username,
                                          member.authInfo.emailAuthToken!!,
                                          member.authInfo.emailAuthExpireTime!!)
        
        return member.id!!
    }
    
    /**
     * 이메일 인증: 인증 토큰 및 인증 시간 확인 후 미인증 -> 인증 상태로 변경
     *
     * @param username  - 메일 주소
     * @param authToken - 인증 토큰
     * @param authTime  - 인증 시간
     */
    @Transactional
    fun verifyEmailAuth(username: String, authToken: String, authTime: LocalDateTime) {
        val member = memberRepository.findByUsername(username.lowercase(), false).orElseThrow {
            EntityNotFoundException("Could not found 'Member' by username: $username")
        }
        
        //이미 인증된 계정인 경우
        if (member.authInfo.auth)
            return
        
        val optionalMember = memberRepository.authenticateEmail(username.lowercase(), authToken, authTime)
        
        if (!optionalMember.isPresent || member !== optionalMember.get())
        //인증이 완료되지 않은 경우: 토큰 불일치 또는 인증 만료 제한 시간 초과
            resendVerifyEmail(username)
        else if (optionalMember.get() === member)
        //인증 정보가 일치하는 경우
            member.authenticate()
    }
    
    /**
     * 인증 메일 재전송: 새로운 인증 토큰 반영 및 인증 -> 미인증 상태로 변경
     *
     * @param username - 메일 주소
     */
    @Transactional
    fun resendVerifyEmail(username: String) {
        val member = memberRepository.findByUsername(username.lowercase(), false).orElseThrow {
            EntityNotFoundException("Could not found 'Member' by username: $username")
        }
        
        //새로운 인증 토큰 설정
        member.updateEmailAuthToken(UUID.randomUUID().toString())
        
        //인증 메일 재전송
        emailSender.sendVerificationEmail(username,
                                          member.authInfo.emailAuthToken!!,
                                          member.authInfo.emailAuthExpireTime!!)
    }
    
    /**
     * 비밀번호 찾기: 아이디로 계정 검증 후 임시 비밀번호 변경 및 비밀번호 찾기 결과 메일 전송
     *
     * @param username - 메일 주소
     */
    @Transactional
    fun findPassword(username: String) {
        val member = memberRepository.findByUsername(username.lowercase(), false).orElseThrow {
            EntityNotFoundException("Could not found 'Member' by username: $username")
        }
        
        //이메일 인증 여부 확인
        if (!member.authInfo.auth) {
            resendVerifyEmail(username)
            throw InvalidValueException("Email is not verified.", ErrorCode.AUTH_NOT_COMPLETED)
        }
        
        //임시 비밀번호 생성 및 반영
        val tempPassword = RandomGenerator.randomPassword(20)
        member.updatePassword(passwordEncoder.encode(tempPassword))
        
        //비밀번호 찾기 결과 메일 전송
        emailSender.sendPasswordResetEmail(member.username, tempPassword!!)
    }
    
    /**
     * 계정 정보 조회
     *
     * @param id - PK
     * @return 계정 정보 DTO
     */
    fun findById(id: Long): MemberInfoResponseDto = MemberInfoResponseDto(
            memberRepository.findById(id, false).orElseThrow {
                EntityNotFoundException("Could not found 'Member' by id: $id")
            }
    )
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @return 계정 정보 DTO
     */
    fun findByUsername(username: String): MemberInfoResponseDto = MemberInfoResponseDto(
            memberRepository.findByUsername(username.lowercase(), false).orElseThrow {
                EntityNotFoundException("Could not found 'Member' by username: $username")
            }
    )
    
    /**
     * 계정 정보 수정
     *
     * @param dto - 계정 수정 정보 DTO
     */
    @Transactional
    fun update(dto: MemberUpdateRequestDto) {
        val member = memberRepository.findById(dto.id!!, false).orElseThrow {
            EntityNotFoundException("Could not found 'Member' by id: $dto.id")
        }
        
        //이메일 인증 여부 확인
        if (!member.authInfo.auth) {
            resendVerifyEmail(member.username)
            throw InvalidValueException("Email is not verified.", ErrorCode.AUTH_NOT_COMPLETED)
        }
        
        //비밀번호 확인
        if (!passwordEncoder.matches(dto.password, member.password))
            throw InvalidValueException("Invalid input value, Password do not match.", ErrorCode.PASSWORD_DO_NOT_MATCH)
        
        //닉네임 변경
        if (member.nickname != dto.nickname) {
            //변경할 닉네임 중복 확인
            if (memberRepository.existsByNickname(dto.nickname!!))
                throw InvalidValueException("Invalid input value: ${dto.nickname}", ErrorCode.NICKNAME_DUPLICATION)
            member.updateNickname(dto.nickname!!)
        }
        
        //서명 변경
        if (member.signature == null) {
            if (!dto.signature.isNullOrBlank())
                member.updateSignature(dto.signature)
        } else if (member.signature != dto.signature)
            member.updateSignature(dto.signature)
        
        //비밀번호 변경
        if (!dto.newPassword.isNullOrBlank() && !passwordEncoder.matches(dto.newPassword, member.password)) {
            //신규 비밀번호 확인
            if (dto.newPassword != dto.repeatNewPassword)
                throw InvalidValueException("Invalid input value, New password do not match.",
                                            ErrorCode.PASSWORD_DO_NOT_MATCH)
            member.updatePassword(passwordEncoder.encode(dto.newPassword))
        }
    }
    
    /**
     * 회원 탈퇴
     *
     * @param id       - PK
     * @param password - 비밀번호
     */
    @Transactional
    fun delete(id: Long, password: String) {
        val member = memberRepository.findById(id, false).orElseThrow {
            EntityNotFoundException("Could not found 'Member' by id: $id")
        }
        
        //비밀번호 확인
        if (!passwordEncoder.matches(password, member.password))
            throw InvalidValueException("Invalid input value, Password do not match.", ErrorCode.PASSWORD_DO_NOT_MATCH)
        
        member.isDelete(true)
    }
    
}