
# 구축된 FCM 시스템 사용 가이드

## 개요

Firebase Cloud Messaging(FCM)은 무료로 메시지를 안정적으로 전송할 수 있는 크로스 플랫폼 메시징 솔루션입니다. Eye-Hope 애플리케이션에서는 FCM을 통해 사용자에게 중요한 알림을 전송할 수 있습니다.

## 서버 측 설정

Eye-Hope 백엔드에는 이미 FCM이 다음과 같이 구성되어 있습니다:

1. **Firebase Admin SDK**: `firebase-admin:9.5.0` 의존성이 build.gradle에 추가되어 있습니다.
2. **서비스 계정 키**: Firebase 비공개 키가 `src/main/resources/firebase/eye-hope-firebase-adminsdk-fbsvc-ee4f3eeaf3.json`에 저장되어 있습니다.
3. **Firebase 구성**: `FirebaseConfig` 클래스가 애플리케이션 시작 시 Firebase Admin SDK를 초기화합니다.

## API 엔드포인트 사용 방법

### 1. 알림 전송하기

**엔드포인트**: `POST /api/v1/fcm/send`

이 엔드포인트를 사용하여 단일 기기, 여러 기기 또는 토픽에 알림을 보낼 수 있습니다.

**요청 본문 예시**:
```json
{
  "targetType": "token",
  "token": "device_token_here",
  "title": "알림 제목",
  "body": "알림 내용",
  "data": {
    "key1": "value1",
    "key2": "value2"
  }
}
```

**여러 기기에 전송**:
```json
{
  "targetType": "tokens",
  "tokens": ["token1", "token2", "token3"],
  "title": "여러 기기 알림",
  "body": "여러 기기에 전송되는 알림입니다",
  "data": {
    "type": "multi_device"
  }
}
```

**토픽에 전송**:
```json
{
  "targetType": "topic",
  "topic": "news_updates",
  "title": "뉴스 업데이트",
  "body": "새로운 뉴스가 도착했습니다",
  "data": {
    "type": "news"
  }
}
```

**참고사항**:
- `targetType`: "token"(단일 기기), "tokens"(여러 기기), "topic"(토픽) 중 하나를 선택
- `token`: targetType이 "token"일 때 필수
- `tokens`: targetType이 "tokens"일 때 필수
- `topic`: targetType이 "topic"일 때 필수
- `data`: 추가 데이터를 키-값 쌍으로 전송할 수 있음 (선택사항)

### 2. 토픽 구독하기

**엔드포인트**: `POST /api/v1/fcm/topic/subscribe`

이 엔드포인트를 사용하여 기기를 FCM 토픽에 구독시킬 수 있습니다. 구독 정보는 데이터베이스에도 저장됩니다.

**요청 본문 예시**:
```json
{
  "deviceId": "사용자_기기_ID",
  "topic": "news_updates"
}
```

**참고사항**:
- `deviceId`: 사용자의 기기 ID (UUID 형식)
- `topic`: 구독할 토픽 이름

### 3. 토픽 구독 취소하기

**엔드포인트**: `POST /api/v1/fcm/topic/unsubscribe`

이 엔드포인트를 사용하여 기기의 FCM 토픽 구독을 취소할 수 있습니다. 구독 정보는 데이터베이스에서도 삭제됩니다.

**요청 본문 예시**:
```json
{
  "deviceId": "사용자_기기_ID",
  "topic": "news_updates"
}
```

**참고사항**:
- `deviceId`: 사용자의 기기 ID (UUID 형식)
- `topic`: 구독 취소할 토픽 이름

## 테스트 엔드포인트

### 1. 테스트 알림 전송하기

**엔드포인트**: `GET /api/v1/fcm-test/send-test-notification`

이 엔드포인트를 사용하여 특정 기기 토큰에 테스트 알림을 보낼 수 있습니다.

**쿼리 파라미터**:
- `token` (필수): 알림을 보낼 기기 토큰
- `title` (선택, 기본값: "Test Notification"): 알림 제목
- `body` (선택, 기본값: "This is a test notification from Eye-Hope"): 알림 내용

**예시 요청**:
```
GET /api/v1/fcm-test/send-test-notification?token=your_device_token&title=테스트&body=테스트 메시지입니다
```

### 2. 테스트 토픽 알림 전송하기

**엔드포인트**: `GET /api/v1/fcm-test/send-test-topic`

이 엔드포인트를 사용하여 특정 토픽에 테스트 알림을 보낼 수 있습니다.

**쿼리 파라미터**:
- `topic` (필수): 알림을 보낼 토픽
- `title` (선택, 기본값: "Test Topic Notification"): 알림 제목
- `body` (선택, 기본값: "This is a test topic notification from Eye-Hope"): 알림 내용

**예시 요청**:
```
GET /api/v1/fcm-test/send-test-topic?topic=news_updates&title=토픽 테스트&body=토픽 테스트 메시지입니다
```

## 클라이언트 통합

클라이언트 애플리케이션에서 FCM 메시지를 수신하려면 다음 단계를 따라야 합니다:

1. 클라이언트 애플리케이션(Android, iOS 또는 Web)에서 Firebase 설정
2. Firebase에서 기기 토큰 얻기
3. 얻은 기기 토큰을 Eye-Hope 백엔드에 전송
4. 클라이언트 애플리케이션에서 수신 메시지 처리

### Android 통합

Android의 경우 앱에 Firebase SDK를 추가하고 `FirebaseMessagingService`를 확장하는 서비스를 구현해야 합니다. 이 서비스는 수신 메시지와 토큰 새로고침을 처리합니다.

### iOS 통합

iOS의 경우 앱에 Firebase SDK를 추가하고 수신 메시지와 토큰 새로고침을 처리하는 데 필요한 메서드를 구현해야 합니다.

### 웹 통합

웹 애플리케이션의 경우 앱에 Firebase SDK를 추가하고 수신 메시지와 토큰 새로고침을 처리하는 데 필요한 코드를 구현해야 합니다.

## 데이터베이스 통합

FCM 시스템은 다음과 같은 데이터베이스 엔티티와 통합되어 있습니다:

1. **User**: 사용자 정보와 FCM 토큰을 저장합니다.
2. **Topic**: 구독 가능한 토픽 정보를 저장합니다.
3. **UserTopic**: 사용자와 토픽 간의 구독 관계를 저장합니다.

토픽 구독/구독 취소 API를 호출하면 이 정보가 자동으로 데이터베이스에 반영됩니다.

## 문제 해결

FCM 통합에 문제가 발생하면 다음을 확인하세요:

1. Firebase 서비스 계정 키가 올바르게 구성되어 있는지 확인
2. 기기 토큰이 유효하고 Firebase 프로젝트에 속하는지 확인
3. 사용자의 deviceId가 데이터베이스에 존재하는지 확인
4. FCM 관련 오류 메시지가 있는지 애플리케이션 로그 확인
5. 클라이언트 애플리케이션이 FCM 메시지를 수신하도록 올바르게 설정되어 있는지 확인

## 보안 고려사항

- Firebase 서비스 계정 키는 민감한 정보이므로 안전하게 보관해야 합니다.
- FCM API 엔드포인트에 대한 액세스는 인증된 사용자로 제한해야 합니다.
- 스팸을 방지하기 위해 알림을 보내기 전에 기기 토큰을 검증해야 합니다.
- 사용자의 deviceId는 UUID 형식으로 안전하게 관리해야 합니다.
