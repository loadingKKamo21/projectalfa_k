package com.project.alfa.repositories.v1

import com.project.alfa.entities.Attachment
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
class AttachmentRepositoryV1 {
    
    @PersistenceContext
    private lateinit var em: EntityManager
    
    /**
     * 첨부파일 저장
     *
     * @param attachment - 첨부파일 정보
     * @return 첨부파일 정보
     */
    fun save(attachment: Attachment): Attachment {
        em.persist(attachment)
        return attachment
    }
    
    /**
     * 첨부파일 다중 저장
     *
     * @param attachments - 첨부파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    fun saveAll(attachments: List<Attachment>): List<Attachment> {
        for (attachment in attachments)
            em.persist(attachment)
        return attachments
    }
    
    /**
     * 첨부파일 다중 저장
     *
     * @param attachments - 첨부파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    fun saveAllAndFlush(attachments: List<Attachment>): List<Attachment> {
        for (attachment in attachments)
            em.persist(attachment)
        em.flush()
        return attachments
    }
    
    /**
     * 첨부파일 정보 조회
     *
     * @param id - PK
     * @return 첨부파일 정보
     */
    fun findById(id: Long): Optional<Attachment> = Optional.ofNullable(
            em.createQuery("SELECT a FROM Attachment a WHERE a.id = :id", Attachment::class.java)
                    .setParameter("id", id)
                    .resultList.stream().findFirst().orElse(null)
    )
    
    /**
     * 첨부파일 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보
     */
    fun findById(id: Long, deleteYn: Boolean): Optional<Attachment> = Optional.ofNullable(
            em.createQuery("SELECT a FROM Attachment a WHERE a.id = :id AND a.deleteYn = :deleteYn",
                           Attachment::class.java)
                    .setParameter("id", id)
                    .setParameter("deleteYn", deleteYn)
                    .resultList.stream().findFirst().orElse(null)
    )
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @return 첨부파일 정보 목록
     */
    fun findAll(): List<Attachment> = em.createQuery("SELECT a FROM Attachment a", Attachment::class.java).resultList
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    fun findAll(deleteYn: Boolean): List<Attachment> =
            em.createQuery("SELECT a FROM Attachment a WHERE a.deleteYn = :deleteYn", Attachment::class.java)
                    .setParameter("deleteYn", deleteYn)
                    .resultList
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 첨부파일 정보 목록
     */
    fun findAll(ids: List<Long>): List<Attachment> =
            if (ids.isEmpty())
                emptyList()
            else em.createQuery("SELECT a FROM Attachment a WHERE a.id IN :ids", Attachment::class.java)
                    .setParameter("ids", ids)
                    .resultList
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    fun findAll(ids: List<Long>, deleteYn: Boolean): List<Attachment> =
            if (ids.isEmpty())
                emptyList()
            else em.createQuery("SELECT a FROM Attachment a WHERE a.id IN :ids AND a.deleteYn = :deleteYn",
                                Attachment::class.java)
                    .setParameter("ids", ids)
                    .setParameter("deleteYn", deleteYn)
                    .resultList
    
    /**
     * 게시글 기준 첨부파일 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 첨부파일 정보 목록
     */
    fun findAll(postId: Long): List<Attachment> =
            em.createQuery("SELECT a FROM Attachment a WHERE a.post.id = :postId", Attachment::class.java)
                    .setParameter("postId", postId)
                    .resultList
    
    /**
     * 게시글 기준 첨부파일 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    fun findAll(postId: Long, deleteYn: Boolean): List<Attachment> =
            em.createQuery("SELECT a FROM Attachment a WHERE a.post.id = :postId AND a.deleteYn = :deleteYn",
                           Attachment::class.java)
                    .setParameter("postId", postId)
                    .setParameter("deleteYn", deleteYn)
                    .resultList
    
    /**
     * 첨부파일 정보 영구 삭제
     *
     * @param attachment - 첨부파일 정보
     */
    fun delete(attachment: Attachment) = em.remove(em.find(Attachment::class.java, attachment.id))
    
    /**
     * 첨부파일 정보 영구 삭제
     *
     * @param id - PK
     */
    fun deleteById(id: Long) = em.remove(em.find(Attachment::class.java, id))
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param attachments - 첨부파일 정보 목록
     */
    fun deleteAll(attachments: List<Attachment>) {
        for (attachment in attachments)
            em.remove(attachment)
    }
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param attachments - 첨부파일 정보 목록
     */
    fun deleteAllInBatch(attachments: List<Attachment>) {
        val ids = attachments.map { it.id }
        em.createQuery("DELETE FROM Attachment a WHERE a.id IN :ids").setParameter("ids", ids).executeUpdate()
    }
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    fun deleteAllById(ids: List<Long>) {
        for (id in ids)
            em.remove(em.find(Attachment::class.java, id))
    }
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    fun deleteAllByIdInBatch(ids: List<Long>) =
            em.createQuery("DELETE FROM Attachment a WHERE a.id IN :ids").setParameter("ids", ids).executeUpdate()
    
}