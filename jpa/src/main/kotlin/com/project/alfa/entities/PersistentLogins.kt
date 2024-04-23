package com.project.alfa.entities

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class PersistentLogins {
    
    @Id
    @Column(length = 64)
    var series: String? = null
    
    @Column(nullable = false, length = 64)
    var username: String? = null
    
    @Column(nullable = false, length = 64)
    var token: String? = null
    
    @Column(nullable = false, length = 64)
    var lastUsed: LocalDateTime? = null
    
}