# MRAP

**MRAP(Market Risk Assessment Portfolio)**는 미국 주식 데이터를 활용한 모의 투자 및 개인 포트폴리오 관리 웹 애플리케이션입니다. 사용자는 회원가입과 로그인을 거친 뒤 종목을 검색하고, 보유 종목을 관리하며, 매수·매도 내역과 포트폴리오 수익률을 확인할 수 있습니다.

이 프로젝트는 Spring Boot 기반의 REST API 서버와 정적 웹 화면으로 구성되어 있습니다. 사용자 및 포트폴리오 데이터는 TiDB Cloud에 저장하고, 주식 데이터는 외부 금융 API에서 조회합니다.

## 주요 기능

### 회원 및 인증

- 회원가입 및 아이디 중복 확인

- BCrypt 기반 비밀번호 암호화

- JWT 기반 로그인 및 요청 인증

- 로그아웃 처리

- 회원 탈퇴

### 종목 조회

- Finnhub API를 이용한 종목 현재가 조회

- 티커 입력을 통한 종목 검색

- TradingView Widget을 이용한 종목 차트 표시

### 실시간 랭킹

- Alpha Vantage API를 이용한 거래량 상위 종목 조회

- 급상승 종목 조회

- 급하락 종목 조회

- 각 랭킹별 최대 20개 항목 표시

- 외부 API 호출 실패 및 호출 제한 응답 처리

Alpha Vantage의 `TOP_GAINERS_LOSERS` 엔드포인트는 미국 시장의 상승률 상위 종목, 하락률 상위 종목, 거래량 기준 주요 종목을 제공합니다. 무료 API 사용량과 데이터 제공 시점은 서비스 정책의 영향을 받습니다.

### 모의 투자

- 포트폴리오에 종목 추가

- 같은 종목 추가 매수 시 평균 매수 단가 재계산

- 보유 수량보다 많은 매도 요청 검증

- 전량 매도 및 부분 매도

- 매수·매도 거래 내역 기록

### 포트폴리오 분석

- 보유 종목과 보유 수량 조회

- 평균 매수 단가와 현재가 조회

- 종목별 평가 금액과 수익률 계산

- 전체 투자 원금과 평가 금액 계산

- Chart.js 기반 포트폴리오 비중 시각화

### 환율 조회

웹 화면에서 Exchange Rate API를 호출하여 USD 자산을 KRW로 환산합니다. 환율 API 호출은 백엔드가 아니라 프론트엔드에서 수행합니다.

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| 언어 | Java 21, JavaScript |
| 백엔드 | Spring Boot, Spring MVC, Spring Security |
| 인증 | JWT, BCrypt |
| 데이터 접근 | Spring Data JPA, Hibernate |
| 배치 | Spring Batch |
| 데이터베이스 | TiDB Cloud(MySQL 호환) |
| 프론트엔드 | HTML, CSS, JavaScript, Bootstrap |
| 시각화 | Chart.js, TradingView Widget |
| 주식 데이터 | Alpha Vantage API, Finnhub API |
| 빌드 | Gradle |

## 시스템 구조

```
┌──────────────────────────────┐
│          Web Browser          │
│   HTML / CSS / JavaScript     │
│ Bootstrap / Chart.js / TV     │
└──────────────┬───────────────┘
               │ HTTP REST API
               ▼
┌──────────────────────────────┐
│          Spring Boot          │
│                              │
│ Controller                   │
│      ↓                       │
│ Service                      │
│      ↓                       │
│ Repository                   │
│                              │
│ Spring Security / JWT        │
└──────────┬───────────┬───────┘
           │           │
           ▼           ▼
┌────────────────┐  ┌─────────────────────┐
│   TiDB Cloud   │  │    External APIs    │
│                │  │                     │
│ User           │  │ Finnhub             │
│ Portfolio      │  │ Alpha Vantage       │
│ PortfolioItem  │  │ Exchange Rate API   │
│ TradeHistory   │  └─────────────────────┘
│ Stock          │
└────────────────┘
```

종목 검색과 포트폴리오 현재가 조회는 Finnhub을 사용합니다. 랭킹 조회는 Alpha Vantage를 사용합니다. 환율 조회는 프론트엔드가 Exchange Rate API를 직접 호출합니다.

## 데이터 모델

```
User
 ├── Portfolio
 │    └── PortfolioItem ─── Stock
 └── TradeHistory
```

주요 엔티티는 다음과 같습니다.

