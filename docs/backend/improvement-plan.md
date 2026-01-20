# 선불관리 서비스 개선 구현 계획

## 📋 목표

현재 MVP 수준의 선불관리 서비스를 **프로덕션 레디**로 개선합니다. 예외 처리, 금액 검증, 멱등성 보장, 감사 로그 등 핵심 기능을 단계적으로 구현합니다.

---

## 🎯 사용자 확인 필요 사항

> [!IMPORTANT]
> **감사 로그 Elasticsearch 연동**
> - Elasticsearch는 Docker Compose로 로컬 환경에 추가할 예정입니다
> - 실제 데이터 전송은 구현하되, 로컬에서 ELK 스택을 띄우지 않아도 동작하도록 설정합니다
> - 나중에 프로덕션에서 Elasticsearch URL만 설정하면 바로 동작하도록 준비합니다

> [!WARNING]
> **금액 제한 정책**
> - 최소 충전 금액: 1,000원
> - 최대 충전 금액: 1,000,000원
> - 일일 충전 한도: 5,000,000원
> - 위 값들은 application.yml에서 설정 가능하도록 구현합니다

---

## 📦 구현 내역

### Phase 1: 기반 시스템 (P0 - 필수)

#### 1. 예외 처리 시스템

##### [NEW] [common/exception/BusinessException.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/common/exception/BusinessException.java)
- 비즈니스 예외 최상위 클래스
- errorCode, message 포함

##### [NEW] [common/exception/ErrorCode.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/common/exception/ErrorCode.java)
- 에러 코드 Enum
- `INSUFFICIENT_BALANCE`, `WALLET_NOT_FOUND`, `INVALID_AMOUNT` 등

##### [NEW] [common/exception/specific/*.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/common/exception/specific)
- `InsufficientBalanceException`
- `WalletNotFoundException`
- `InvalidAmountException`
- `DuplicateRequestException`

##### [NEW] [common/exception/GlobalExceptionHandler.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/common/exception/GlobalExceptionHandler.java)
- `@ControllerAdvice`
- 예외별 HTTP 상태 코드 매핑
- ErrorResponse 반환

##### [NEW] [common/dto/ErrorResponse.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/common/dto/ErrorResponse.java)
```json
{
  "errorCode": "INSUFFICIENT_BALANCE",
  "message": "잔액이 부족합니다.",
  "timestamp": "2026-01-20T20:55:00"
}
```

##### [MODIFY] [ledger/service/LedgerService.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/ledger/service/LedgerService.java)
- `RuntimeException` → 커스텀 예외로 변경

---

#### 2. 금액 검증

##### [NEW] [payment/validation/PaymentValidator.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/payment/validation/PaymentValidator.java)
- 최소/최대 금액 검증
- 음수 체크
- 일일 한도 확인 (Redis 사용)

##### [MODIFY] [application-local.yml](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/resources/application-local.yml)
```yaml
payment:
  charge:
    min-amount: 1000
    max-amount: 1000000
    daily-limit: 5000000
  use:
    min-amount: 100
    max-amount: 1000000
```

##### [MODIFY] [PaymentService.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/payment/service/PaymentService.java)
- 충전 전 금액 검증 추가

##### [MODIFY] [LedgerService.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/ledger/service/LedgerService.java)
- 사용 전 금액 검증 추가

---

#### 3. 멱등성 구현

##### [NEW] [common/idempotency/IdempotencyService.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/common/idempotency/IdempotencyService.java)
- Redis 기반 중복 요청 체크
- TTL 24시간

##### [NEW] [common/idempotency/IdempotentRequest.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/common/idempotency/IdempotentRequest.java)
```java
@Data
public class IdempotentRequest {
    private String idempotencyKey;
    private String status; // PROCESSING, COMPLETED, FAILED
    private Object result;
}
```

##### [MODIFY] [PaymentController.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/controller/PaymentController.java)
- `@RequestHeader("Idempotency-Key")` 추가
- 중복 요청 체크

---

### Phase 2: 핵심 기능 (P1)

#### 4. 거래 취소/환불

##### [NEW] [payment/service/RefundService.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/payment/service/RefundService.java)
- Toss API 환불 호출
- 지갑 잔액 차감
- 환불 원장 기록

##### [NEW] [payment/dto/RefundRequest.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/payment/dto/RefundRequest.java)
```java
public record RefundRequest(
    String orderId,
    Long amount,
    String cancelReason
) {}
```

##### [MODIFY] [LedgerService.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/ledger/service/LedgerService.java)
- `recordRefund()` 메서드 추가

