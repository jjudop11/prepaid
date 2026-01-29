# 🚀 전체 시스템 실행 가이드

## 📋 실행 전 체크리스트

### 1. 환경변수 설정 확인

#### 백엔드 (.env)
```bash
cd /Users/juahyun/Desktop/project/prepaid/prepaid
cat .env
```

확인 항목:
- ✅ DB_PASSWORD
- ✅ NAVER_CLIENT_ID
- ✅ NAVER_CLIENT_SECRET
- ✅ TOSS_CLIENT_KEY
- ✅ TOSS_SECRET_KEY
- ✅ JWT_SECRET

#### 프론트엔드 (.env.local)
```bash
cd front
cat .env.local
```

확인 항목:
- ✅ NEXT_PUBLIC_API_URL=http://localhost:8080
- ✅ NEXT_PUBLIC_TOSS_CLIENT_KEY (백엔드와 동일한 값)

---

## 🚀 실행 방법

### 방법 1: 자동 스크립트 (추천 ⭐)

```bash
# 프로젝트 루트로 이동
cd /Users/juahyun/Desktop/project/prepaid/prepaid

# 1단계: Docker 컨테이너 실행
export $(cat .env | grep -v '^#' | xargs)
docker-compose up -d

# 2단계: 백엔드 실행 (새 터미널)
cd backend
./run.sh

# 3단계: 프론트엔드 실행 (새 터미널)
cd front
./run.sh
```

### 방법 2: 수동 실행

```bash
# 터미널 1: Docker
cd /Users/juahyun/Desktop/project/prepaid/prepaid
docker-compose up -d

# 터미널 2: 백엔드
cd backend
export $(cat ../.env | grep -v '^#' | xargs)
./gradlew bootRun

# 터미널 3: 프론트엔드
cd front
npm run dev
```

---

## 🌐 접속 주소

서비스 실행 후 다음 주소로 접속하세요:

| 서비스 | 주소 | 설명 |
|--------|------|------|
| **프론트엔드** | http://localhost:3000 | 사용자 UI |
| **백엔드 API** | http://localhost:8080 | REST API |
| **Swagger** | http://localhost:8080/swagger-ui.html | API 문서 |
| **Kafka UI** | http://localhost:8085 | Kafka 관리 |

---

## ✅ 정상 실행 확인

### Docker 컨테이너 상태
```bash
docker-compose ps
```

모든 컨테이너가 `running` 상태여야 합니다:
- prepaid-postgres
- prepaid-redis
- prepaid-kafka
- prepaid-kafka-ui

### 백엔드 로그
```
Started PrepaidPlatformApplication in X seconds
Tomcat started on port(s): 8080 (http)
```

### 프론트엔드 로그
```
- ready started server on 0.0.0.0:3000, url: http://localhost:3000
- Local:        http://localhost:3000
```

---

## 🧪 기능 테스트

### 1. 회원가입
```bash
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test1234!@#$",
    "email": "test@example.com"
  }'
```

### 2. 로그인
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test1234!@#$"
  }'
```

### 3. OAuth 로그인 (브라우저)
```
http://localhost:8080/oauth2/authorization/naver
```

---

## 🔧 문제 해결

### "환경변수를 찾을 수 없습니다"
→ .env 파일 확인 및 export 명령 재실행

### "Connection refused" (PostgreSQL)
→ Docker 컨테이너 상태 확인: `docker-compose ps`

### "invalid_client" (Naver OAuth)
→ NAVER_CLIENT_ID, CLIENT_SECRET 확인

### 프론트엔드 "Network Error"
→ 백엔드가 실행 중인지 확인 (http://localhost:8080/actuator/health)

---

## 🛑 종료 방법

```bash
# 프론트엔드 종료: Ctrl+C

# 백엔드 종료: Ctrl+C

# Docker 컨테이너 종료
docker-compose down

# Docker 볼륨까지 삭제 (주의!)
docker-compose down -v
```

---

## 📝 추가 문서

- [ENV_SETUP.md](ENV_SETUP.md) - 환경변수 상세 가이드
- [QUICK_START.md](QUICK_START.md) - 빠른 시작 가이드
- [ELK_SETUP_GUIDE.md](ELK_SETUP_GUIDE.md) - ELK 스택 설정

---

**준비 완료!** 이제 위 명령어로 실행하세요! 🎉
