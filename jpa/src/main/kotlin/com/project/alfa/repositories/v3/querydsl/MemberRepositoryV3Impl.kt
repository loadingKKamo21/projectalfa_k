package com.project.alfa.repositories.v3.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class MemberRepositoryV3Impl(private val jpaQueryFactory: JPAQueryFactory) : MemberRepositoryV3Custom