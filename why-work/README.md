# 💼 Why-Work?: 개인 포트폴리오 관리 웹앱

<br>

## **[👉 데모 페이지 바로가기](http://16.176.18.127:8000/)**

<br>

## 🧾 프로젝트 소개
**Why-Work**는 다양한 자산(주식, 암호화폐, 현금 등)을 한 곳에서 관리하고 실시간으로 포트폴리오 가치를 계산할 수 있는 웹 애플리케이션입니다. 사용자는 간편하게 회원가입/로그인 후, 자신만의 포트폴리오를 구성하고, 자산의 현재 가치를 달러와 원화로 확인할 수 있습니다.

<br>

## 📌 주요 기능 및 강점
- **회원가입/로그인**: JWT 기반 인증으로 계정 관리
- **다양한 자산 지원**: 미국/한국 주식, 암호화폐, 현금(KRW, USD, USDT) 등 다양한 자산 입력 가능
- **실시간 시세 반영**: TradingView, Yahoo Finance 등 외부 API를 활용한 실시간 가격 반영
- **포트폴리오 프리셋**: 3개의 프리셋을 지원하여 다양한 투자 전략을 저장/비교 가능
- **클라우드 호스팅**: AWS 클라우드 환경에 배포하여 웹페이지 호스팅

<br>

## 📚 API 문서 (Swagger/OpenAPI)
- **Swagger UI**: `http://localhost:8000/swagger-ui`
- **ReDoc**: `http://localhost:8000/redoc`
- **OpenAPI JSON**: `http://localhost:8000/api/openapi.json`

<br>

## 🖥️ 데모 화면

### 자산 입력
<img width="600" alt="Image" src="https://github.com/user-attachments/assets/e6ba83fc-4374-43c5-aed5-3f156a874139" />

### 평가 결과
<img width="600" alt="Image" src="https://github.com/user-attachments/assets/05b943fa-b9fe-48c9-9058-d31e29ab9ec3" />

<br>

## 🔧 기술 스택
- **백엔드**: FastAPI, SQLAlchemy, MariaDB, aiomysql
- **프론트엔드**: HTML5, CSS3, JavaScript (Vanilla JS)
- **인증/보안**: JWT, passlib, python-jose
- **데이터 수집**: tradingview-ta, yfinance, requests, aiohttp
- **배포/운영**: Docker, docker-compose, AWS(EC2)

<br>

## 📁 폴더 구조
```
why-work/
  ├── main.py           # FastAPI 메인 서버
  ├── models.py         # DB 모델 정의
  ├── schemas.py        # Pydantic 스키마
  ├── auth.py           # 인증/보안 로직
  ├── database.py       # DB 연결 및 세션
  ├── static/           # JS, CSS 등 정적 파일
  ├── templates/        # HTML 템플릿
  ├── requirements.txt  # 파이썬 의존성
  ├── Dockerfile        # 도커 이미지 빌드
  ├── docker-compose.yml# 서비스 오케스트레이션
```

<br>

## ❌ 보완점 및 한계
- **tradingview-ta의 트롤링 기반 API 한계**: tradingview-ta는 공식 API가 아닌 웹 트롤링 기반으로 동작하기 때문에, 짧은 시간에 여러 번 요청을 보내면 TradingView 측에서 요청을 차단합니다. 이를 완화하기 위해 가격 정보를 일정 시간(10분) 캐싱하는 방식을 사용하지만, 그럼에도 요청 거부가 쉽게 발생하여 크롤링 기반 API의 구조적인 한계를 가집니다.