##### [NEW] [controller/RefundController.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/controller/RefundController.java)
- `POST /api/refunds` 엔드포인트

---

#### 5. 감사 로그 (PostgreSQL + Elasticsearch)

##### [NEW] [audit/domain/AuditLog.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/audit/domain/AuditLog.java)
```java
@Entity
@Table(indexes = {
    @Index(name = "idx_user_timestamp", columnList = "user_id,timestamp")
})
public class AuditLog {
    private Long userId;
    private String action; // CHARGE, USE, REFUND
    private Long amount;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private String result; // SUCCESS, FAILED
    private String errorMessage;
}
```

##### [NEW] [audit/event/AuditEvent.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/audit/event/AuditEvent.java)
- Kafka로 발행할 감사 이벤트

##### [NEW] [audit/service/AuditEventPublisher.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/audit/service/AuditEventPublisher.java)
- `audit-events` 토픽으로 발행

##### [NEW] [audit/consumer/AuditEventConsumer.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/audit/consumer/AuditEventConsumer.java)
- PostgreSQL 저장
- Elasticsearch 전송 (설정 시)

##### [NEW] [audit/config/ElasticsearchConfig.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/audit/config/ElasticsearchConfig.java)
- RestHighLevelClient 설정 (선택적)

##### [NEW] [audit/scheduler/AuditLogArchiver.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/audit/scheduler/AuditLogArchiver.java)
- 매일 자정 실행
- 30일 이상 데이터 삭제 (이미 Elasticsearch에 있음)

##### [MODIFY] [PaymentService.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/payment/service/PaymentService.java)
- 거래 후 감사 이벤트 발행

##### [MODIFY] [docker-compose.yml](file:///Users/juahyun/Desktop/project/prepaid/prepaid/docker-compose.yml)
```yaml
elasticsearch:
  image: elasticsearch:8.11.0
  ports:
    - "9200:9200"
  environment:
    - discovery.type=single-node
    - xpack.security.enabled=false
```

---

### Phase 3: 개발 경험 (P2)

#### 6. API 문서화

##### [MODIFY] [build.gradle](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/build.gradle)
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
```

##### [NEW] [config/OpenApiConfig.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/config/OpenApiConfig.java)
- Swagger UI 설정
- API 정보, 보안 스키마

##### [MODIFY] All Controllers
- `@Operation`, `@ApiResponse` 어노테이션 추가

---

#### 7. 모니터링

##### [MODIFY] [build.gradle](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/build.gradle)
```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-registry-prometheus'
```

##### [MODIFY] [application-local.yml](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/resources/application-local.yml)
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

---

### Phase 4: 확장 기능 (P3)

#### 8. 포인트 만료

##### [NEW] [ledger/scheduler/PointExpirationScheduler.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/ledger/scheduler/PointExpirationScheduler.java)
- 매일 자정 만료 처리

##### [MODIFY] [ChargeLot.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/ledger/domain/ChargeLot.java)
- `expiryDate` 필드 추가

---

#### 9. 보안 개선

##### [NEW] [auth/resolver/CurrentUserArgumentResolver.java](file:///Users/juahyun/Desktop/project/prepaid/prepaid/backend/src/main/java/com/prepaid/auth/resolver/CurrentUserArgumentResolver.java)
- `@CurrentUser` 어노테이션으로 User 주입

##### [MODIFY] Controllers
- 중복 코드 제거, `@CurrentUser` 사용

---

#### 10. 테스트 강화

##### [NEW] Unit Tests
- `PaymentValidatorTest`
- `IdempotencyServiceTest`
- `RefundServiceTest`

##### [NEW] API Tests
- `PaymentApiTest`
- `RefundApiTest`

---

## ✅ 검증 계획

### 자동 테스트
- 단위 테스트: 각 서비스 로직 검증
- 통합 테스트: API → Service → DB 전체 흐름
- 동시성 테스트: 멱등성 검증

### 수동 검증
- Swagger UI에서 API 테스트
- Kafka UI에서 이벤트 확인
- Prometheus에서 메트릭 확인

---

## 📅 작업 순서

1. **Phase 1 (P0)** - 1~2시간
   - 예외 처리 → 금액 검증 → 멱등성
   
2. **Phase 2 (P1)** - 2~3시간
   - 환불 → 감사 로그
   
3. **Phase 3 (P2)** - 30분
   - API 문서 → 모니터링
   
4. **Phase 4 (P3)** - 1시간
   - 포인트 만료 → 보안 → 테스트

---

## 🚀 다음 단계

승인되면 **Phase 1**부터 순차적으로 구현을 시작합니다.
