package com.project.alfa.entities

enum class Role(value: String) {
    
    ADMIN("ROLE_ADMIN"), USER("ROLE_USER");
    
    var value: String = value   //계정 유형
        private set
    
}