| 엔티티 | 설명 |
| --- | --- |
| `User` | 로그인 계정, 암호화된 비밀번호, 사용자 권한 저장 |
| `Portfolio` | 사용자별 포트폴리오 저장 |
| `PortfolioItem` | 포트폴리오에 포함된 종목, 수량, 평균 매수 단가 저장 |
| `Stock` | 티커와 종목 기본 정보 저장 |
| `TradeHistory` | 매수·매도 거래 내역과 거래 일시 저장 |
| `RiskMetric` | 위험 지표 데이터 저장을 위한 엔티티 |
| `UserRiskSetting` | 사용자별 위험 설정 저장 |
| `UserRiskHistory` | 사용자 위험 설정 이력 저장 |

## 주요 API

인증이 필요한 API는 로그인 후 발급된 JWT를 다음 헤더에 넣어 호출합니다.

```
Authorization: Bearer {JWT_TOKEN}
```

| Method | Endpoint | 설명 |
| --- | --- | --- |
| `POST` | `/api/auth/signup` | 회원가입 |
| `GET` | `/api/auth/check-id` | 아이디 중복 확인 |
| `POST` | `/api/auth/login` | 로그인 및 JWT 발급 |
| `DELETE` | `/api/auth/withdraw` | 회원 탈퇴 |
| `GET` | `/api/stocks/{ticker}` | Finnhub 기반 현재가 조회 |
| `GET` | `/api/ranking/active` | 거래량 상위 랭킹 조회 |
| `GET` | `/api/ranking/rise` | 급상승 랭킹 조회 |
| `GET` | `/api/ranking/fall` | 급하락 랭킹 조회 |
| `GET` | `/api/portfolio/items` | 내 포트폴리오 조회 |
| `POST` | `/api/portfolio/items` | 포트폴리오 종목 추가 또는 추가 매수 |
| `POST` | `/api/portfolio/items/sell` | 종목 매도 |
| `DELETE` | `/api/portfolio/items/{itemId}` | 포트폴리오 종목 삭제 |
| `GET` | `/api/history` | 내 거래 내역 조회 |
| `DELETE` | `/api/history/{id}` | 거래 내역 삭제 |
| `DELETE` | `/api/history/all` | 내 거래 내역 전체 삭제 |

## 프로젝트 구조

```
src/main/java/com/marketrisk
├── common
│   ├── entity
│   │   └── BaseEntity.java
│   └── GlobalExceptionHandler.java
├── controller
│   ├── auth
│   ├── portfolio
│   └── stock
├── dto
│   ├── auth
│   └── portfolio
├── entity
│   ├── alert
│   ├── batch
│   ├── portfolio
│   ├── stock
│   └── user
├── repository
│   ├── alert
│   ├── batch
│   ├── portfolio
│   ├── stock
│   └── user
├── scheduler
├── security
├── service
│   ├── auth
│   ├── portfolio
│   └── stock
└── MrapApplication.java

src/main/resources
├── application-template.yml
└── static
    ├── css/style.css
    ├── index.html
    └── js/app.js
```

## 환경 설정

실제 데이터베이스 비밀번호, JWT 시크릿, 외부 API 키는 저장소에 포함하지 않습니다. 저장소에는 환경변수 이름만 포함된 `application-template.yml`을 제공합니다.

필요한 환경변수는 다음과 같습니다.

```
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
ALPHA_VANTAGE_KEY
FINNHUB_KEY
```

`src/main/resources/application.yml`은 다음과 같은 환경변수 참조 형태로 구성합니다.

```yaml
spring:
  application:
    name: MRAP

  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect

  batch:
    jdbc:
      initialize-schema: always

jwt:
  secret: ${JWT_SECRET}
  expiration-time: ${JWT_EXPIRATION_TIME:86400000}

alphavantage:
  api-key: ${ALPHA_VANTAGE_KEY}

finnhub:
  api-key: ${FINNHUB_KEY}
```

`application.yml`은 `.gitignore`에 등록되어 있으므로 저장소에 커밋하지 않습니다.

## 실행 방법

### 1. 저장소 복제

```bash
git clone https://github.com/kkujuaz8-code/MRAP.git
cd MRAP
```

### 2. 설정 파일 생성

Linux 또는 macOS에서는 다음 명령으로 템플릿을 복사할 수 있습니다.

```bash
cp src/main/resources/application-template.yml \
   src/main/resources/application.yml
```

Windows PowerShell에서는 다음 명령을 사용합니다.

```
Copy-Item `
  src/main/resources/application-template.yml `
  src/main/resources/application.yml
```

### 3. 환경변수 설정

