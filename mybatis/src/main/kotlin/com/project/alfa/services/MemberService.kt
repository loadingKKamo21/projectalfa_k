package com.project.alfa.services

import com.project.alfa.entities.AuthInfo
import com.project.alfa.entities.Member
import com.project.alfa.entities.Role
import com.project.alfa.error.exception.EntityNotFoundException
import com.project.alfa.error.exception.ErrorCode
import com.project.alfa.error.exception.InvalidValueException
import com.project.alfa.repositories.MemberRepository
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

private const val MAX_EXPIRE_TIME: Long = 5L  //이메일 인증 만료 제한 시간

@Service
@Transactional(readOnly = true)
class MemberService(
        private val memberRepository: MemberRepository,
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
        if (usernameDuplicationCheck(dto.username))
            throw InvalidValueException("Invalid input value: ${dto.username}", ErrorCode.USERNAME_DUPLICATION)
        
        //닉네임 중복 확인
        if (nicknameDuplicationCheck(dto.nickname))
            throw InvalidValueException("Invalid input value: ${dto.nickname}", ErrorCode.NICKNAME_DUPLICATION)
        
        val member = Member(username = dto.username.lowercase(),
                            password = passwordEncoder.encode(dto.password),
                            authInfo = AuthInfo(
                                    emailAuthToken = UUID.randomUUID().toString(),
                                    emailAuthExpireTime = LocalDateTime.now().withNano(0).plusMinutes(MAX_EXPIRE_TIME)
                            ),
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
        
        memberRepository.authenticateEmail(username.lowercase(), authToken, authTime)
        
        val id = member.id!!
        //인증이 완료되지 않은 경우: 토큰 불일치 또는 인증 만료 제한 시간 초과
        if (!memberRepository.findById(id, false).orElseThrow {
                    EntityNotFoundException("Could not found 'Member' by id: $id")
                }.authInfo.auth)
            resendVerifyEmail(username)
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
        
        val authToken = UUID.randomUUID().toString()    //새로운 인증 토큰
        val expireTime = LocalDateTime.now().withNano(0).plusMinutes(MAX_EXPIRE_TIME)   //새로운 인증 만료 제한 시간
        
        val param = Member(id = member.id,
                           username = "",
                           password = "",
                           authInfo = AuthInfo(
                                   emailAuthToken = authToken,
                                   emailAuthExpireTime = expireTime
                           ),
                           nickname = "")
        
        memberRepository.update(param)
        
        //인증 메일 재전송
        emailSender.sendVerificationEmail(username, authToken, expireTime)
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
        isVerifiedEmail(member.username, member.authInfo.auth)
        
        //임시 비밀번호 생성 및 반영
        val tempPassword = RandomGenerator.randomPassword(20)
        memberRepository.update(Member(id = member.id,
                                       username = "",
                                       password = passwordEncoder.encode(tempPassword),
                                       authInfo = AuthInfo(),
                                       nickname = ""))
        
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
            EntityNotFoundException("Could not found 'Member' by id: ${dto.id}")
        }
        var flag: Boolean = false
        var password: String = ""
        var nickname: String = ""
        var signature: String = ""
        
        //이메일 인증 여부 확인
        isVerifiedEmail(member.username, member.authInfo.auth)
        
        //비밀번호 확인
        if (!passwordEncoder.matches(dto.password, member.password))
            throw InvalidValueException("Invalid input value, Password do not match.", ErrorCode.PASSWORD_DO_NOT_MATCH)
        
        //닉네임 변경
        if (dto.nickname != null)
            if (member.nickname != dto.nickname && dto.nickname!!.isNotBlank()) {
                //변경할 닉네임 중복 확인
                if (nicknameDuplicationCheck(dto.nickname!!))
                    throw InvalidValueException("Invalid input value: ${dto.nickname}", ErrorCode.NICKNAME_DUPLICATION)
                flag = true
                nickname = dto.nickname!!
            }
        
        //서명 변경
        if (member.signature == null) {
            if (dto.signature != null && dto.signature!!.isNotBlank()) {
                flag = true
                signature = dto.signature!!
            }
        } else if (member.signature != dto.signature) {
            flag = true
            signature = dto.signature!!
        }
        
        //비밀번호 변경
        if ((dto.newPassword != null && dto.newPassword!!.isNotBlank())
                && !passwordEncoder.matches(dto.newPassword, member.password)) {
            //신규 비밀번호 확인
            if (dto.newPassword != dto.repeatNewPassword)
                throw InvalidValueException("Invalid input value, New password do not match.",
                                            ErrorCode.PASSWORD_DO_NOT_MATCH)
            flag = true
            password = passwordEncoder.encode(dto.newPassword)
        }
        
        //변경될 값이 있는 지 확인
        if (flag)
            memberRepository.update(
                    Member(id = member.id,
                           username = "",
                           password = password,
                           authInfo = AuthInfo(),
                           nickname = nickname,
                           signature = signature)
            )
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
        
        memberRepository.deleteById(member.id!!)
    }
    
    //==================== 검증 메서드 ====================//
    
    /**
     * 아이디(이메일) 중복 확인
     *
     * @param inputUsername - 중복 확인할 아이디
     * @return 중복 여부
     */
    private fun usernameDuplicationCheck(inputUsername: String): Boolean =
            inputUsername.isNotBlank() && memberRepository.existsByUsername(inputUsername.lowercase())
    
    /**
     * 닉네임 중복 확인
     *
     * @param inputNickname - 중복 확인할 닉네임
     * @return 중복 여부
     */
    private fun nicknameDuplicationCheck(inputNickname: String): Boolean =
            inputNickname.isNotBlank() && memberRepository.existsByNickname(inputNickname)
    
    /**
     * 이메일 인증 여부 확인: 인증 상태 -> return, 미인증 상태 -> 인증 메일 전송 및 RuntimeException
     *
     * @param username   - 메일 주소
     * @param authStatus - 인증 상태
     */
    private fun isVerifiedEmail(username: String, authStatus: Boolean) {
        if (!authStatus) {
            resendVerifyEmail(username)
            throw InvalidValueException("Email is not verified.", ErrorCode.AUTH_NOT_COMPLETED)
        }
        return
    }
    
}