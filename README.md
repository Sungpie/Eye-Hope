#  Eye-Hope

### 꿈꾸는 청년들의 창업 스토리 

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![React Native](https://img.shields.io/badge/React_Native-20232A?style=flat-square&logo=react&logoColor=61DAFB)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat-square&logo=postgresql&logoColor=white)



## 📝 프로젝트 소개

**Eye-Hope**는 다양한 언론사의 뉴스를 수집하고, AI를 활용하여 핵심만 요약해 사용자에게 제공하는 뉴스 애플리케이션입니다.
사용자는 카테고리별로 뉴스를 조회하거나 관심 키워드로 검색할 수 있습니다.


## ✨ 주요 기능

* **뉴스 수집**: Rome 라이브러리를 통해 RSS 피드에서 다양한 언론사의 뉴스를 자동으로 수집합니다.
* **AI 요약**: Google Gemini AI를 활용하여 방대한 뉴스 내용을 빠르고 간결하게 요약합니다.
* **카테고리별 뉴스**: 경제, 정치, 사회, IT 등 섹션별로 분류된 뉴스를 제공합니다.
* **키워드 검색**: 사용자가 관심 있는 특정 키워드로 관련 뉴스를 즉시 검색할 수 있습니다.
* **모바일 앱**: React Native 기반의 Android/IOS 앱으로 언제 어디서나 편리하게 뉴스를 확인할 수 있습니다.




## 🛠 기술 스택 (Tech Stack)

### 백엔드 (Backend)
| 구분                   | 기술                                                                                                                                                                          |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Framework**        | ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)                                                     |
| **Language**         | ![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)                                                                         |
| **Database**         | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat-square&logo=postgresql&logoColor=white)                                                             |
| **ORM**              | ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square&logoColor=white)                                                                   |
| **AI / API**         | ![Google Gemini](https://img.shields.io/badge/Google_Gemini-8E75B2?style=flat-square&logo=googlegemini&logoColor=white) ![Rome](https://img.shields.io/badge/Rome_API-orange) |
| ~~**Notification**~~ | ![Firebase](https://img.shields.io/badge/Firebase_FCM-FFCA28?style=flat-square&logo=firebase&logoColor=black)                                                               |
| **Docs**             | ![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=flat-square&logo=swagger&logoColor=black)                                                                      |

### 프론트엔드 (Frontend)
| 구분                   | 기술                                                                                                                   |
|----------------------|----------------------------------------------------------------------------------------------------------------------|
| **Framework**        | ![React Native](https://img.shields.io/badge/React_Native-20232A?style=flat-square&logo=react&logoColor=61DAFB)      |
| ~~**Notification**~~ | ![Firebase](https://img.shields.io/badge/Firebase_FCM_Client-FFCA28?style=flat-square&logo=firebase&logoColor=black) |

### 🏗 인프라 (Infrastructure)
![AWS EC2](https://img.shields.io/badge/AWS-EC2-232F3E?style=flat-square&logo=amazon-aws&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=github-actions&logoColor=white)

<br>

## ⚙️ 시스템 아키텍처 (System Architecture)

1.  **뉴스 수집 서비스**: 스케줄러가 정기적으로 RSS 피드를 크롤링합니다.
2.  **AI 요약 서비스**: 수집된 뉴스 원문을 **Gemini API**로 전송하여 요약문을 생성합니다.
3.  **REST API**: 프론트엔드 요청에 따라 정제된 뉴스 데이터를 JSON 형태로 반환합니다.
4.  ~~**📲 알림 서비스**: 이슈 발생 시 FCM을 통해 사용자 디바이스로 푸시 알림을 전송합니다.~~
5.  **📱 모바일 앱**: React Native 앱에서 사용자와 상호작용합니다.

<br>

## 💻 설치 및 실행 방법 (Installation and Setup)

### Backend

1. **저장소 클론**
    ```bash
    git clone https://github.com/Sungpie/Eye-Hope.git
    cd Eye-Hope/backend
    ```

2. **환경 변수 설정 (`application.yml` or `.env`)**
    ```properties
    DB_URL=your_database_url
    DB_USERNAME=your_database_username
    DB_PASSWORD=your_database_password
    GEMINI_API_KEY=your_gemini_api_key
    ```

3. **빌드 및 실행**
    ```bash
    ./gradlew clean build
    java -jar build/libs/eyehope-0.0.1-SNAPSHOT.jar
    ```

### Frontend

1. **디렉토리 이동**
    ```bash
    cd frontend
    ```

2. **의존성 설치**
    ```bash
    npm install
    # 또는
    yarn install
    ```

3. **앱 실행**
    ```bash
    # iOS
    npx react-native run-ios
    # Android
    npx react-native run-android
    ```



## 📄 API 문서 (API Documentation)

서버 실행 후 아래 주소에서 Swagger UI를 통해 API 명세를 확인할 수 있습니다.

> 🔗 **Swagger UI**: `http://localhost:8080/swagger-ui.html`



## 📞 연락처 (Contact)

**Eye-Hope Team**
* 프로젝트 관리자: 정종현 - jhyeon1027@naver.com

---
© 2025 Eye-Hope Team. All Rights Reserved.
