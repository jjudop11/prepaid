# 환경 변수 설정 가이드

## 1. .env 파일 생성

프로젝트 루트에서 `.env.example`을 복사하여 `.env` 파일을 만드세요:

```bash
cd /Users/juahyun/Desktop/project/prepaid/prepaid
cp .env.example .env
```

## 2. API 키 발급 및 설정

### 네이버 로그인 설정

1. **네이버 개발자 센터** 접속: https://developers.naver.com/apps/
2. **애플리케이션 등록**
   - 애플리케이션 이름: Prepaid Platform (원하는 이름)
   - 사용 API: 네이버 로그인
3. **API 설정**
   - 서비스 URL: `http://localhost:8080`
   - Callback URL: `http://localhost:8080/login/oauth2/code/naver`
4. **키 복사**
   - Client ID와 Client Secret을 `.env` 파일에 입력

```bash
NAVER_CLIENT_ID=발급받은_CLIENT_ID
NAVER_CLIENT_SECRET=발급받은_CLIENT_SECRET
```

### Toss Payments 설정

1. **Toss Payments 개발자 센터** 접속: https://developers.tosspayments.com/
2. **회원가입 및 로그인**
3. **내 개발정보 > API 키** 메뉴
4. **테스트 키 발급**
   - 테스트 모드 Client Key (test_ck_로 시작)
   - 테스트 모드 Secret Key (test_sk_로 시작)
5. **키 복사**

```bash
TOSS_CLIENT_KEY=test_ck_발급받은_키
TOSS_SECRET_KEY=test_sk_발급받은_키
```

## 3. .env 파일 예시 (실제 키 입력)

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5433/prepaid_db
DB_USERNAME=postgres
DB_PASSWORD=mypassword123

# Redis
REDIS_HOST=localhost
REDIS_PORT=6380
REDIS_PASSWORD=

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:29092

# JWT
JWT_SECRET=my-super-secret-key-256-bits-long-for-production-use-only
JWT_EXPIRATION=1800000
JWT_REFRESH_EXPIRATION=604800000

# Naver OAuth
NAVER_CLIENT_ID=ABC123XYZ789
NAVER_CLIENT_SECRET=secret1234567890

# Toss Payments
TOSS_CLIENT_KEY=test_ck_abcdefghijklmnop
TOSS_SECRET_KEY=test_sk_1234567890abcdefghijklmnop
TOSS_API_URL=https://api.tosspayments.com/v1/payments
```

## 4. 애플리케이션 실행

### 환경변수 파일 로드 방법

#### 방법 1: IntelliJ IDEA
1. Run > Edit Configurations
2. Environment variables 입력란에:
   ```
   DB_URL=jdbc:postgresql://localhost:5433/prepaid_db;DB_USERNAME=postgres;...
   ```
   또는 **EnvFile 플러그인** 설치 후 `.env` 파일 지정

#### 방법 2: Gradle 실행 (추천)
```bash
# .env 파일을 자동으로 읽어서 실행
cd backend
export $(cat ../.env | xargs) && ./gradlew bootRun
```

#### 방법 3: 직접 환경변수 설정
```bash
export DB_URL=jdbc:postgresql://localhost:5433/prepaid_db
export DB_USERNAME=postgres
export NAVER_CLIENT_ID=your_client_id
export TOSS_SECRET_KEY=your_secret_key
# ... 모든 환경변수 설정

./gradlew bootRun
```

## 5. 검증

애플리케이션 실행 후 다음을 확인하세요:

### 네이버 로그인 테스트
```
http://localhost:8080/oauth2/authorization/naver
```
→ 네이버 로그인 페이지로 리다이렉트 확인

### Toss Payments API 테스트
```bash
curl -X POST http://localhost:8080/api/payments/confirm \
  -H "Content-Type: application/json" \
  -d '{
    "paymentKey": "test_key",
    "orderId": "test_order_123",
    "amount": 10000
  }'
```

## ⚠️  보안 주의사항

1. **절대 .env 파일을 Git에 커밋하지 마세요!**
   - `.gitignore`에 이미 포함됨
   
2. **프로덕션에서는 실제 키 사용**
   - Toss: 실제 Secret Key (live_sk_로 시작)
   - JWT_SECRET: 256bit 이상의 랜덤 문자열

3. **환경변수 우선순위**
   ```
   시스템 환경변수 > .env 파일 > application.yml 기본값
   ```

## 문제 해결

### "환경변수를 찾을 수 없습니다" 에러
→ .env 파일이 제대로 로드되었는지 확인
→ `export $(cat .env | xargs)` 명령 재실행

### "Invalid Client ID" 에러 (네이버)
→ NAVER_CLIENT_ID가 정확한지 확인
→ Callback URL이 정확히 설정되었는지 확인

### "Invalid API Key" 에러 (Toss)
→ TOSS_SECRET_KEY가 정확한지 확인
→ 테스트 키(test_sk_)를 사용 중인지 확인
