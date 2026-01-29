# ELK Stack + Spring Boot Application 시작 가이드

## 📋 사전 준비

- Docker 및 Docker Compose 설치 필요
- 최소 4GB RAM 권장 (Elasticsearch 실행용)

---

## 🚀 실행 순서

### 1. ELK 스택 시작

```bash
# prepaid 프로젝트 루트에서
cd /Users/juahyun/Desktop/project/prepaid/prepaid

# ELK 스택 실행 (약 1-2분 소요)
docker-compose -f docker-compose-elk.yml up -d

# 로그 확인 (선택사항)
docker-compose -f docker-compose-elk.yml logs -f
```

### 2. 서비스 상태 확인

**Elasticsearch** (약 30초 대기):
```bash
curl http://localhost:9200
# 응답 예시: {"name":"...", "version":{"number":"8.11.0"}}
```

**Kibana** (약 1분 대기):
```bash
curl http://localhost:5601/api/status
# 또는 브라우저에서: http://localhost:5601
```

### 3. Spring Boot 애플리케이션 실행 (프로덕션 모드)

```bash
cd backend

# 프로덕션 프로파일로 실행 (JSON 로그 + 파일 저장)
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

### 4. Kibana에서 로그 확인

#### 4-1. Kibana 접속
브라우저에서 http://localhost:5601 열기

#### 4-2. Data View (Index Pattern) 생성

1. 좌측 메뉴 → **Management** → **Stack Management**
2. **Kibana** → **Data Views** 클릭
3. **Create data view** 클릭
4. 설정:
   - **Name**: `prepaid-logs`
   - **Index pattern**: `prepaid-logs-*`
   - **Timestamp field**: `@timestamp`
5. **Save data view** 클릭

#### 4-3. 로그 조회

1. 좌측 메뉴 → **Discover** 클릭
2. 상단에서 `prepaid-logs` 선택
3. 시간 범위 조정 (우측 상단, 예: Last 15 minutes)
4. 로그 확인!

---

## 🔍 Kibana에서 로그 검색하기

### 기본 검색

**특정 사용자 로그**:
```
userId: "123"
```

**충전 거래만**:
```
txType: "CHARGE"
```

**특정 금액 이상**:
```
amount >= 10000
```

**에러 로그만**:
```
level: "ERROR"
```

### 고급 검색 (KQL)

**특정 사용자의 충전 거래**:
```
userId: "123" AND txType: "CHARGE"
```

**10,000원 이상 충전**:
```
txType: "CHARGE" AND amount >= 10000
```

**특정 Trace ID로 전체 흐름 추적**:
```
traceId: "abc123def456"
```

---

## 📊 유용한 Kibana 기능

### 1. 필터 추가
- 검색 바 아래 **+ Add filter** 클릭
- 필드 선택 (예: `userId`, `txType`)
- 값 입력

### 2. 컬럼 커스터마이징
- 좌측 **Available fields**에서 관심 필드 클릭
- 강조 표시하려면 ⊕ 버튼 클릭
- 예: `timestamp`, `userId`, `txType`, `amount`, `message`

### 3. 시각화 생성
- **Visualize** 메뉴로 이동
- 차트 타입 선택 (Line, Bar, Pie 등)
- 예: 시간대별 거래량, 사용자별 충전 금액

---

## 🛑 중단 및 정리

### ELK 스택 중단
```bash
docker-compose -f docker-compose-elk.yml down
```

### 데이터 포함 완전 삭제
```bash
docker-compose -f docker-compose-elk.yml down -v
```

---

## 🐛 문제 해결

### Elasticsearch가 시작하지 않는 경우
```bash
# 로그 확인
docker logs prepaid-elasticsearch

# 메모리 부족 시 docker-compose-elk.yml에서 조정:
# ES_JAVA_OPTS=-Xms512m -Xmx512m → ES_JAVA_OPTS=-Xms256m -Xmx256m
```

### Filebeat가 로그를 수집하지 않는 경우
```bash
# Filebeat 로그 확인
docker logs prepaid-filebeat

# 로그 파일 권한 확인
ls -la backend/logs/

# 로그 파일 존재 확인
tail -f backend/logs/application.log
```

### Kibana에서 데이터가 보이지 않는 경우
1. Elasticsearch에 데이터가 있는지 확인:
   ```bash
   curl http://localhost:9200/prepaid-logs-*/_search?size=1
   ```
2. Index pattern이 올바른지 확인
3. 시간 범위를 넓게 설정 (예: Last 24 hours)

---

## 📌 참고사항

- **로그 저장 위치**: `backend/logs/application.log`
- **Kibana URL**: http://localhost:5601
- **Elasticsearch URL**: http://localhost:9200
- **로그 보관 기간**: 7일 (logback 설정에서 변경 가능)
- **최대 로그 크기**: 1GB (logback 설정에서 변경 가능)

---

## 🎯 실전 시나리오

### 시나리오: 충전 요청 디버깅

1. **충전 API 호출**:
   ```bash
   curl -X POST http://localhost:8080/api/payments/confirm \
     -H "Content-Type: application/json" \
     -d '{"amount": 10000, "orderId": "test123"}'
   ```

2. **Kibana에서 검색**:
   ```
   orderId: "test123"
   ```

3. **결과 확인**:
   - 요청 수신 로그
   - 충전 처리 로그
   - Kafka 이벤트 발행 로그
   - 모두 같은 `traceId`로 연결됨!

---

**Kibana에서 실시간 로그를 확인하세요!** 🎉
