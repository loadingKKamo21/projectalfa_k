package com.project.alfa.repositories.v3.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class AttachmentRepositoryV3Impl(private val jpaQueryFactory: JPAQueryFactory) : AttachmentRepositoryV3Custom