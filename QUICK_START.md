# Quick Start Guide

## 1. 환경 설정 (필수!)

```bash
# 1. .env 파일 생성
cp .env.example .env

# 2. .env 파일 편집 (실제 API 키 입력)
vi .env
```

### 입력해야 할 API 키

| 항목 | 발급 위치 | 필수 여부 |
|------|----------|----------|
| NAVER_CLIENT_ID | [네이버 개발자 센터](https://developers.naver.com/apps/) | ✅ 필수 (OAuth 사용 시) |
| NAVER_CLIENT_SECRET | [네이버 개발자 센터](https://developers.naver.com/apps/) | ✅ 필수 (OAuth 사용 시) |
| TOSS_SECRET_KEY | [Toss Payments](https://developers.tosspayments.com/) | ✅ 필수 |
| TOSS_CLIENT_KEY | [Toss Payments](https://developers.tosspayments.com/) | ✅ 필수 |
| JWT_SECRET | 직접 생성 (256bit 랜덤 문자열) | ✅ 필수 |

## 2. 실행

### 방법 1: 자동 스크립트 (추천 ⭐)

```bash
cd backend
./run.sh
```

스크립트가 자동으로:
- .env 파일 존재 확인
- 환경변수 로드
- 필수 키 검증
- 애플리케이션 실행

### 방법 2: 수동 실행

```bash
cd backend

# 환경변수 로드
export $(cat ../.env | grep -v '^#' | xargs)

# 실행
./gradlew bootRun
```

## 3. 접속

```
애플리케이션: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html
```

## 4. 테스트

### 네이버 로그인
```
http://localhost:8080/oauth2/authorization/naver
```

### 일반 회원가입
```bash
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test1234!@#$",
    "email": "test@example.com"
  }'
```

## 문제 해결

### "환경변수를 찾을 수 없습니다"
→ `.env` 파일 생성 및 API 키 입력 확인

### "Invalid Client ID" (네이버)
→ NAVER_CLIENT_ID 확인
→ Callback URL: `http://localhost:8080/login/oauth2/code/naver`

### "Invalid API Key" (Toss)
→ TEST 키(test_sk_, test_ck_) 사용 확인

## 자세한 가이드

- [ENV_SETUP.md](ENV_SETUP.md) - 환경 변수 상세 가이드
- [ELK_SETUP_GUIDE.md](ELK_SETUP_GUIDE.md) - ELK 스택 설정
