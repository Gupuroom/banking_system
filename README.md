# Banking Application

간단한 은행 애플리케이션 API 서버입니다. 계좌 관리, 입출금, 송금 등의 기본적인 은행 기능을 제공합니다.

## 기술 스택

- Java 17
- Spring Boot 3.3.6
- Spring Data JPA
- MariaDB
- Docker & Docker Compose
- Gradle (멀티 모듈)
- JUnit 5

## 프로젝트 구조

```
banking/
├── api/                 # API 모듈 (Controller, DTO)
├── domain/             # 도메인 모듈 (Entity, Repository, Service)
└── core/               # 공통 모듈 (Exception, Util)
```

## 주요 기능

- 계좌 관리
  - 계좌 생성/삭제
  - 계좌 조회
  - 계좌 타입별 한도 설정
- 거래 관리
  - 입금
  - 출금
  - 송금
  - 거래 내역 조회

## 실행 방법

### Docker Compose로 실행
0. 프로젝트 루트 디렉토리에서 ROOT 모듈의 실행 가능한 JAR 파일을 생성합니다:


1. API 모듈 디렉토리로 이동:

   ```bash
   cd api
   ```

2. Docker Compose로 서버 실행:

   ```bash
   docker compose up --build -d
   ```

3. 서버 접속 정보:

   - API 서버: http://localhost:8080
   - MariaDB: localhost:3306

4. 서버 종료:
   ```bash
   docker compose down
   ```

## API 명세

API의 상세 명세는 Swagger UI를 통해 확인할 수 있습니다:

- URL: http://localhost:8080/swagger-ui/index.html

## 동시성 처리

1. 비관적 락(Pessimistic Lock)

   - `@Lock(LockModeType.PESSIMISTIC_WRITE)`를 통한 동시성 제어
   - 데이터베이스 레벨에서 락 획득
   - 동시 수정 시 즉시 예외 발생

2. 동시성 테스트
   - `TransactionConcurrencyTest`를 통한 검증
   - 다중 스레드 환경에서의 정확성 확인