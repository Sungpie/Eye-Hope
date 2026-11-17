# 네트워크 요청 실패 오류 해결 가이드

## 오류 상황
```
ERROR 뉴스 데이터 가져오기 실패: [TypeError: Network request failed]
```

## 가능한 원인과 해결 방법

### 1. **포트 번호 불일치**
백엔드 서버가 다른 포트에서 실행 중일 수 있습니다.

**확인 방법:**
- 백엔드 팀원에게 서버가 실행 중인 포트 번호 확인
- 일반적으로 사용되는 포트: `3000`, `8080`, `8000`, `5000`

**해결 방법:**
`newsList.tsx`에서 `possibleUrls` 배열의 포트 번호를 실제 서버 포트로 수정:

```typescript
const possibleUrls = [
  `http://localhost:실제포트번호/api/news/search?keyword=${encodeURIComponent(categoryName)}&page=0&size=20`,
  // ... 다른 URL들
];
```

### 2. **호스트 주소 불일치**
모바일 앱과 백엔드 서버가 다른 네트워크 환경에 있을 수 있습니다.

**확인 방법:**
- 백엔드 서버가 실행 중인 컴퓨터의 IP 주소 확인
- `ipconfig` (Windows) 또는 `ifconfig` (Mac/Linux) 명령어로 확인

**해결 방법:**
실제 서버 IP 주소로 URL 수정:

```typescript
const possibleUrls = [
  `http://실제서버IP:포트번호/api/news/search?keyword=${encodeURIComponent(categoryName)}&page=0&size=20`,
  // ... 다른 URL들
];
```

### 3. **CORS 설정 문제**
백엔드 서버에서 CORS 설정이 되어 있지 않을 수 있습니다.

**확인 방법:**
브라우저 개발자 도구에서 CORS 오류 메시지 확인

**해결 방법:**
백엔드 팀원에게 CORS 설정 요청:

```javascript
// Node.js + Express 예시
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
  next();
});
```

### 4. **방화벽 또는 보안 소프트웨어**
방화벽이 특정 포트를 차단하고 있을 수 있습니다.

**확인 방법:**
- Windows Defender 방화벽 설정 확인
- 백엔드 서버 포트가 방화벽에서 허용되어 있는지 확인

**해결 방법:**
방화벽에서 백엔드 서버 포트 허용 설정

### 5. **백엔드 서버 상태 확인**
서버가 실제로 실행 중인지 확인이 필요합니다.

**확인 방법:**
브라우저에서 직접 API 엔드포인트 접근:
```
http://localhost:포트번호/api/news/search?keyword=경제&page=0&size=20
```

**예상 응답:**
```json
{
  "success": true,
  "message": "뉴스 검색이 완료되었습니다.",
  "data": [...]
}
```

## 디버깅 단계

### 1단계: 백엔드 서버 상태 확인
```bash
# 백엔드 팀원에게 확인 요청
1. 서버가 실행 중인지 확인
2. 실행 중인 포트 번호 확인
3. 서버 IP 주소 확인
```

### 2단계: 네트워크 연결 테스트
```bash
# 터미널에서 ping 테스트
ping localhost
ping 127.0.0.1
ping 실제서버IP
```

### 3단계: 포트 연결 테스트
```bash
# telnet으로 포트 연결 테스트 (Windows)
telnet localhost 8080

# nc로 포트 연결 테스트 (Mac/Linux)
nc -zv localhost 8080
```

### 4단계: 브라우저에서 API 테스트
브라우저에서 직접 API 엔드포인트에 접근하여 응답 확인

## 임시 해결 방법

백엔드 연결이 실패할 경우, 앱에서는 자동으로 샘플 데이터를 표시합니다. 이는 개발 중에 백엔드 서버가 아직 준비되지 않았을 때 유용합니다.

## 백엔드 팀원과 공유할 정보

```
프론트엔드에서 다음 URL들로 API 호출을 시도하고 있습니다:

1. http://localhost:8080/api/news/search?keyword={카테고리}&page=0&size=20
2. http://127.0.0.1:8080/api/news/search?keyword={카테고리}&page=0&size=20
3. http://10.0.2.2:8080/api/news/search?keyword={카테고리}&page=0&size=20
4. http://192.168.1.100:8080/api/news/search?keyword={카테고리}&page=0&size=20

현재 "Network request failed" 오류가 발생하고 있습니다.
다음 사항들을 확인해주세요:

1. 서버가 실행 중인지
2. 실행 중인 포트 번호
3. 서버 IP 주소
4. CORS 설정 여부
5. 방화벽 설정
```

## 추가 디버깅 정보

앱을 실행할 때 콘솔에 다음과 같은 로그가 출력됩니다:

```
API 호출 시도: http://localhost:8080/api/news/search?keyword=경제&page=0&size=20
API 호출 실패 (http://localhost:8080/api/news/search?keyword=경제&page=0&size=20): [TypeError: Network request failed]
API 호출 시도: http://127.0.0.1:8080/api/news/search?keyword=경제&page=0&size=20
...
```

이 로그를 통해 어떤 URL에서 오류가 발생하는지 확인할 수 있습니다.
