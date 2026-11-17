# 뉴스 API 명세서

## 기본 정보
- **Base URL**: `http://localhost:8080` (개발 환경)
- **Content-Type**: `application/json`

## 엔드포인트

### 1. 키워드로 뉴스 검색

#### GET `/api/news/search`
제목이나 내용에 특정 키워드가 포함된 뉴스를 검색합니다.

**Query Parameters:**
- `keyword` (string, required): 검색 키워드
  - 예시: "경제", "스포츠", "IT" 등
- `page` (integer, optional): 페이지 번호 (0부터 시작)
  - 기본값: `0`
- `size` (integer, optional): 페이지 크기
  - 기본값: `10`

**Request Example:**
```
GET /api/news/search?keyword=경제&page=0&size=20
```

**Response Example:**
```json
{
  "success": true,
  "message": "뉴스 검색이 완료되었습니다.",
  "data": [
    {
      "id": 1,
      "source": "경제일보",
      "title": "한국은행, 기준금리 동결 결정...인플레이션 우려 지속",
      "content": "한국은행이 오늘 기준금리를 현재 수준으로 동결하기로 결정했습니다. 글로벌 인플레이션 우려와 경제 불확실성이 지속되면서 신중한 정책 기조를 유지한다는 방침입니다.",
      "createdAt": "2024-01-15T10:00:00.000Z",
      "url": "https://example.com/news/1",
      "category": "경제",
      "collectedAt": "2024-01-15T10:00:00.000Z"
    }
  ]
}
```

**Response Fields:**
- `success`: 요청 성공 여부 (boolean)
- `message`: 응답 메시지 (string)
- `data`: 뉴스 배열
  - `id`: 뉴스 고유 ID (number)
  - `source`: 언론사/출처 (string)
  - `title`: 뉴스 제목 (string)
  - `content`: 뉴스 내용 (string)
  - `createdAt`: 생성일시 (ISO 8601 형식)
  - `url`: 뉴스 원문 URL (string)
  - `category`: 뉴스 카테고리 (string)
  - `collectedAt`: 수집일시 (ISO 8601 형식)

**Error Response:**
```json
{
  "success": false,
  "message": "검색 키워드를 입력해주세요.",
  "data": null
}
```

## 백엔드 구현 예시 (Node.js + Express)

```javascript
const express = require('express');
const app = express();

app.use(express.json());

// 뉴스 데이터 (실제로는 데이터베이스에서 가져옴)
const newsData = [
  {
    id: 1,
    source: "경제일보",
    title: "한국은행, 기준금리 동결 결정...인플레이션 우려 지속",
    content: "한국은행이 오늘 기준금리를 현재 수준으로 동결하기로 결정했습니다...",
    createdAt: "2024-01-15T10:00:00.000Z",
    url: "https://example.com/news/1",
    category: "경제",
    collectedAt: "2024-01-15T10:00:00.000Z"
  },
  {
    id: 2,
    source: "스포츠신문",
    title: "손흥민, 프리미어리그 득점왕 경쟁 선두...토트넘 승리",
    content: "손흥민이 프리미어리그 득점왕 경쟁에서 선두를 달리고 있습니다...",
    createdAt: "2024-01-15T06:00:00.000Z",
    url: "https://example.com/news/2",
    category: "스포츠",
    collectedAt: "2024-01-15T06:00:00.000Z"
  }
  // ... 더 많은 뉴스 데이터
];

// CORS 설정 (프론트엔드와 통신을 위해)
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
  next();
});

// 키워드로 뉴스 검색 API
app.get('/api/news/search', (req, res) => {
  const { keyword, page = 0, size = 10 } = req.query;
  
  if (!keyword) {
    return res.status(400).json({
      success: false,
      message: "검색 키워드를 입력해주세요.",
      data: null
    });
  }
  
  // 키워드가 제목이나 내용에 포함된 뉴스 검색
  const filteredNews = newsData.filter(news => 
    news.title.includes(keyword) || 
    news.content.includes(keyword) ||
    news.category.includes(keyword)
  );
  
  // 페이지네이션 적용
  const startIndex = page * size;
  const endIndex = startIndex + parseInt(size);
  const paginatedNews = filteredNews.slice(startIndex, endIndex);
  
  res.json({
    success: true,
    message: "뉴스 검색이 완료되었습니다.",
    data: paginatedNews
  });
});

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  console.log(`서버가 포트 ${PORT}에서 실행 중입니다.`);
});
```

## 프론트엔드 연동

프론트엔드에서는 `fetchNewsByCategory` 함수의 `apiUrl`을 실제 백엔드 서버 URL로 변경하면 됩니다:

```typescript
// 개발 환경
const apiUrl = `http://localhost:8080/api/news/search?keyword=${encodeURIComponent(categoryName)}&page=0&size=20`;

// 프로덕션 환경
const apiUrl = `https://your-backend-domain.com/api/news/search?keyword=${encodeURIComponent(categoryName)}&page=0&size=20`;
```

## 사용 방법

### 카테고리별 뉴스 검색
각 카테고리를 키워드로 사용하여 해당 분야의 뉴스를 검색할 수 있습니다:

- **경제**: `GET /api/news/search?keyword=경제&page=0&size=20`
- **스포츠**: `GET /api/news/search?keyword=스포츠&page=0&size=20`
- **IT**: `GET /api/news/search?keyword=IT&page=0&size=20`
- **정치**: `GET /api/news/search?keyword=정치&page=0&size=20`

### 페이지네이션
- `page`: 0부터 시작하는 페이지 번호
- `size`: 한 페이지당 뉴스 개수 (기본값: 10)

## 주의사항

1. **CORS 설정**: 프론트엔드와 백엔드가 다른 도메인에서 실행될 경우 CORS 설정이 필요합니다.
2. **에러 처리**: 네트워크 오류, 서버 오류 등 다양한 상황에 대한 에러 처리가 필요합니다.
3. **보안**: 실제 프로덕션 환경에서는 인증, 권한 확인 등의 보안 조치가 필요합니다.
4. **성능**: 대량의 뉴스 데이터를 다룰 경우 인덱싱, 캐싱 등의 최적화가 필요합니다.
5. **검색 정확도**: 키워드 검색의 정확도를 높이기 위해 Elasticsearch 등의 전문 검색 엔진 사용을 고려할 수 있습니다.
