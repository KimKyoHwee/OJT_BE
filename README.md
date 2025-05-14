# 🛠 고급 배치 처리 시스템

> **Quartz 기반 REST API 스케줄러**와 **Spring Batch 5 기반 배치 처리**를 결합한
> OCR 사업자등록증 검증 기능을 포함한 종합 배치 관리 시스템
>

## 📌 프로젝트 개요
본 프로젝트는 두 가지 방식의 배치 처리 기능을 제공합니다:
1. **REST API 스케줄러**: Quartz를 활용한 REST API 호출 스케줄링
2. **커스텀 배치 작업**: Spring Batch 5를 활용한 사용자 정의 배치 개발

주요 구현 사례로 사업자등록증 검증 배치가 포함되어 있습니다:
- CLOVA OCR을 통한 사업자등록증 이미지 처리
- 국세청 API를 통한 추출 데이터 검증
- 상세 실행 로그 관리

## 🧩 핵심 기술 스택
### 백엔드
- **Spring Boot 3**
- **Spring Batch 5**: 커스텀 배치 작업용
- **Quartz**: REST API 스케줄링
- **Spring Data JPA + MySQL**
- **CLOVA OCR 연동**: 문서 텍스트 추출
- **국세청 API 연동**: 사업자등록증 진위확인

### 프론트엔드
- **React (Next.js) + TailwindCSS**
- **React Query**: 클라이언트 캐시 관리
- **Docker** (선택사항)

## 🏗 시스템 아키텍처
### REST API 스케줄러 흐름
[사용자] └─ API 작업 등록 (엔드포인트, 스케줄) └─ [Quartz 스케줄러] └─ 스케줄 관리 └─ REST API 실행 └─ 결과 로깅
### 커스텀 배치 흐름
[사업자등록증 검증] └─ 이미지 처리 (CLOVA OCR) └─ 텍스트 추출 └─ 데이터 검증 (국세청 API) └─ 결과 처리
## 💡 주요 기능
### 1. 이원화된 배치 처리 방식
- **REST API 스케줄링**
    - 동적 엔드포인트 등록
    - 유연한 스케줄링 옵션
    - 자동 재시도 메커니즘

- **커스텀 배치 작업**
    - Spring Batch 5 통합
    - Chunk 처리 지원
    - 병렬 처리 기능

### 2. 사업자등록증 검증
- **OCR 통합**
    - CLOVA OCR 문서 처리
    - 정확한 텍스트 추출
    - 다양한 이미지 포맷 지원

- **데이터 검증**
    - 국세청 API 연동
    - 실시간 유효성 검증
    - 종합적 오류 처리

### 3. 모니터링 및 관리
- 실시간 실행 모니터링
- 상세 로깅 시스템
- 성능 지표 추적

## 🔧 기술 구현
### OCR 처리
@Service public class ClovaOcrService { // 이미지 처리 및 텍스트 추출 public ClovaOcrResponseDto extractText(String imageUrl) { // Base64 인코딩 // API 통신 // 응답 처리 } }
### 스케줄링 시스템
@Component public class QuartzRestJob extends QuartzJobBean { @Override protected void executeInternal(JobExecutionContext context) { // 스케줄 관리 // 작업 실행 // 결과 로깅 } }
## 🖥️ 프론트엔드 기능
- 작업 등록 및 수정 (모달 기반 UI)
- 실행 시간/반복 간격 입력
- 실행 로그 확인 (성공/실패/응답)
- 작업 즉시 실행 버튼
- 로그인/로그아웃 상태 관리

## ⏰ 스케줄링 전략
### 📍 선택 이유
- 사용자 정의 시간에 유연하게 대응 (2025-04-19 03:12도 가능)
- Spring Scheduler 기반의 단순하고 강력한 구조
- DB에 등록된 스케줄 정보를 기반으로 실행 조건을 판단

### 📐 비교

| 방식 | 설명 | 장점 | 단점 |
| --- | --- | --- | --- |
| ⏱️ Quartz (현재 방식) | DB 기반 스케줄러 | 유연함, 동적 등록 | 설정 복잡 |
| 🕰️ Cron 기반 @Scheduled | 코드 상에 cron 직접 설정 | 정밀, 가볍다 | 동적 등록 불가 |
| ☁️ Airflow / Luigi | DAG 기반 파이프라인 관리 | 워크플로우 최적화 | 러닝 커브 높음 |
## 📁 주요 도메인 모델
### ✅ BatchJob
- 배치 작업 정의 (이름, endpoint URL, 설명 등)

### ✅ BatchSchedule
- 실행 시간 및 반복 주기 설정 LocalDateTime scheduleTime; Integer repeatIntervalHour; // 24면 매일 LocalDateTime nextExecutionTime; Boolean isActive;

### ✅ BatchLog
- 실행 결과 저장 (성공/실패, 응답 or 에러 메시지, 실행 시간 등)

## 🔜 향후 개선 계획
- [ ] 고급 재시도 메커니즘
- [ ] 분산 실행 지원
- [ ] 향상된 모니터링 대시보드
- [ ] 이메일/슬랙 알림
- [ ] API 문서화 개선

## 🧑‍💻 개발자 가이드
### REST API 작업 등록
POST /api/v1/batch-job { "name": "사업자등록증 검증", "description": "사업자등록증 처리 및 검증", "endpointUrl": "[https://api.example.com/verify](https://api.example.com/verify)", "scheduleTime": "2024-03-19T10:00:00", "repeatInterval": 24 }
### 실행 로그 조회
GET /api/v1/logs/job/{jobId}
## 🧠 진행 상황 요약

| 파트 | 상태 |
| --- | --- |
| 도메인 모델 설계 | ✅ 완료 |
| Quartz 스케줄러 구현 | ✅ 완료 |
| OCR 연동 | ✅ 완료 |
| 국세청 API 연동 | ✅ 완료 |
| 로그 저장 기능 | ✅ 완료 |
| 프론트엔드 기능 | ✅ 완료 |
## 📚 참고 문서
- [Spring Batch 공식 문서](https://docs.spring.io/spring-batch)
- [Quartz 스케줄러 가이드](https://www.quartz-scheduler.org)
- [CLOVA OCR API 문서](https://www.ncloud.com/product/aiService/ocr)

## 👥 개발자
- Kyohwee – 백엔드 / 배치 아키텍처 담당
- [GitHub 프로필]
