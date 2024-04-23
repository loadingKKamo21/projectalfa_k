package com.project.alfa.repositories.dto

class SearchParam {
    
    constructor() : this(null, null)
    
    constructor(
            searchCondition: String? = null,
            searchKeyword: String? = null
    ) {
        this.searchCondition = searchCondition
        this.searchKeyword = searchKeyword
        this.keywords = if (searchKeyword.isNullOrBlank()) emptyList() else searchKeyword.split("\\s+".toRegex())
    }
    
    var searchCondition: String?    //검색 조건
    
    var searchKeyword: String?      //검색 키워드
    
    var keywords: List<String>      //공백 기준 검색 키워드 분리 목록
    
}