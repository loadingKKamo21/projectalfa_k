package com.project.alfa.repositories.v2

import com.project.alfa.entities.Member
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class MemberRepositoryV2(private val memberJpaRepository: MemberJpaRepository) {
    
    /**
     * 계정 저장
     *
     * @param member - 계정 정보
     * @return 계정 정보
     */
    fun save(member: Member): Member = memberJpaRepository.save(member)
    
    /**
     * 계정 정보 조회
     *
     * @param id - PK
     * @return 계정 정보
     */
    fun findById(id: Long): Optional<Member> = memberJpaRepository.findById(id)
    
    /**
     * 계정 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보
     */
    fun findById(id: Long, deleteYn: Boolean): Optional<Member> = memberJpaRepository.findByIdAndDeleteYn(id, deleteYn)
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @return 계정 정보
     */
    fun findByUsername(username: String): Optional<Member> = memberJpaRepository.findByUsername(username)
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보
     */
    fun findByUsername(username: String,
                       deleteYn: Boolean): Optional<Member> = memberJpaRepository.findByUsernameAndDeleteYn(username,
                                                                                                            deleteYn)
    
    /**
     * 계정 정보 목록 조회
     *
     * @return 계정 정보 목록
     */
    fun findAll(): List<Member> = memberJpaRepository.findAll()
    
    /**
     * 계정 정보 목록 조회
     *
     * @param auth - 인증 여부
     * @return 계정 정보 목록
     */
    fun findAllByAuth(auth: Boolean): List<Member> = memberJpaRepository.findAllByAuthInfo_Auth(auth)
    
    /**
     * 계정 정보 목록 조회
     *
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보 목록
     */
    fun findAllByDeleteYn(deleteYn: Boolean): List<Member> = memberJpaRepository.findAllByDeleteYn(deleteYn)
    
    /**
     * 이메일 인증 정보로 미인증 계정 정보 조회
     *
     * @param username          - 아이디(이메일)
     * @param emailAuthToken    - 인증 토큰
     * @param authenticatedTime - 인증 시각
     * @return 계정 정보
     */
    fun authenticateEmail(username: String,
                          emailAuthToken: String,
                          authenticatedTime: LocalDateTime): Optional<Member> =
            memberJpaRepository.findByUsernameAndAuthInfo_EmailAuthTokenAndAuthInfo_EmailAuthExpireTimeGreaterThanEqualAndDeleteYnFalse(
                    username, emailAuthToken, authenticatedTime)
    
    /**
     * OAuth 2.0 인증 정보로 미인증 계정 정보 조회
     *
     * @param username   - 아이디
     * @param provider   - OAuth 2.0 Provider
     * @param providerId - OAuth 2.0 Provider Id
     * @return 계정 정보
     */
    fun authenticateOAuth(username: String,
                          provider: String,
                          providerId: String): Optional<Member> =
            memberJpaRepository.findByUsernameAndAuthInfo_oAuthProviderAndAuthInfo_oAuthProviderIdAndDeleteYnFalse(
                    username, provider, providerId)
    
    /**
     * 아이디(이메일) 사용 확인
     *
     * @param username - 아이디(이메일)
     * @return 사용 여부
     */
    fun existsByUsername(username: String): Boolean = memberJpaRepository.existsByUsername(username)
    
    /**
     * 아이디(이메일) 사용 확인
     *
     * @param username - 아이디(이메일)
     * @param deleteYn - 탈퇴 여부
     * @return 사용 여부
     */
    fun existsByUsername(username: String,
                         deleteYn: Boolean): Boolean = memberJpaRepository.existsByUsernameAndDeleteYn(username,
                                                                                                       deleteYn)
    
    /**
     * 닉네임 사용 확인
     *
     * @param nickname - 닉네임
     * @return 사용 여부
     */
    fun existsByNickname(nickname: String): Boolean = memberJpaRepository.existsByNickname(nickname)
    
    /**
     * 닉네임 사용 확인
     *
     * @param nickname - 닉네임
     * @param deleteYn - 탈퇴 여부
     * @return 사용 여부
     */
    fun existsByNickname(nickname: String,
                         deleteYn: Boolean): Boolean = memberJpaRepository.existsByNicknameAndDeleteYn(nickname,
                                                                                                       deleteYn)
    
    /**
     * 계정 정보 영구 삭제
     *
     * @param member - 계정 정보
     */
    fun delete(member: Member) = memberJpaRepository.delete(member)
    
    /**
     * 계정 정보 영구 삭제
     *
     * @param id - PK
     */
    fun deleteById(id: Long) = memberJpaRepository.deleteById(id)
    
}