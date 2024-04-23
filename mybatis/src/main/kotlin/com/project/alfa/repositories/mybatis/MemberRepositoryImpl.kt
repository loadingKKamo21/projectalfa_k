package com.project.alfa.repositories.mybatis

import com.project.alfa.entities.Member
import com.project.alfa.repositories.MemberRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class MemberRepositoryImpl(private val memberMapper: MemberMapper) : MemberRepository {
    
    /**
     * 계정 저장
     *
     * @param member - 계정 정보
     * @return 계정 정보
     */
    override fun save(member: Member): Member {
        memberMapper.save(member)
        return member
    }
    
    /**
     * 계정 정보 조회
     *
     * @param id - PK
     * @return 계정 정보
     */
    override fun findById(id: Long): Optional<Member> = Optional.ofNullable(memberMapper.findById(id))
    
    /**
     * 계정 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보
     */
    override fun findById(id: Long, deleteYn: Boolean): Optional<Member> = Optional.ofNullable(
            memberMapper.findByIdAndDeleteYn(id, deleteYn))
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @return 계정 정보
     */
    override fun findByUsername(username: String): Optional<Member> = Optional.ofNullable(
            memberMapper.findByUsername(username))
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보
     */
    override fun findByUsername(username: String, deleteYn: Boolean): Optional<Member> = Optional.ofNullable(
            memberMapper.findByUsernameAndDeleteYn(username, deleteYn))
    
    /**
     * 계정 정보 목록 조회
     *
     * @return 계정 정보 목록
     */
    override fun findAll(): List<Member> = memberMapper.findAll()
    
    /**
     * 계정 정보 목록 조회
     *
     * @param auth - 인증 여부
     * @return 계정 정보 목록
     */
    override fun findAllByAuth(auth: Boolean): List<Member> = memberMapper.findAllByAuth(auth)
    
    /**
     * 계정 정보 목록 조회
     *
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보 목록
     */
    override fun findAllByDeleteYn(deleteYn: Boolean): List<Member> = memberMapper.findAllByDeleteYn(deleteYn)
    
    /**
     * 이메일 인증 정보로 미인증 계정 인증
     *
     * @param username          - 아이디(이메일)
     * @param emailAuthToken    - 인증 토큰
     * @param authenticatedTime - 인증 시각
     */
    override fun authenticateEmail(username: String,
                                   emailAuthToken: String,
                                   authenticatedTime: LocalDateTime) = memberMapper.authenticateEmail(username,
                                                                                                      emailAuthToken,
                                                                                                      authenticatedTime)
    
    /**
     * OAuth 2.0 인증 정보로 미인증 계정 인증
     *
     * @param username          - 아이디
     * @param provider          - OAuth 2.0 Provider
     * @param providerId        - OAuth 2.0 Provider Id
     * @param authenticatedTime - 인증 시각
     */
    override fun authenticateOAuth(username: String,
                                   provider: String,
                                   providerId: String,
                                   authenticatedTime: LocalDateTime) = memberMapper.authenticateOAuth(username,
                                                                                                      provider,
                                                                                                      providerId,
                                                                                                      authenticatedTime)
    
    /**
     * 계정 정보 수정
     *
     * @param param - 계정 수정 정보
     */
    override fun update(param: Member) = memberMapper.update(param)
    
    /**
     * 계정 엔티티 존재 확인
     *
     * @param id - PK
     * @return 존재 여부
     */
    override fun existsById(id: Long): Boolean = memberMapper.existsById(id)
    
    /**
     * 계정 엔티티 존재 확인
     *
     * @param id       - PK
     * @param deleteYn - 탈퇴 여부
     * @return 존재 여부
     */
    override fun existsById(id: Long, deleteYn: Boolean): Boolean = memberMapper.existsByIdAndDeleteYn(id, deleteYn)
    
    /**
     * 아이디(이메일) 사용 확인
     *
     * @param username - 아이디(이메일)
     * @return 사용 여부
     */
    override fun existsByUsername(username: String): Boolean = memberMapper.existsByUsername(username)
    
    /**
     * 아이디(이메일) 사용 확인
     *
     * @param username - 아이디(이메일)
     * @param deleteYn - 탈퇴 여부
     * @return 사용 여부
     */
    override fun existsByUsername(username: String,
                                  deleteYn: Boolean): Boolean = memberMapper.existsByUsernameAndDeleteYn(username,
                                                                                                         deleteYn)
    
    /**
     * 닉네임 사용 확인
     *
     * @param nickname - 닉네임
     * @return 사용 여부
     */
    override fun existsByNickname(nickname: String): Boolean = memberMapper.existsByNickname(nickname)
    
    /**
     * 닉네임 사용 확인
     *
     * @param nickname - 닉네임
     * @param deleteYn - 탈퇴 여부
     * @return 사용 여부
     */
    override fun existsByNickname(nickname: String,
                                  deleteYn: Boolean): Boolean = memberMapper.existsByNicknameAndDeleteYn(nickname,
                                                                                                         deleteYn)
    
    /**
     * 회원 탈퇴
     *
     * @param id - PK
     */
    override fun deleteById(id: Long) = memberMapper.deleteById(id)
    
    /**
     * 계정 정보 영구 삭제
     *
     * @param id - PK
     */
    override fun permanentlyDeleteById(id: Long) = memberMapper.permanentlyDeleteById(id)
    
}