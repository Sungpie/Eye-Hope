import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  Pressable,
  StatusBar,
  ActivityIndicator,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useRouter, useLocalSearchParams } from "expo-router";

interface NewsItem {
  id: number;
  source: string;
  title: string;
  content: string;
  createdAt: string;
  url: string;
  category: string;
  collectedAt: string;
}

export default function NewsListScreen() {
  const router = useRouter();
  const { category } = useLocalSearchParams<{ category: string }>();
  
  const [news, setNews] = useState<NewsItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 백엔드 서버에서 뉴스 데이터 가져오기
  const fetchNewsByCategory = async (categoryName: string) => {
    try {
      setLoading(true);
      setError(null);
      
      // 백엔드 서버 URL (여러 포트와 호스트를 시도)
      const possibleUrls = [
        `http://localhost:8080/api/news/search?keyword=${encodeURIComponent(categoryName)}&page=0&size=20`,
        `http://127.0.0.1:8080/api/news/search?keyword=${encodeURIComponent(categoryName)}&page=0&size=20`,
        `http://10.0.2.2:8080/api/news/search?keyword=${encodeURIComponent(categoryName)}&page=0&size=20`, // Android 에뮬레이터용
        `http://192.168.1.100:8080/api/news/search?keyword=${encodeURIComponent(categoryName)}&page=0&size=20`, // 로컬 네트워크 IP
      ];
      
      let response: Response | null = null;
      let lastError: Error | null = null;
      
      // 여러 URL을 시도
      for (const apiUrl of possibleUrls) {
        try {
          console.log(`API 호출 시도: ${apiUrl}`);
          
          // 타임아웃을 위한 Promise.race 사용 (AbortSignal.timeout 대신)
          const timeoutPromise = new Promise<never>((_, reject) => {
            setTimeout(() => reject(new Error('요청 시간 초과')), 5000);
          });
          
          const fetchPromise = fetch(apiUrl, {
            method: 'GET',
            headers: {
              'Accept': 'application/json',
              'Content-Type': 'application/json',
            },
          });
          
          // 타임아웃과 fetch를 경쟁시킴
          response = await Promise.race([fetchPromise, timeoutPromise]);
          
          if (response.ok) {
            console.log(`API 호출 성공: ${apiUrl}`);
            break;
          } else {
            console.log(`API 응답 오류: ${response.status} - ${response.statusText}`);
          }
        } catch (err) {
          console.log(`API 호출 실패 (${apiUrl}):`, err);
          lastError = err instanceof Error ? err : new Error(String(err));
          continue;
        }
      }
      
      if (!response || !response.ok) {
        const errorMsg = lastError?.message || '알 수 없는 오류';
        throw new Error(`모든 API 엔드포인트 연결 실패. 마지막 오류: ${errorMsg}`);
      }
      
      const data = await response.json();
      console.log('API 응답 데이터:', data);
      
      // API 응답 구조에 맞게 데이터 처리
      if (data.success && data.data) {
        setNews(data.data);
      } else {
        throw new Error('데이터 형식이 올바르지 않습니다.');
      }
      
    } catch (err) {
      console.error('뉴스 데이터 가져오기 실패:', err);
      
      // 더 자세한 에러 메시지
      let errorMessage = '뉴스를 불러오는데 실패했습니다.';
      if (err instanceof TypeError && err.message.includes('Network request failed')) {
        errorMessage = '백엔드 서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요.';
      } else if (err instanceof Error) {
        errorMessage = err.message;
      }
      
      setError(errorMessage);
      
      // 개발용 샘플 데이터 (백엔드 연결 실패 시)
      const fallbackNews = getFallbackNews(categoryName);
      setNews(fallbackNews);
    } finally {
      setLoading(false);
    }
  };

  // 개발용 샘플 데이터 (백엔드 연결 실패 시 사용)
  const getFallbackNews = (categoryName: string): NewsItem[] => {
    const fallbackData: { [key: string]: NewsItem[] } = {
      "경제": [
        {
          id: 1,
          source: "경제일보",
          title: "한국은행, 기준금리 동결 결정...인플레이션 우려 지속",
          content: "한국은행이 오늘 기준금리를 현재 수준으로 동결하기로 결정했습니다. 글로벌 인플레이션 우려와 경제 불확실성이 지속되면서 신중한 정책 기조를 유지한다는 방침입니다.",
          createdAt: "2024-01-15T10:00:00.000Z",
          url: "https://example.com/news/1",
          category: "경제",
          collectedAt: "2024-01-15T10:00:00.000Z",
        },
        {
          id: 2,
          source: "경제신문",
          title: "주요 기업들 실적 발표...반도체 업계 회복세 뚜렷",
          content: "삼성전자, SK하이닉스 등 주요 반도체 기업들이 4분기 실적을 발표했습니다. AI 수요 증가로 인한 반도체 업계 회복세가 뚜렷하게 나타나고 있습니다.",
          createdAt: "2024-01-15T09:00:00.000Z",
          url: "https://example.com/news/2",
          category: "경제",
          collectedAt: "2024-01-15T09:00:00.000Z",
        },
      ],
      "증권": [
        {
          id: 3,
          source: "증권일보",
          title: "코스피 지수 2,500선 회복...기관 투자자 매수세 확대",
          content: "코스피 지수가 2,500선을 회복했습니다. 기관 투자자들의 매수세가 확대되면서 시장 낙관론이 강화되고 있습니다.",
          createdAt: "2024-01-15T08:00:00.000Z",
          url: "https://example.com/news/3",
          category: "증권",
          collectedAt: "2024-01-15T08:00:00.000Z",
        },
        {
          id: 4,
          source: "투자신문",
          title: "외국인 투자자 순매수 전환...한국 시장 매력도 상승",
          content: "외국인 투자자들이 한국 시장에 대한 관심을 높이고 있습니다. 글로벌 경제 회복 기대감과 함께 한국 시장의 매력도가 상승하고 있습니다.",
          createdAt: "2024-01-15T07:00:00.000Z",
          url: "https://example.com/news/4",
          category: "증권",
          collectedAt: "2024-01-15T07:00:00.000Z",
        },
      ],
      "스포츠": [
        {
          id: 5,
          source: "스포츠신문",
          title: "손흥민, 프리미어리그 득점왕 경쟁 선두...토트넘 승리",
          content: "손흥민이 프리미어리그 득점왕 경쟁에서 선두를 달리고 있습니다. 오늘 경기에서도 득점을 기록하며 토트넘의 승리에 기여했습니다.",
          createdAt: "2024-01-15T06:00:00.000Z",
          url: "https://example.com/news/5",
          category: "스포츠",
          collectedAt: "2024-01-15T06:00:00.000Z",
        },
        {
          id: 6,
          source: "축구일보",
          title: "김민재, 바이에른 뮌헨에서 안정적인 수비...팬들 호평",
          content: "김민재가 바이에른 뮌헨에서 안정적인 수비를 보여주고 있습니다. 독일 언론과 팬들로부터 높은 평가를 받고 있습니다.",
          createdAt: "2024-01-15T05:00:00.000Z",
          url: "https://example.com/news/6",
          category: "스포츠",
          collectedAt: "2024-01-15T05:00:00.000Z",
        },
      ],
      "연예": [
        {
          id: 7,
          source: "연예신문",
          title: "BTS 지민, 솔로 앨범 발매...글로벌 차트 1위",
          content: "BTS 지민의 솔로 앨범이 발매되어 글로벌 차트에서 1위를 기록했습니다. 전 세계 팬들의 뜨거운 반응을 받고 있습니다.",
          createdAt: "2024-01-15T04:00:00.000Z",
          url: "https://example.com/news/7",
          category: "연예",
          collectedAt: "2024-01-15T04:00:00.000Z",
        },
        {
          id: 8,
          source: "K-POP뉴스",
          title: "뉴진스, 월드투어 성공적 마무리...글로벌 인기 증명",
          content: "뉴진스가 월드투어를 성공적으로 마무리했습니다. 전 세계 각지에서 열린 공연을 통해 글로벌 인기를 다시 한번 증명했습니다.",
          createdAt: "2024-01-15T03:00:00.000Z",
          url: "https://example.com/news/8",
          category: "연예",
          collectedAt: "2024-01-15T03:00:00.000Z",
        },
      ],
      "정치": [
        {
          id: 9,
          source: "정치일보",
          title: "국회, 예산안 처리 완료...내년도 정책 방향 확정",
          content: "국회에서 내년도 예산안 처리를 완료했습니다. 주요 정책 방향과 재정 운용 계획이 확정되었습니다.",
          createdAt: "2024-01-15T02:00:00.000Z",
          url: "https://example.com/news/9",
          category: "정치",
          collectedAt: "2024-01-15T02:00:00.000Z",
        },
        {
          id: 10,
          source: "정치신문",
          title: "외교부, 주요국과 협력 강화...글로벌 이슈 대응",
          content: "외교부가 주요국과의 협력을 강화하고 있습니다. 글로벌 이슈에 대한 공동 대응 방안을 모색하고 있습니다.",
          createdAt: "2024-01-15T01:00:00.000Z",
          url: "https://example.com/news/10",
          category: "정치",
          collectedAt: "2024-01-15T01:00:00.000Z",
        },
      ],
      "IT": [
        {
          id: 11,
          source: "IT뉴스",
          title: "AI 기술 발전 가속화...한국 기업들 혁신 주도",
          content: "AI 기술 발전이 가속화되고 있습니다. 한국 기업들이 AI 분야에서 혁신을 주도하며 글로벌 경쟁력을 강화하고 있습니다.",
          createdAt: "2024-01-15T00:00:00.000Z",
          url: "https://example.com/news/11",
          category: "IT",
          collectedAt: "2024-01-15T00:00:00.000Z",
        },
        {
          id: 12,
          source: "테크뉴스",
          title: "메타버스 플랫폼 경쟁 심화...기술 표준화 논의 활발",
          content: "메타버스 플랫폼 간 경쟁이 심화되고 있습니다. 기술 표준화를 위한 국제적 논의가 활발하게 진행되고 있습니다.",
          createdAt: "2024-01-14T23:00:00.000Z",
          url: "https://example.com/news/12",
          category: "IT",
          collectedAt: "2024-01-14T23:00:00.000Z",
        },
      ],
      "사회": [
        {
          id: 13,
          source: "사회신문",
          title: "기후변화 대응 정책 강화...탄소중립 목표 달성 노력",
          content: "정부가 기후변화 대응 정책을 강화하고 있습니다. 2050년 탄소중립 목표 달성을 위한 다양한 정책을 추진하고 있습니다.",
          createdAt: "2024-01-14T22:00:00.000Z",
          url: "https://example.com/news/13",
          category: "사회",
          collectedAt: "2024-01-14T22:00:00.000Z",
        },
        {
          id: 14,
          source: "사회일보",
          title: "인구 고령화 대응...노인 복지 정책 확대",
          content: "인구 고령화에 대응하기 위한 노인 복지 정책이 확대되고 있습니다. 노인들의 삶의 질 향상을 위한 다양한 지원책이 마련되고 있습니다.",
          createdAt: "2024-01-14T21:00:00.000Z",
          url: "https://example.com/news/14",
          category: "사회",
          collectedAt: "2024-01-14T21:00:00.000Z",
        },
      ],
      "복지": [
        {
          id: 15,
          source: "복지신문",
          title: "전남 순천서 60대 노동자 기계에 깔려 숨져..",
          content: "당시 A씨는 크레인을 이용해 대형 기계를 차량에 싣던 중 기계가 갑자기 기울어지며 사고를 당한 것으로 알려졌습니다. 신고를 받고 출동한 소방당국은 심정지 상태의 A씨를 인근 병원으로 옮겼지만 결국 숨졌습니다.",
          createdAt: "2024-01-14T20:00:00.000Z",
          url: "https://example.com/news/15",
          category: "복지",
          collectedAt: "2024-01-14T20:00:00.000Z",
        },
        {
          id: 16,
          source: "복지일보",
          title: "장애인 복지시설 확충...접근성 개선 정책 추진",
          content: "정부가 장애인 복지시설 확충을 위한 정책을 추진하고 있습니다. 장애인들의 사회 참여를 위한 접근성 개선 사업이 확대되고 있습니다.",
          createdAt: "2024-01-14T19:00:00.000Z",
          url: "https://example.com/news/16",
          category: "복지",
          collectedAt: "2024-01-14T19:00:00.000Z",
        },
      ],
    };
    
    return fallbackData[categoryName] || fallbackData["복지"];
  };

  // 카테고리가 변경될 때마다 뉴스 데이터 가져오기
  useEffect(() => {
    if (category) {
      fetchNewsByCategory(category);
    }
  }, [category]);

  const handleNewsPress = (newsId: number, pressCount: number) => {
    if (pressCount === 1) {
      // 한 번 터치: 다음 뉴스로 넘어가기
      console.log("다음 뉴스로 넘어가기");
    } else if (pressCount === 2) {
      // 두 번 터치: 본문 보기
      console.log("본문 보기:", newsId);
    }
  };

  const handleBackPress = () => {
    router.back();
  };

  // 시간 표시 함수
  const formatTimeAgo = (publishedAt: string) => {
    const now = new Date();
    
    // UTC 시간을 한국 시간(KST)으로 변환 (+9시간)
    const publishedUTC = new Date(publishedAt);
    const publishedKST = new Date(publishedUTC.getTime() + (9 * 60 * 60 * 1000));
    
    // 현재 시간과 한국 시간으로 변환된 발행 시간의 차이 계산
    const diffInMilliseconds = now.getTime() - publishedKST.getTime();
    const diffInMinutes = Math.floor(diffInMilliseconds / (1000 * 60));
    const diffInHours = Math.floor(diffInMilliseconds / (1000 * 60 * 60));

    // 1분 미만
    if (diffInMinutes < 1) return "방금 전";
    
    // 1시간 미만 (분으로 표기)
    if (diffInHours < 1) return `${diffInMinutes}분 전`;
    
    // 24시간 미만 (시간으로 표기)
    if (diffInHours < 24) return `${diffInHours}시간 전`;

    // 24시간 이상 (일로 표기)
    const diffInDays = Math.floor(diffInHours / 24);
    return `${diffInDays}일 전`;
  };

  // category가 없을 경우 기본값 설정
  const displayCategory = category || "카테고리";

  if (loading) {
    return (
      <View style={styles.container}>
        <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#007AFF" />
          <Text style={styles.loadingText}>뉴스를 불러오는 중...</Text>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      
      {/* 헤더 */}
      <View style={styles.header}>
        <Pressable style={styles.backButton} onPress={handleBackPress}>
          <Ionicons name="arrow-back" size={24} color="#000000" />
        </Pressable>
        <Text style={styles.headerTitle}>{displayCategory}</Text>
        <View style={styles.placeholder} />
      </View>

      {/* 사용법 안내 */}
      <View style={styles.instructions}>
        <Text style={styles.instructionText}>
          본문을 보고싶다면 두 번을 눌러주세요.
        </Text>
        <Text style={styles.instructionText}>
          다음 뉴스로 넘어가고싶다면 한 번을 눌러주세요.
        </Text>
      </View>

      {/* 에러 메시지 */}
      {error && (
        <View style={styles.errorContainer}>
          <Text style={styles.errorText}>{error}</Text>
          <Text style={styles.errorSubtext}>샘플 데이터를 표시합니다.</Text>
        </View>
      )}

      {/* 뉴스 목록 */}
      <ScrollView style={styles.newsList} showsVerticalScrollIndicator={false}>
        {news.map((newsItem) => (
          <View key={newsItem.id} style={styles.newsItem}>
            <Text style={styles.newsTitle}>{newsItem.title}</Text>
            <Text style={styles.newsSummary}>{newsItem.content}</Text>
            <View style={styles.newsMetaContainer}>
              <Text style={styles.newsSource}>{newsItem.source}</Text>
              <Text style={styles.newsMeta}>
                {formatTimeAgo(newsItem.createdAt)}
              </Text>
            </View>
          </View>
        ))}
      </ScrollView>

      {/* 하단 네비게이션 */}
      <View style={styles.bottomNavigation}>
        <Pressable style={styles.navItem}>
          <Ionicons name="grid-outline" size={24} color="#007AFF" />
          <Text style={styles.navText}>카테고리</Text>
        </Pressable>
        <Pressable style={styles.navItem}>
          <Ionicons name="home-outline" size={24} color="#8E8E93" />
          <Text style={styles.navText}>관심뉴스</Text>
        </Pressable>
        <Pressable style={styles.navItem}>
          <Ionicons name="settings-outline" size={24} color="#8E8E93" />
          <Text style={styles.navText}>설정</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F2F2F7",
  },
  loadingContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#F2F2F7",
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: "#8E8E93",
  },
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    backgroundColor: "#FFFFFF",
    paddingHorizontal: 20,
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: "#E5E5EA",
  },
  backButton: {
    padding: 8,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: "600",
    color: "#000000",
  },
  placeholder: {
    width: 40,
  },
  instructions: {
    backgroundColor: "#FFFFFF",
    paddingHorizontal: 20,
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: "#E5E5EA",
  },
  instructionText: {
    fontSize: 14,
    color: "#007AFF",
    lineHeight: 20,
    marginBottom: 4,
  },
  errorContainer: {
    backgroundColor: "#FFE5E5",
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: "#FFCCCC",
  },
  errorText: {
    fontSize: 14,
    color: "#D70015",
    fontWeight: "500",
  },
  errorSubtext: {
    fontSize: 12,
    color: "#D70015",
    marginTop: 4,
  },
  newsList: {
    flex: 1,
    paddingHorizontal: 20,
  },
  newsItem: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    padding: 16,
    marginTop: 16,
    shadowColor: "#000",
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  newsTitle: {
    fontSize: 16,
    fontWeight: "600",
    color: "#000000",
    marginBottom: 8,
    lineHeight: 22,
  },
  newsSummary: {
    fontSize: 14,
    color: "#666666",
    lineHeight: 20,
    marginBottom: 8,
  },
  newsMetaContainer: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  newsSource: {
    fontSize: 12,
    color: "#007AFF",
    fontWeight: "500",
  },
  newsMeta: {
    fontSize: 12,
    color: "#8E8E93",
    fontStyle: "italic",
  },
  bottomNavigation: {
    flexDirection: "row",
    backgroundColor: "#FFFFFF",
    borderTopWidth: 1,
    borderTopColor: "#E5E5EA",
    paddingVertical: 12,
  },
  navItem: {
    flex: 1,
    alignItems: "center",
    paddingVertical: 8,
  },
  navText: {
    fontSize: 12,
    color: "#8E8E93",
    marginTop: 4,
  },
});
