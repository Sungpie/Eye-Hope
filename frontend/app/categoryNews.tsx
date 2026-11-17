import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  Pressable,
  ActivityIndicator,
  RefreshControl,
  Alert,
  Linking, // Linking import 추가
  TouchableOpacity,
  Platform,
} from "react-native";
import { useRouter, useLocalSearchParams } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { categoryNewsStyles } from "../styles/categoryNewsStyles";

interface NewsItem {
  id: string;
  title: string;
  content: string;
  category: string;
  source: string;
  publishedAt: string;
  url?: string;
  collectedAt?: string;
  uniqueKey: string; // 고유 키 추가
}

// 카테고리별 색상 매핑 함수 추가
const getCategoryColor = (category: string): string => {
  const colorMap: { [key: string]: string } = {
    경제: "#FF6B6B",
    증권: "#4ECDC4",
    스포츠: "#45B7D1",
    연예: "#96CEB4",
    정치: "#FECA57",
    IT: "#48CAE4",
    사회: "#FF9FF3",
    오피니언: "#54A0FF",
  };

  return colorMap[category] || "#007AFF"; // 기본색상
};

export default function CategoryNewsScreen() {
  const router = useRouter();
  const { category } = useLocalSearchParams<{ category: string }>();
  const [newsData, setNewsData] = useState<NewsItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const insets = useSafeAreaInsets();

  useEffect(() => {
    if (category) {
      fetchNews(0, true);
    }
  }, [category]);

  const fetchNews = async (pageNum: number = 0, isRefresh: boolean = false) => {
    if (!category) return;

    setError(null); // 오류 상태 초기화
    try {
      if (isRefresh) {
        setLoading(true);
      }

      // API 명세서에 따른 올바른 엔드포인트 사용 (HTTPS로 변경)
      const url = `https://eyehope.site/api/news/category/${encodeURIComponent(
        category
      )}?page=${pageNum}&size=10`;

      console.log(`${category} 카테고리 뉴스 API 호출:`, url);

      const response = await fetch(url);
      console.log(`${category} 응답 상태:`, response.status);

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log(`${category} 응답 데이터:`, data);

      // API 명세서에 따른 응답 구조 처리 (success, message, data)
      let newsArray = [];

      if (data.success && Array.isArray(data.data)) {
        newsArray = data.data;
      } else if (Array.isArray(data)) {
        // 직접 배열이 반환되는 경우를 위한 fallback
        newsArray = data;
      } else {
        console.log(`${category} 카테고리: 예상치 못한 데이터 구조:`, data);
        throw new Error(data.message || "데이터를 불러올 수 없습니다");
      }

      // 고유한 키 생성을 위해 현재 시간과 페이지, 인덱스 조합
      const timestamp = Date.now();

      const processedNews = newsArray.map((news: any, index: number) => ({
        id: news.id || news.articleId || `news-${pageNum}-${index}`,
        title: news.title || news.headline || "제목 없음",
        content: news.content || news.summary || "내용 없음",
        category: news.category || category || "기타",
        source: news.source || news.publisher || "출처 없음",
        publishedAt:
          news.publishedAt ||
          news.createdAt ||
          news.publishDate ||
          new Date().toISOString(),
        url: news.url || "",
        collectedAt: news.collectedAt || "",
        // 완전히 고유한 키 생성: 카테고리 + 페이지 + 인덱스 + 타임스탬프
        uniqueKey: `${category}-${pageNum}-${index}-${timestamp}`,
      }));

      if (isRefresh || pageNum === 0) {
        setNewsData(processedNews);
        setPage(0);
      } else {
        setNewsData((prevNews) => [...prevNews, ...processedNews]);
      }

      // 더 불러올 데이터가 있는지 확인
      setHasMore(processedNews.length === 10);
      setPage(pageNum);
    } catch (error) {
      console.error(`${category} 카테고리 뉴스 가져오기 오류:`, error);
      setError("뉴스를 불러오는 중 문제가 발생했습니다.");

      // 개발 중에는 샘플 데이터 표시
      if (pageNum === 0) {
        const fallbackNews = getFallbackNews(category);
        setNewsData(fallbackNews);
      }
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  // 개발용 샘플 데이터 (백엔드 연결 실패 시 사용)
  const getFallbackNews = (categoryName: string): NewsItem[] => {
    const timestamp = Date.now();
    const fallbackData: { [key: string]: Omit<NewsItem, "uniqueKey">[] } = {
      경제: [
        {
          id: "fallback-1",
          source: "경제일보",
          title: "한국은행, 기준금리 동결 결정...인플레이션 우려 지속",
          content:
            "한국은행이 오늘 기준금리를 현재 수준으로 동결하기로 결정했습니다. 글로벌 인플레이션 우려와 경제 불확실성이 지속되면서 신중한 정책 기조를 유지한다는 방침입니다.",
          publishedAt: "2024-01-15T10:00:00.000Z",
          url: "https://example.com/news/1",
          category: "경제",
          collectedAt: "2024-01-15T10:00:00.000Z",
        },
        {
          id: "fallback-2",
          source: "경제신문",
          title: "주요 기업들 실적 발표...반도체 업계 회복세 뚜렷",
          content:
            "삼성전자, SK하이닉스 등 주요 반도체 기업들이 4분기 실적을 발표했습니다. AI 수요 증가로 인한 반도체 업계 회복세가 뚜렷하게 나타나고 있습니다.",
          publishedAt: "2024-01-15T09:00:00.000Z",
          url: "https://example.com/news/2",
          category: "경제",
          collectedAt: "2024-01-15T09:00:00.000Z",
        },
      ],
      증권: [
        {
          id: "fallback-3",
          source: "증권일보",
          title: "코스피 지수 2,500선 회복...기관 투자자 매수세 확대",
          content:
            "코스피 지수가 2,500선을 회복했습니다. 기관 투자자들의 매수세가 확대되면서 시장 낙관론이 강화되고 있습니다.",
          publishedAt: "2024-01-15T08:00:00.000Z",
          url: "https://example.com/news/3",
          category: "증권",
          collectedAt: "2024-01-15T08:00:00.000Z",
        },
      ],
      스포츠: [
        {
          id: "fallback-4",
          source: "스포츠신문",
          title: "손흥민, 프리미어리그 득점왕 경쟁 선두...토트넘 승리",
          content:
            "손흥민이 프리미어리그 득점왕 경쟁에서 선두를 달리고 있습니다. 오늘 경기에서도 득점을 기록하며 토트넘의 승리에 기여했습니다.",
          publishedAt: "2024-01-15T06:00:00.000Z",
          url: "https://example.com/news/4",
          category: "스포츠",
          collectedAt: "2024-01-15T06:00:00.000Z",
        },
      ],
      연예: [
        {
          id: "fallback-5",
          source: "연예신문",
          title: "BTS 지민, 솔로 앨범 발매...글로벌 차트 1위",
          content:
            "BTS 지민의 솔로 앨범이 발매되어 글로벌 차트에서 1위를 기록했습니다. 전 세계 팬들의 뜨거운 반응을 받고 있습니다.",
          publishedAt: "2024-01-15T04:00:00.000Z",
          url: "https://example.com/news/5",
          category: "연예",
          collectedAt: "2024-01-15T04:00:00.000Z",
        },
      ],
      정치: [
        {
          id: "fallback-6",
          source: "정치일보",
          title: "국회, 예산안 처리 완료...내년도 정책 방향 확정",
          content:
            "국회에서 내년도 예산안 처리를 완료했습니다. 주요 정책 방향과 재정 운용 계획이 확정되었습니다.",
          publishedAt: "2024-01-15T02:00:00.000Z",
          url: "https://example.com/news/6",
          category: "정치",
          collectedAt: "2024-01-15T02:00:00.000Z",
        },
      ],
      IT: [
        {
          id: "fallback-7",
          source: "IT뉴스",
          title: "AI 기술 발전 가속화...한국 기업들 혁신 주도",
          content:
            "AI 기술 발전이 가속화되고 있습니다. 한국 기업들이 AI 분야에서 혁신을 주도하며 글로벌 경쟁력을 강화하고 있습니다.",
          publishedAt: "2024-01-15T00:00:00.000Z",
          url: "https://example.com/news/7",
          category: "IT",
          collectedAt: "2024-01-15T00:00:00.000Z",
        },
      ],
      사회: [
        {
          id: "fallback-8",
          source: "사회신문",
          title: "기후변화 대응 정책 강화...탄소중립 목표 달성 노력",
          content:
            "정부가 기후변화 대응 정책을 강화하고 있습니다. 2050년 탄소중립 목표 달성을 위한 다양한 정책을 추진하고 있습니다.",
          publishedAt: "2024-01-14T22:00:00.000Z",
          url: "https://example.com/news/8",
          category: "사회",
          collectedAt: "2024-01-14T22:00:00.000Z",
        },
      ],
      오피니언: [
        {
          id: "fallback-9",
          source: "오피니언지",
          title: "[사설] 디지털 전환 시대, 교육의 방향성",
          content:
            "디지털 전환 시대를 맞아 교육의 방향성에 대한 논의가 활발합니다. 미래 인재 양성을 위한 교육 시스템의 혁신이 필요합니다.",
          publishedAt: "2024-01-14T20:00:00.000Z",
          url: "https://example.com/news/9",
          category: "오피니언",
          collectedAt: "2024-01-14T20:00:00.000Z",
        },
      ],
    };

    const baseNews = fallbackData[categoryName] || [];
    return baseNews.map((news, index) => ({
      ...news,
      uniqueKey: `fallback-${categoryName}-${index}-${timestamp}`,
    }));
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    setError(null); // 새로고침 시 오류 상태 초기화
    await fetchNews(0, true);
  };

  const handleRetry = async () => {
    setError(null);
    await fetchNews(0, true);
  };

  const handleLoadMore = () => {
    if (!loading && hasMore) {
      fetchNews(page + 1, false);
    }
  };

  const handleGoBack = () => {
    router.back();
  };

  const formatTimeAgo = (publishedAt: string) => {
    const now = new Date();

    // UTC 시간을 한국 시간(KST)으로 변환 (+9시간)
    const publishedUTC = new Date(publishedAt);
    const publishedKST = new Date(publishedUTC.getTime() + 9 * 60 * 60 * 1000);

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

  // [수정된 부분] index.tsx와 동일하게 뉴스 클릭 시 외부 브라우저에서 원문 열기
  const handleNewsPress = async (url: string) => {
    if (!url) {
      Alert.alert("알림", "기사 원문 주소가 없습니다.");
      return;
    }
    // 해당 URL을 열 수 있는지 확인합니다.
    const supported = await Linking.canOpenURL(url);

    if (supported) {
      // URL을 엽니다 (기본 웹 브라우저 실행).
      await Linking.openURL(url);
    } else {
      Alert.alert("오류", `다음 주소를 열 수 없습니다: ${url}`);
    }
  };

  // 접근성을 위한 뉴스 카드 텍스트 생성 함수 추가
  const getNewsAccessibilityLabel = (news: NewsItem) => {
    let label = `${news.category} 카테고리 뉴스. `;
    label += `제목: ${news.title}. `;
    label += `내용: ${news.content}. `;
    label += `출처: ${news.source}. `;
    label += `${formatTimeAgo(news.publishedAt)}. `;
    if (news.url) {
      label += `원문 링크 있음.`;
    }

    return label;
  };

  if (loading && newsData.length === 0) {
    return (
      <View style={categoryNewsStyles.container}>
        <View
          style={[
            categoryNewsStyles.header,
            {
              paddingTop:
                Platform.OS === "android"
                  ? Math.max(insets.top + 25, 35)
                  : Math.max(insets.top + 10, 20),
            },
          ]}
        >
          <Pressable
            style={categoryNewsStyles.backButton}
            onPress={handleGoBack}
          >
            <Ionicons name="chevron-back" size={24} color="#007AFF" />
          </Pressable>
          <Text style={categoryNewsStyles.title}>{category} 뉴스</Text>
          <View style={categoryNewsStyles.placeholder} />
        </View>
        <View style={categoryNewsStyles.loadingContainer}>
          <ActivityIndicator size="large" color="#007AFF" />
          <Text style={categoryNewsStyles.loadingText}>
            뉴스를 불러오는 중입니다
          </Text>
        </View>
      </View>
    );
  }

  if (error) {
    return (
      <View style={categoryNewsStyles.container}>
        <View
          style={[
            categoryNewsStyles.header,
            {
              paddingTop:
                Platform.OS === "android"
                  ? Math.max(insets.top + 25, 35)
                  : Math.max(insets.top + 10, 20),
            },
          ]}
        >
          <Pressable
            style={categoryNewsStyles.backButton}
            onPress={handleGoBack}
          >
            <Ionicons name="chevron-back" size={24} color="#007AFF" />
          </Pressable>
          <Text style={categoryNewsStyles.title}>{category} 뉴스</Text>
          <View style={categoryNewsStyles.placeholder} />
        </View>
        <View style={categoryNewsStyles.errorContainer}>
          <Ionicons name="alert-circle-outline" size={64} color="#FF3B30" />
          <Text style={categoryNewsStyles.errorTitle}>
            정보를 불러오지 못했어요
          </Text>
          <Text style={categoryNewsStyles.errorMessage}>
            인터넷 연결을 확인해주세요!
          </Text>
          <TouchableOpacity
            style={categoryNewsStyles.retryButton}
            onPress={handleRetry}
          >
            <Ionicons name="refresh" size={20} color="#FFFFFF" />
            <Text style={categoryNewsStyles.retryButtonText}>
              정보 불러오기
            </Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  return (
    <View style={categoryNewsStyles.container}>
      {/* 상단 헤더 */}
      <View
        style={[
          categoryNewsStyles.header,
          {
            paddingTop:
              Platform.OS === "android"
                ? Math.max(insets.top + 25, 35)
                : Math.max(insets.top + 10, 20),
          },
        ]}
      >
        <Pressable style={categoryNewsStyles.backButton} onPress={handleGoBack}>
          <Ionicons name="chevron-back" size={24} color="#007AFF" />
        </Pressable>
        <Text style={categoryNewsStyles.title}>{category} 뉴스</Text>
        <View style={categoryNewsStyles.placeholder} />
      </View>

      {/* 뉴스 목록 */}
      <ScrollView
        style={categoryNewsStyles.content}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={handleRefresh} />
        }
        onScroll={({ nativeEvent }) => {
          const { layoutMeasurement, contentOffset, contentSize } = nativeEvent;

          // 전체 스크롤 가능한 높이
          const totalScrollableHeight =
            contentSize.height - layoutMeasurement.height;

          // 70% 지점 계산
          const triggerPoint = totalScrollableHeight * 0.7;

          // 현재 스크롤 위치가 70%를 넘었는지 확인
          if (contentOffset.y >= triggerPoint && totalScrollableHeight > 0) {
            handleLoadMore();
          }
        }}
        scrollEventThrottle={400}
      >
        {newsData.length > 0 ? (
          <>
            <View style={categoryNewsStyles.newsSection}>
              <Text style={categoryNewsStyles.sectionTitle}>
                {category} 관련 뉴스 ({newsData.length}개)
              </Text>

              {newsData.map((news) => (
                <Pressable
                  key={news.uniqueKey} // uniqueKey 사용으로 중복 방지
                  style={({ pressed }) => [
                    categoryNewsStyles.newsCard,
                    pressed && categoryNewsStyles.pressedNewsCard,
                  ]}
                  onPress={() => handleNewsPress(news.url || "")}
                  accessible={true}
                  accessibilityRole="button"
                  accessibilityLabel={getNewsAccessibilityLabel(news)}
                  accessibilityHint="뉴스 원문을 보려면 두 번 탭하세요"
                >
                  <View
                    style={categoryNewsStyles.newsHeader}
                    accessible={false}
                  >
                    <Text
                      style={[
                        categoryNewsStyles.newsCategory,
                        { backgroundColor: getCategoryColor(news.category) },
                      ]}
                      accessible={false}
                    >
                      {news.category}
                    </Text>
                    <Text
                      style={categoryNewsStyles.newsTime}
                      accessible={false}
                    >
                      {formatTimeAgo(news.publishedAt)}
                    </Text>
                  </View>
                  <Text
                    style={categoryNewsStyles.newsTitle}
                    numberOfLines={3}
                    accessible={false}
                  >
                    {news.title}
                  </Text>
                  <Text
                    style={categoryNewsStyles.newsContent}
                    accessible={false}
                  >
                    {news.content}
                  </Text>
                  <View
                    style={categoryNewsStyles.newsFooter}
                    accessible={false}
                  >
                    <Text
                      style={categoryNewsStyles.newsSource}
                      accessible={false}
                    >
                      {news.source}
                    </Text>
                    {news.url && (
                      <Ionicons name="link-outline" size={14} color="#8E8E93" />
                    )}
                  </View>
                </Pressable>
              ))}
            </View>

            {/* 로딩 더 보기 */}
            {loading && (
              <View style={categoryNewsStyles.loadMoreContainer}>
                <ActivityIndicator size="small" color="#007AFF" />
                <Text style={categoryNewsStyles.loadMoreText}>
                  더 많은 뉴스를 불러오는 중...
                </Text>
              </View>
            )}

            {/* 더 이상 뉴스가 없을 때 */}
            {!hasMore && newsData.length > 0 && (
              <View style={categoryNewsStyles.noMoreContainer}>
                <Text style={categoryNewsStyles.noMoreText}>
                  모든 뉴스를 확인했습니다
                </Text>
              </View>
            )}
          </>
        ) : (
          <View style={categoryNewsStyles.emptyContainer}>
            <Ionicons name="newspaper-outline" size={64} color="#C7C7CC" />
            <Text style={categoryNewsStyles.emptyTitle}>뉴스가 없습니다</Text>
            <Text style={categoryNewsStyles.emptySubtitle}>
              {category} 관련 뉴스를 찾을 수 없습니다.{"\n"}새로고침을
              시도해보세요.
            </Text>
            <Pressable
              style={categoryNewsStyles.retryButton}
              onPress={handleRefresh}
            >
              <Ionicons name="refresh" size={20} color="#007AFF" />
              <Text style={categoryNewsStyles.retryButtonText}>다시 시도</Text>
            </Pressable>
          </View>
        )}
      </ScrollView>
    </View>
  );
}
