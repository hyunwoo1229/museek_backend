# Museek

## 소개
OpenAI API와 음악을 생성하는 Suno API를 사용하여 사용자가 구체적인 아이디어가 없거나, 구체적인 틀에 일일이 정보를 입력하지 않더라도 AI가 직접 사용자와 대화하며 사용자가 원하는 장르, 가사의 노래를 만들 수 있습니다.

본인 게시판의 노래는 유튜브에 업로드할 수 있습니다.

---

## 화면 설계서

<img width="1436" height="942" alt="image" src="https://github.com/user-attachments/assets/56a8d3ce-52ab-4297-9a6f-081ed0c09546" />

<img width="1376" height="634" alt="image" src="https://github.com/user-attachments/assets/61132fd4-8cae-4106-b426-1493a4c5c7ef" />

<img width="1512" height="942" alt="image" src="https://github.com/user-attachments/assets/0585b594-ce70-40dc-8b72-9ff30a4d6aa4" />

<img width="1532" height="706" alt="image" src="https://github.com/user-attachments/assets/42feb4d9-a47b-4488-ba7f-56bab72a2001" />

<img width="1552" height="718" alt="image" src="https://github.com/user-attachments/assets/c2fd2f28-fcc2-41c6-a2fd-a1eec42f401e" />


---

## DB 설계 (ERD)

* **상세 보기:** [ERD 링크 이동](https://www.erdcloud.com/d/y9YajmnGiK3iLNgsb)
<img width="1796" height="1042" alt="image" src="https://github.com/user-attachments/assets/d6241fbd-a6d5-43e0-b817-4d95cf3eec97" />


---

## 개발 내용

### AI 파이프라인 및 프롬프트 엔지니어링을 통한 작곡 프로세스 자동화
* **GPT-4o 기반 작곡가 에이전트 설계 및 프롬프트 엔지니어링 적용**
    * 사용자 대화 문맥을 유지하며 전문 작곡가 페르소나를 구현하는 시스템 메시지 설계
    * 대화 내용 중 비정형 텍스트에서 정규표현식을 활용해 Suno API 규격에 맞는 JSON 데이터를 정밀하게 추출하는 파이프라인 구축
* **멀티 모델 워크플로우 구성을 통한 태스크 자동화**
    * GPT-4o가 구체화한 프롬프트를 Suno V4_5 모델로 전달하여 제목, 가사, 장르, 스타일이 포함된 음원 생성 전 과정을 자동화

### GCS 기반 미디어 데이터 내재화 및 로딩 성능 최적화
* **리소스 접근 레이턴시 약 97% 개선 (30s → 1s) 및 데이터 영속성 확보**
    * 외부 API(Suno)의 휘발성 URL 의존성을 제거하기 위해 데이터를 GCS 버킷으로 직접 업로드
    * InputStream 기반의 스트림 업로드 방식을 도입하여 메모리 효율성을 고려한 데이터 내재화 수행
* **기존 데이터 무결성 보장을 위한 자동화된 마이그레이션 수행**
    * DB 내 외부 URL 데이터를 GCS 경로로 일괄 전환하는 배치 로직 구현
    * 중복 처리 방지 로직 및 예외 처리를 통해 유실 없는 데이터 이전 수행
* **오디오 플레이어 UI**
    * GCS에 저장된 음악을 HTML5 Audio API를 활용해 제어하고, 재생/일시정지/진행률 확인이 가능한 사용자 친화적 플레이어 구현

### 멀티미디어 합성 엔진 구축 및 배포 채널 확장
* **FFmpeg 기반 미디어 합성 및 YouTube 자동 배포 파이프라인 구축**
    * Docker 컨테이너 내 FFmpeg 환경을 구축하고, 이미지와 음원 데이터를 MP4 영상으로 합성하는 미디어 엔진 구현
    * OAuth2 기반 사용자 권한 획득 및 YouTube Data API 연동을 통해 개인 채널 자동 업로드 기능을 구현
* **WebClient를 활용한 비동기 외부 API 통신 구조 설계**
    * AI 모델의 긴 응답 대기 시간 동안 서버 스레드 점유를 최소화하는 논블로킹(Non-blocking) 방식 비동기 호출 구조 도입

### 보안 체계 고도화
* **JWT 및 OAuth2 기반의 통합 인증/인가 체계 구축**
    * Access Token(1시간)과 Refresh Token(2주) 기반의 이중 토큰 체계를 구축하여 보안성과 사용자 편의성을 동시 확보
    * Google, Naver, Kakao 소셜 로그인을 통합 관리하는 보안 레이어 구현

### Docker 기반의 CI/CD 파이프라인 구축을 통한 운영 효율화
* **컨테이너 기반 배포**
    * Docker를 활용하여 애플리케이션을 컨테이너화하고, 빌드부터 배포까지의 전 과정을 자동화하여 운영 효율성 개선
  


---

## 시스템 구성

* **프론트엔드:** 프론트엔드는 Vercel에서 배포되며, React와 Vite로 구성되어 있습니다.
  
  
* **백엔드:** 백엔드는 Cloud run에서 배포되며, 빌드된 Docker 이미지는 GCR에 저장되어 관리됩니다. MySQL(Cloud SQL) 와 연동하여 데이터를 관리합니다.
  

* **CI/CD 파이프라인:** Cloud Build를 통해 자동화된 CI/CD 파이프라인을 구성하였으며, 코드 업데이트가 발생할 때마다 Vercel과 Cloud Run에서 자동으로 빌드 및 배포가 이루어집니다.
  

---
