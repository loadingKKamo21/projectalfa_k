package com.project.alfa.repositories.v1

import com.project.alfa.entities.Member
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
class MemberRepositoryV1 {
    
    @PersistenceContext
    private lateinit var em: EntityManager
    
    /**
     * 계정 저장
     *
     * @param member - 계정 정보
     * @return 계정 정보
     */
    fun save(member: Member): Member {
        em.persist(member)
        return member
    }
    
    /**
     * 계정 정보 조회
     *
     * @param id - PK
     * @return 계정 정보
     */
    fun findById(id: Long): Optional<Member> = Optional.ofNullable(
            em.createQuery("SELECT m FROM Member m WHERE m.id = :id", Member::class.java)
                    .setParameter("id", id)
                    .resultList.firstOrNull()
    )
    
    /**
     * 계정 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보
     */
    fun findById(id: Long, deleteYn: Boolean): Optional<Member> = Optional.ofNullable(
            em.createQuery("SELECT m FROM Member m WHERE m.id = :id AND m.deleteYn = :deleteYn", Member::class.java)
                    .setParameter("id", id)
                    .setParameter("deleteYn", deleteYn)
                    .resultList.firstOrNull()
    )
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @return 계정 정보
     */
    fun findByUsername(username: String): Optional<Member> = Optional.ofNullable(
            em.createQuery("SELECT m FROM Member m WHERE m.username = :username", Member::class.java)
                    .setParameter("username", username)
                    .resultList.firstOrNull()
    )
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보
     */
    fun findByUsername(username: String, deleteYn: Boolean): Optional<Member> = Optional.ofNullable(
            em.createQuery("SELECT m FROM Member m WHERE m.username = :username AND m.deleteYn = :deleteYn",
                           Member::class.java)
                    .setParameter("username", username)
                    .setParameter("deleteYn", deleteYn)
                    .resultList.stream().findFirst().orElse(null)
    )
    
    /**
     * 계정 정보 목록 조회
     *
     * @return 계정 정보 목록
     */
    fun findAll(): List<Member> = em.createQuery("SELECT m FROM Member m", Member::class.java).resultList
    
    /**
     * 계정 정보 목록 조회
     *
     * @param auth - 인증 여부
     * @return 계정 정보 목록
     */
    fun findAllByAuth(auth: Boolean): List<Member> =
            em.createQuery("SELECT m FROM Member m WHERE m.authInfo.auth = :auth", Member::class.java)
                    .setParameter("auth", auth)
                    .resultList
    
    /**
     * 계정 정보 목록 조회
     *
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보 목록
     */
    fun findAllByDeleteYn(deleteYn: Boolean): List<Member> =
            em.createQuery("SELECT m FROM Member m WHERE m.deleteYn = :deleteYn", Member::class.java)
                    .setParameter("deleteYn", deleteYn)
                    .resultList
    
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
                          authenticatedTime: LocalDateTime): Optional<Member> = Optional.ofNullable(
            em.createQuery("SELECT m FROM Member m WHERE m.username = :username AND m.authInfo.emailAuthToken = :emailAuthToken AND m.authInfo.emailAuthExpireTime >= :authenticatedTime AND m.deleteYn = false",
                           Member::class.java)
                    .setParameter("username", username)
                    .setParameter("emailAuthToken", emailAuthToken)
                    .setParameter("authenticatedTime", authenticatedTime)
                    .resultList.stream().findFirst().orElse(null)
    )
    
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
                          providerId: String): Optional<Member> = Optional.ofNullable(
            em.createQuery("SELECT m FROM Member m WHERE m.username = :username AND m.authInfo.oAuthProvider = :provider AND m.authInfo.oAuthProviderId = :providerId AND m.deleteYn = false",
                           Member::class.java)
                    .setParameter("username", username)
                    .setParameter("provider", provider)
                    .setParameter("providerId", providerId)
                    .resultList.stream().findFirst().orElse(null)
    )
    
    /**
     * 아이디(이메일) 사용 확인
     *
     * @param username - 아이디(이메일)
     * @return 사용 여부
     */
    fun existsByUsername(username: String): Boolean =
            em.createQuery("SELECT CASE WHEN (COUNT(m) > 0) THEN TRUE ELSE FALSE END FROM Member m WHERE m.username = :username",
                           Boolean::class.javaObjectType)
                    .setParameter("username", username).singleResult
    
    /**
     * 아이디(이메일) 사용 확인
     *
     * @param username - 아이디(이메일)
     * @param deleteYn - 탈퇴 여부
     * @return 사용 여부
     */
    fun existsByUsername(username: String, deleteYn: Boolean): Boolean =
            em.createQuery("SELECT CASE WHEN (COUNT(m) > 0) THEN TRUE ELSE FALSE END FROM Member m WHERE m.username = :username AND m.deleteYn = :deleteYn",
                           Boolean::class.javaObjectType)
                    .setParameter("username", username)
                    .setParameter("deleteYn", deleteYn)
                    .singleResult
    
    /**
     * 닉네임 사용 확인
     *
     * @param nickname - 닉네임
     * @return 사용 여부
     */
    fun existsByNickname(nickname: String): Boolean =
            em.createQuery("SELECT CASE WHEN (COUNT(m) > 0) THEN TRUE ELSE FALSE END FROM Member m WHERE m.nickname = :nickname",
                           Boolean::class.javaObjectType)
                    .setParameter("nickname", nickname).singleResult
    
    /**
     * 닉네임 사용 확인
     *
     * @param nickname - 닉네임
     * @param deleteYn - 탈퇴 여부
     * @return 사용 여부
     */
    fun existsByNickname(nickname: String, deleteYn: Boolean): Boolean =
            em.createQuery("SELECT CASE WHEN (COUNT(m) > 0) THEN TRUE ELSE FALSE END FROM Member m WHERE m.nickname = :nickname AND m.deleteYn = :deleteYn",
                           Boolean::class.javaObjectType)
                    .setParameter("nickname", nickname)
                    .setParameter("deleteYn", deleteYn)
                    .singleResult
    
    /**
     * 계정 정보 영구 삭제
     *
     * @param member - 계정 정보
     */
    fun delete(member: Member) = em.remove(em.find(Member::class.java, member.id))
    
    /**
     * 계정 정보 영구 삭제
     *
     * @param id - PK
     */
    fun deleteById(id: Long) = em.remove(em.find(Member::class.java, id))
    
}