Linux 또는 macOS의 예시는 다음과 같습니다.

```bash
export DB_URL='jdbc:mysql://your-tidb-host:4000/test?sslMode=VERIFY_IDENTITY'
export DB_USERNAME='your-db-username'
export DB_PASSWORD='your-db-password'
export JWT_SECRET='your-long-random-jwt-secret'
export ALPHA_VANTAGE_KEY='your-alpha-vantage-key'
export FINNHUB_KEY='your-finnhub-key'
```

Windows PowerShell의 예시는 다음과 같습니다.

```
$env:DB_URL = 'jdbc:mysql://your-tidb-host:4000/test?sslMode=VERIFY_IDENTITY'
$env:DB_USERNAME = 'your-db-username'
$env:DB_PASSWORD = 'your-db-password'
$env:JWT_SECRET = 'your-long-random-jwt-secret'
$env:ALPHA_VANTAGE_KEY = 'your-alpha-vantage-key'
$env:FINNHUB_KEY = 'your-finnhub-key'
```

Eclipse를 사용하는 경우 `Run Configurations → Spring Boot App → Environment`에서 같은 이름으로 환경변수를 등록할 수 있습니다.

### 4. 애플리케이션 실행

Linux, macOS, Windows Git Bash:

```bash
./gradlew bootRun
```

Windows PowerShell 또는 명령 프롬프트:

```
.\gradlew.bat bootRun
```

애플리케이션은 기본적으로 다음 주소에서 실행됩니다.

```
http://localhost:8080
```

## 보안 주의사항

다음 파일은 실제 인증정보를 포함할 수 있으므로 저장소에 커밋하지 않습니다.

```
src/main/resources/application.yml
.env
.env.*
```

커밋 전에 다음 명령으로 `application.yml`이 Git에서 제외되는지 확인할 수 있습니다.

```bash
git check-ignore -v src/main/resources/application.yml
```

이미 외부에 노출된 DB 비밀번호나 API 키는 파일에서 삭제하는 것만으로 충분하지 않습니다. 해당 서비스에서 기존 값을 폐기하고 새 값을 발급해야 합니다. JWT 시크릿을 변경하면 기존 JWT가 무효화되므로 사용자는 다시 로그인해야 합니다.

## 구현 과정에서 해결한 문제

### 외부 API 장애 처리

외부 금융 API가 실패하거나 API 호출 제한을 반환하는 경우를 구분하여 처리했습니다. 랭킹 서비스는 Alpha Vantage의 오류 응답을 확인하고, 정상적인 랭킹 배열이 없으면 적절한 HTTP 오류를 반환합니다.

### 랭킹 데이터 개수 제한

랭킹 응답은 `RANKING_LIMIT` 상수를 기준으로 최대 20개 항목만 반환하도록 구성했습니다.

### 종목 검색 API 분리

종목 검색은 랭킹 API와 분리했습니다. 검색 요청은 `/api/stocks/{ticker}`로 전달되고, `StockController`와 `StockService`가 Finnhub을 통해 현재가를 조회합니다.

### 평균 매수 단가 계산

동일 종목을 추가 매수하면 기존 투자 금액과 추가 매수 금액을 합산한 뒤 전체 수량으로 나누어 새로운 평균 매수 단가를 계산합니다.

```
새 평균 매수 단가
=
(기존 수량 × 기존 평균 매수 단가
 + 추가 수량 × 추가 매수 가격 )
÷
(기존 수량 + 추가 수량)
```

### 거래 데이터 일관성

매수 및 매도 과정에서 포트폴리오 보유 정보와 거래 내역이 함께 변경되도록 서비스 메서드에 트랜잭션을 적용했습니다.

## 라이선스 및 데이터 이용 주의

이 프로젝트는 학습과 포트폴리오 목적의 모의 투자 서비스입니다. 실제 주문이나 투자 자문을 제공하지 않습니다. 외부 금융 API의 사용량 제한, 데이터 제공 시점, 이용 약관은 각 제공자의 정책을 따릅니다.

## References

[1]: https://www.alphavantage.co/documentation/ "Alpha Vantage API Documentation"

[2]: https://www.alphavantage.co/support/#support "Alpha Vantage Support and API Limits"

[3]: https://finnhub.io/docs/api "Finnhub API Documentation"

[4]: https://docs.pingcap.com/tidbcloud/secure-connections-to-serverless-clusters/ "TiDB Cloud Secure Connections"

[5]: https://spring.io/projects/spring-boot "Spring Boot Project"

[6]: https://spring.io/projects/spring-security "Spring Security Project"
