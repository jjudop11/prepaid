# 📚 Prepaid Platform 문서

프로젝트의 기술 문서, 아키텍처 설명, 구현 가이드를 관리하는 디렉토리입니다.

## 📁 디렉토리 구조

```
docs/
├── README.md                    # 이 파일
├── architecture/                # 아키텍처 및 시스템 설계 문서
│   ├── kafka-architecture.md   # Kafka 이벤트 시스템 아키텍처
│   └── ...
├── backend/                     # 백엔드 구현 문서
│   ├── kafka-retry-dlq.md      # Kafka 재시도 + DLQ 구현
│   └── ...
├── frontend/                    # 프론트엔드 구현 문서
│   └── ...
└── guides/                      # 개발 가이드 및 튜토리얼
    └── ...
```

## 📝 문서 작성 가이드

### 문서 분류 기준

#### `architecture/` - 시스템 아키텍처
- 전체 시스템 구조 및 설계
- 기술 스택 선택 이유
- 데이터 흐름 및 통신 패턴
- 컴포넌트 간 관계도

**예시**: Kafka 아키텍처, DB 스키마 설계, 인증/인가 시스템

#### `backend/` - 백엔드 구현
- 백엔드 기능 구현 상세 내역
- API 설계 및 명세
- 비즈니스 로직 설명
- 성능 최적화 내역

**예시**: Kafka 재시도 로직 구현, 결제 연동, 지갑 시스템

#### `frontend/` - 프론트엔드 구현
- 프론트엔드 기능 구현 상세 내역
- UI/UX 설계 결정사항
- 컴포넌트 구조
- 상태 관리 패턴

**예시**: SSE 연동, 실시간 알림 UI, 충전/사용 화면

#### `guides/` - 개발 가이드
- 개발 환경 설정
- 코딩 컨벤션
- 배포 가이드
- 트러블슈팅

**예시**: 로컬 개발 환경 설정, Docker 사용법, CI/CD

---

## 📄 문서 템플릿

### 구현 문서 (Implementation)
```markdown
# [기능명] 구현

## 개요
- 간단한 설명

## 구현 내역
- 주요 변경사항

## 기술적 의사결정
- 왜 이렇게 구현했는지

## 코드 예시
- 핵심 코드 스니펫

## 테스트
- 테스트 방법

## 참고 자료
- 관련 링크
```

### 아키텍처 문서 (Architecture)
```markdown
# [시스템명] 아키텍처

## 개요
- 시스템 목적

## 전체 구조
- 다이어그램

## 주요 컴포넌트
- 각 컴포넌트 설명

## 데이터 흐름
- 요청/응답 흐름

## 기술 스택
- 사용 기술 및 선택 이유
```

---

## 🔄 문서 관리 규칙

1. **버전 관리**: 모든 문서는 Git으로 버전 관리
2. **날짜 기록**: 문서 작성/수정 시 날짜 명시
3. **링크 활용**: 관련 코드 파일 링크 포함
4. **다이어그램**: Mermaid 문법 사용 권장
5. **코드 예시**: 실제 동작하는 코드 스니펫 포함

---

## 📖 문서 히스토리

| 날짜 | 문서 | 설명 |
|------|------|------|
| 2026-01-20 | **complete-implementation.md** | ⭐ Phase 1-4 전체 완료 문서 |
| 2026-01-20 | improvement-plan.md | Phase 1-4 구현 계획서 |
| 2026-01-20 | phase1-3-complete.md | Phase 1-3 중간 문서 |
| 2026-01-20 | phase1-2-improvements.md | Phase 1-2 초기 문서 |
| 2026-01-20 | kafka-retry-dlq.md | Kafka 재시도/DLQ 구현 |
| 2026-01-13 | kafka-architecture.md | Kafka + SSE 아키텍처 |

---

## 🔍 빠른 찾기

### 세션이 날아갔을 때
1. **프로젝트 루트의 `STATUS.md`** 먼저 확인
2. `backend/complete-implementation.md` 전체 구현 내역 확인
3. 이 README.md로 문서 구조 파악

### 특정 기능 찾기
- **예외 처리**: complete-implementation.md > Phase 1
- **금액 검증**: complete-implementation.md > Phase 1
- **환불**: complete-implementation.md > Phase 2
- **스케줄러**: complete-implementation.md > Phase 4
- **Kafka**: architecture/kafka-architecture.md

---

## 📝 최근 업데이트

**2026-01-20**: Phase 1-4 전체 완료
- 환불 기능 추가
- 포인트 만료 스케줄러
- @CurrentUser ArgumentResolver
- 단위 테스트 추가
- 전체 구현 문서 작성

---

## 💡 기여 방법

새로운 기능 구현이나 아키텍처 변경 시:
1. 적절한 디렉토리에 문서 작성
2. 이 README의 문서 이력 테이블 업데이트
3. 관련 코드와 함께 커밋

---

**Last Updated**: 2026-01-20
