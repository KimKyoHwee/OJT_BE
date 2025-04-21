# 🛠 OJT - 사용자 정의 배치 스케줄러 시스템

> 사용자가 직접 등록한 배치 작업을 지정된 시간 혹은 반복 간격에 맞춰 자동 실행해주는  
> **Spring Batch 5 + Scheduler 기반 배치 관리 시스템**

---

## 📌 프로젝트 소개

OJT는 다양한 서버에 흩어진 배치 API들을 한 곳에서 등록하고,  
사용자가 정의한 시간에 맞춰 자동 호출해주는 **배치 작업 오케스트레이터**입니다.  
각 작업의 실행 결과는 로그로 저장되며, UI를 통해 관리할 수 있습니다.

---

## 🧩 핵심 기술 스택

- **Spring Boot 3**
- **Spring Batch 5**
- **Spring Scheduler (@Scheduled)**
- **Spring Data JPA + MySQL**
- **React (Next.js) + TailwindCSS**
- **React Query**: 클라이언트 캐시/실시간 반영
- **Docker** (선택)

---

## 🧠 아키텍처 요약

```
[사용자]
  └─ 작업 등록 (endpoint, 시간, 반복여부 등)
      └─ [서버 DB: BatchJob + BatchSchedule 저장]
          └─ Spring Scheduler (매 1분 polling)
              └─ 실행 조건 도달 시 endpoint 호출
                  └─ 결과를 BatchLog로 저장
```

---

## ⏰ 스케줄링 전략: 왜 1분 polling?

### 📍 선택 이유

- 사용자 정의 시간에 유연하게 대응 (`2025-04-19 03:12`도 가능)
- Spring Scheduler 기반의 단순하고 강력한 구조
- DB에 등록된 스케줄 정보를 기반으로, **1분마다 polling**하여 실행 조건을 판단

### 📐 비교

| 방식 | 설명 | 장점 | 단점 |
|------|------|------|------|
| ⏱️ 1분 polling (현재 방식) | DB에 저장된 `nextExecutionTime`을 1분마다 확인 | 유연함, 동적 등록 | 초 단위는 어려움 |
| 🕰️ Cron 기반 `@Scheduled` | 코드 상에 cron 직접 설정 | 정밀, 가볍다 | 동적 등록 불가 |
| 🧱 Quartz Scheduler | 자체 DB 스케줄러 엔진 | 고급 제어 가능 | 무거움, 설정 복잡 |
| ☁️ Airflow / Luigi | DAG 기반 파이프라인 관리 | 워크플로우 최적화 | 배포/러닝 커브 높음 |

---

## 📁 주요 도메인 모델

### ✅ BatchJob
- 배치 작업 정의 (이름, endpoint URL, 설명 등)

### ✅ BatchSchedule
- 실행 시간 및 반복 주기 설정
```java
LocalDateTime scheduleTime;
Integer repeatIntervalHour; // 24면 매일
LocalDateTime nextExecutionTime;
Boolean isActive;
```

### ✅ BatchLog
- 실행 결과 저장 (성공/실패, 응답 or 에러 메시지, 실행 시간 등)

---

## 🔁 배치 실행 흐름

1. 사용자가 작업을 등록하면 `BatchJob`, `BatchSchedule` 생성
2. Spring Scheduler가 매 1분마다 DB를 조회해 실행 대상 판단
3. 해당 작업의 endpoint로 POST 요청 전송
4. 결과는 `BatchLog`로 저장
5. 반복 설정이 있다면 다음 실행 시간으로 `nextExecutionTime` 갱신

---

## 🔍 Spring Batch 구조와 차이점

### 우리는 **Chunk 기반이 아닌 Polling 기반 스케줄러**를 사용합니다.

| 비교 항목 | Chunk-Oriented (Spring Batch) | OJT 방식 (Polling) |
|-----------|-------------------------------|--------------------|
| 핵심 방식 | Reader → Processor → Writer   | Scheduler → 조건 DB 조회 → HTTP 호출 |
| 용도 | 대용량 데이터 처리 | HTTP API 호출 기반 스케줄링 |
| 유연성 | 낮음 (정적 구성) | 높음 (동적 스케줄 등록 가능) |
| 적합도 | DB 작업 중심 | 외부 서비스 호출 중심 |

> 💡 우리는 **Spring Batch의 스케줄링 전략은 차용하되**,  
> 대규모 데이터 처리를 위한 Chunk 패턴 대신,  
> **간단하고 동적인 polling 방식으로 API를 트리거**하는 구조를 채택했습니다.

---

## 🖥️ 프론트엔드 기능

- 작업 등록 및 수정 (모달 기반 UI)
- 실행 시간/반복 간격 입력
- 실행 로그 확인 (성공/실패/응답)
- 작업 즉시 실행 버튼
- 로그인/로그아웃 상태 관리

---

## 🔜 향후 개선 방향

- [ ] 작업 실패 시 재시도 로직
- [ ] cron-like 스케줄 지원
- [ ] 동시성 처리 개선 (분산 락 등)
- [ ] Slack / Email 알림 연동
- [ ] 관리자 통계 화면

---

## 🧑‍💻 개발자 가이드

### 배치 등록 API

```json
POST /api/v1/batch-job
{
  "name": "S3 백업",
  "description": "Daily backup",
  "endpointUrl": "https://myapi.com/backup",
  "scheduleTime": "2025-04-19T02:00:00",
  "repeatIntervalHour": 24
}
```

### 실행 로그 조회

```
GET /api/v1/logs/job/{jobId}
```

---

## 🧠 진행 상황 요약

| 파트 | 상태 |
|------|------|
| 도메인 모델 설계 | ✅ 완료 |
| 스케줄러 polling 로직 | ✅ 완료 |
| Job 등록 → Schedule 자동 생성 | ✅ 완료 |
| 반복 실행/단발 실행 구분 | ✅ 완료 |
| 로그 저장 기능 | ✅ 완료 |
| 프론트 기능 (등록, 수정, 실행, 로그) | ✅ 완료 |
| Spring Batch Chunk 방식 도입 여부 검토 | ❌ 비적합으로 미도입 결정 |

---

## 📎 참고

- Spring Batch 공식 문서: https://docs.spring.io/spring-batch
- Toss 디자인 시스템 참고: https://toss.im

---

## 👨‍🔧 개발자

- Kyohwee – Backend / Spring Batch Architect
- [GitHub 프로필 링크]