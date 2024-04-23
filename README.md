# Project alfa

## 개요
Kotlin / Spring Boot 기반 CRUD 프로젝트

## 목차
1. [개발 환경](#개발-환경)
2. [설계 목표](#설계-목표)
3. [프로젝트 정보](#프로젝트-정보)
	- [구조](#-구조-)
		- [공통](#-공통-)
		- [JPA](#-JPA-)
		- [MyBatis](#-MyBatis-)
	- [코드 예시](#-코드-예시-)
	- [설명](#-설명-)
4. [프로젝트 실행](#프로젝트-실행)

## 개발 환경

#### Backend
- Kotlin, Spring Framework, JPA(+ Querydsl) or MyBatis

#### DB
- H2 Database, Redis
- Embedded Redis (for Test)

#### Tool
- IntelliJ IDEA, Gradle

## 설계 목표
- 스프링 부트를 활용한 CRUD API 애플리케이션
- 같은 기능/동작을 목표로 JPA, MyBatis 프로젝트 분리
	- JPA: Data JPA 및 Querydsl 활용
		- V1: EntityManager 사용
		- V2: JpaRepository + Specification 사용
		- V3: JpaRepository + Querydsl 사용
	- MyBatis: Mapper 활용
- 게시글/댓글 페이징, 게시글 검색 기능
- 스프링 시큐리티 활용 계정 2타입 설계: 이메일 인증 계정, OAuth2 계정
- 파일 업로드/다운로드
- 스프링 캐시 + Redis: 게시글 조회 수 증가 로직
- Repository / Service / Controller 테스트 코드 작성

## 프로젝트 정보

#### [ 구조 ]

###### [ 공통 ]
```
+---main
|   +---java
|   |   \---com
|   |       \---project
|   |           \---alfa
|   |               |   AlfaApplication.kt
|   |               |   InitDb.kt	-> 더미 데이터 생성/저장
|   |               +---aop	//로깅 AOP
|   |               |   |   Pointcuts.kt
|   |               |   |   ProjectAspects.kt
|   |               |   +---annotation
|   |               |   |       ClassAop.kt
|   |               |   |       MethodAop.kt
|   |               |   \---trace
|   |               |       |   TraceId.kt
|   |               |       |   TraceStatus.kt
|   |               |       \---logtrace
|   |               |               LogTrace.kt
|   |               |               ThreadLocalLogTrace.kt
|   |               +---config	//설정
|   |               |       AopConfig.kt
|   |               |       CacheConfig.kt	-> 캐시 설정
|   |               |       ProjectConfig.kt
|   |               |       RedisConfig.kt	-> Redis 설정
|   |               |       SecurityConfig.kt	-> 시큐리티 설정
|   |               |       WebConfig.kt
|   |               +---controllers	//컨트롤러
|   |               |   |   MainController.kt
|   |               |   \---api
|   |               |           AttachmentApiController.kt
|   |               |           CommentApiController.kt
|   |               |           MemberApiController.kt
|   |               |           PostApiController.kt
|   |               +---entities	//엔티티
|   |               |       Attachment.kt	-> 첨부파일(UploadFile 구현체)
|   |               |       AuthInfo.kt	-> 인증 정보(Member 필드)
|   |               |       Comment.kt	-> 댓글
|   |               |       Member.kt	-> 계정
|   |               |       Post.kt	-> 게시글
|   |               |       Role.kt	-> 계정 유형(enum)
|   |               |       UploadFile.kt	-> 업로드 파일(abstract)
|   |               +---error	//에러 or 예외 관련
|   |               |   |   ErrorResponse.kt	->에러 정보 전달
|   |               |   |   GlobalExceptionHandler.kt
|   |               |   \---exception
|   |               |           BusinessException.kt
|   |               |           EntityNotFoundException.kt
|   |               |           ErrorCode.kt	->에러 코드
|   |               |           InvalidValueException.kt
|   |               +---interceptor	//인터셉터
|   |               |       LogInterceptor.kt
|   |               +---repositories	//리포지토리
|   |               |   +---dto
|   |               |   |       SearchParam.kt	-> 검색 파라미터
|   |               +---security	//시큐리티
|   |               |   |   CustomAuthenticationFailureHandler.kt
|   |               |   |   CustomAuthenticationProvider.kt
|   |               |   |   CustomUserDetails.kt	-> UserDetails, OAuth2User 구현체
|   |               |   |   CustomUserDetailsService.kt	-> UserDetailsService 구현체
|   |               |   \---oauth2
|   |               |       |   CustomOAuth2UserService.kt	-> OAuth2 인증 서비스
|   |               |       \---provider
|   |               |               GoogleUserInfo.kt
|   |               |               OAuth2UserInfo.kt -> OAuth2 인증 정보 인터페이스
|   |               +---services	//서비스
|   |               |   |   AttachmentService.kt
|   |               |   |   CommentService.kt
|   |               |   |   MemberService.kt
|   |               |   |   PostService.kt
|   |               |   \---dto
|   |               |           AttachmentResponseDto.kt
|   |               |           CommentRequestDto.kt
|   |               |           CommentResponseDto.kt
|   |               |           MemberInfoResponseDto.kt
|   |               |           MemberJoinRequestDto.kt
|   |               |           MemberUpdateRequestDto.kt
|   |               |           PostRequestDto.kt
|   |               |           PostResponseDto.kt
|   |               |           RegEx.kt	-> 필드값 확인용 정규표현식 모음
|   |               \---utils
|   |                       EmailSender.kt	-> 이메일 전송
|   |                       FileUtil.kt	-> 업로드 파일 관련
|   |                       RandomGenerator.kt	-> 랜덤 데이터 생성
|   \---resources
|       |   application.yml
```

###### [ JPA ]
```
+---main
|   +---java
|   |   \---com
|   |       \---project
|   |           \---alfa
|   |               +---entities
|   |               |       BaseTimeEntity.kt
|   |               |       PersistentLogins.kt	-> 시큐리티 remember-me
|   |               +---repositories	//JPA Repository
|   |               |   +---v1	//EntityManager 사용
|   |               |   |       AttachmentRepositoryV1.kt
|   |               |   |       CommentRepositoryV1.kt
|   |               |   |       MemberRepositoryV1.kt
|   |               |   |       PostRepositoryV1.kt
|   |               |   +---v2	//JpaRepository + Specification 사용
|   |               |   |   |   AttachmentJpaRepository.kt
|   |               |   |   |   AttachmentRepositoryV2.kt
|   |               |   |   |   CommentJpaRepository.kt
|   |               |   |   |   CommentRepositoryV2.kt
|   |               |   |   |   MemberJpaRepository.kt
|   |               |   |   |   MemberRepositoryV2.kt
|   |               |   |   |   PostJpaRepository.kt
|   |               |   |   |   PostRepositoryV2.kt
|   |               |   |   \---specification
|   |               |   |           PostSpecification.kt	//게시글 검색 및 페이징
|   |               |   \---v3	//JpaRepository + Querydsl 사용
|   |               |       |   AttachmentRepositoryV3.kt
|   |               |       |   CommentRepositoryV3.kt
|   |               |       |   MemberRepositoryV3.kt
|   |               |       |   PostRepositoryV3.kt
|   |               |       \---querydsl
|   |               |               AttachmentRepositoryV3Custom.kt
|   |               |               AttachmentRepositoryV3Impl.kt
|   |               |               CommentRepositoryV3Custom.kt
|   |               |               CommentRepositoryV3Impl.kt
|   |               |               MemberRepositoryV3Custom.kt
|   |               |               MemberRepositoryV3Impl.kt
|   |               |               PostRepositoryV3Custom.kt
|   |               |               PostRepositoryV3Impl.kt
```

###### [ MyBatis ]
```
+---main
|   +---java
|   |   \---com
|   |       \---project
|   |           \---alfa
|   |               +---entities
|   |               |       EnumTypeHandler.kt	-> Kotlin-MySQL 간 enum 타입 변환
|   |               +---repositories
|   |               |   |   AttachmentRepository.kt
|   |               |   |   CommentRepository.kt
|   |               |   |   MemberRepository.kt
|   |               |   |   PostRepository.kt
|   |               |   \---mybatis	//MyBatis Repository
|   |               |           AttachmentMapper.kt
|   |               |           AttachmentRepositoryImpl.kt
|   |               |           CommentMapper.kt
|   |               |           CommentRepositoryImpl.kt
|   |               |           MemberMapper.kt
|   |               |           MemberRepositoryImpl.kt
|   |               |           MyBatisTokenRepositoryImpl.kt
|   |               |           PersistentTokenMapper.kt
|   |               |           PostMapper.kt
|   |               |           PostRepositoryImpl.kt
|   \---resources
|       +---mappers	//MyBatis Mapper.xml
|       |       AttachmentMapper.xml
|       |       CommentMapper.xml
|       |       MemberMapper.xml
|       |       PersistentTokenMapper.xml	-> 시큐리티 remember-me
|       |       PostMapper.xml
```

#### [ 코드 예시 ]
![k_mybatis2](https://github.com/loadingKKamo21/projectalfa_k/assets/90470901/06453aff-f742-4f9e-abbc-88f2967e2a8a)
![k_mybatis1](https://github.com/loadingKKamo21/projectalfa_k/assets/90470901/88044e54-cad4-4ece-9060-c0debc6177d8)
![k_jpa7](https://github.com/loadingKKamo21/projectalfa_k/assets/90470901/007e20bf-e0a8-422e-a0eb-4494da174882)
![k_jpa6](https://github.com/loadingKKamo21/projectalfa_k/assets/90470901/7d83ed3e-9f63-4ef4-86e5-9d3a77763ebb)
![k_jpa5](https://github.com/loadingKKamo21/projectalfa_k/assets/90470901/fc9ccb2c-3255-46a3-9995-12b908b826a0)
![k_jpa4](https://github.com/loadingKKamo21/projectalfa_k/assets/90470901/6b9bbab4-238a-44e2-8f05-f7803b26d482)
![k_jpa-test2](https://github.com/loadingKKamo21/projectalfa_k/assets/90470901/efe7ba82-fc2d-49af-b4c9-c5557504814c)
![k_jpa-test1](https://github.com/loadingKKamo21/projectalfa_k/assets/90470901/1ee963c7-eba0-4266-be3a-b6c964a4e736)
![k_jpa3](https://github.com/loadingKKamo21/projectalfa_k/assets/90470901/e40cb6dd-7229-460c-8f47-50b4eacf4ebd)
![k_jpa2](https://github.com/loadingKKamo21/projectalfa_k/assets/90470901/3d9436c1-e2d5-4bb0-abad-5548eff15333)
![k_jpa1](https://github.com/loadingKKamo21/projectalfa_k/assets/90470901/d539136a-9b04-4b65-b496-9cac807e9160)
![k_mybatis3](https://github.com/loadingKKamo21/projectalfa_k/assets/90470901/78b47ee3-17ca-4215-a5b1-6eec87906e04)

#### [ 설명 ]
- 계정, 게시글, 댓글, 첨부파일 Create / Read / Update / Delete
- API 기반 설계
- 게시글/댓글 조회 목록 페이징, 게시글 검색 기능
- 스프링 시큐리티 연동 2가지 계정 가입 방식
	- 아이디(이메일) + 이메일 인증
	- OAuth2
- JPA 프로젝트 OSIV OFF 설정
	- 서비스 레이어 외부로 엔티티 노출 억제
	- 컨트롤러-서비스 전송 간 DTO 사용(MyBatis 프로젝트도 동일한 방식 적용)
	![OSIV](https://github.com/loadingKKamo21/projectalfa_k/assets/90470901/c93031d1-65c8-4990-b8e9-06663dfa03d8)
- Redis 캐시 사용
	- 게시글 조회수 증가 로직
- 컨트롤러/서비스/리포지토리 테스트 코드 작성

## 프로젝트 실행
- 기본 설정값 기반
- application.yml 설정
	- DB: [H2 Database](https://www.h2database.com/html/main.html)와 [Redis](https://redis.io/) 설치/실행
	```
	...
	spring:
		datasource:
			driver-class-name: org.h2.Driver
			url: jdbc:h2:tcp://localhost/~/test;MODE=MYSQL;DATABASE_TO_LOWER=TRUE
			username: sa
			password:
	...
	redis:
		host: localhost
		port: 6379
		password:
		lettuce:
			pool:
				min-idle: 0
				max-idle: 8
				max-active: 8
	...
	```
	- SMTP: 이메일 전송 시 사용, 기본값 Google SMTP
	```
	...
	spring:
		mail:
			host: smtp.gmail.com
			port: 587
			username: { Google Username }
			password: { Google Password }
			properties:
				...
	```
	- OAuth2: 기본값 Google, 타 OAuth2 사용 시 OAuth2UserInfo 구현체 추가 설정 필요
	```
	...
	spring:
		security:
			oauth2:
				client:
					registration:
						google:
							client-id: { Google OAuth 2.0 Client-Id }
							client-secret: { Google OAuth 2.0 Client-Secret }
							scope:
								- email
								- profile
	...
	```
	- File Upload Path: 파일 업로드 경로 등록
	```
	file:
		upload:
			location: { Upload Path }
	```
	- 더미 데이터 추가: InitDb.kt
