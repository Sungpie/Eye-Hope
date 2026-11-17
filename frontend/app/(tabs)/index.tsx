import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  SafeAreaView,
  ScrollView,
  ActivityIndicator,
  Alert,
  RefreshControl,
  TouchableOpacity,
  Linking,
  Platform,
  BackHandler,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { useFocusEffect } from "@react-navigation/native";
import { useLocalSearchParams } from "expo-router";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { indexStyles } from "../../styles/indexStyles";

interface NewsItem {
  id: string;
  title: string;
  content: string;
  category: string;
  source: string;
  publishedAt: string;
  url: string;
}

// 카테고리별 색상 매핑
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

export default function InterestNewsScreen() {
  const params = useLocalSearchParams();
  const insets = useSafeAreaInsets();
  const [newsData, setNewsData] = useState<NewsItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [categories, setCategories] = useState<string[]>([]);
  const [selectedTimes, setSelectedTimes] = useState<{
    morning: string;
    evening: string;
  } | null>(null);

  // 저장된 카테고리 불러오기
  useEffect(() => {
    loadCategories();
  }, []);

  // 파라미터에서 카테고리와 시간 정보 받아오기
  useEffect(() => {
    if (params.categories) {
      try {
        const categoriesFromParams = JSON.parse(params.categories as string);
        setCategories(categoriesFromParams);
        // AsyncStorage에 저장
        saveCategoriesToStorage(categoriesFromParams);
      } catch (error) {
        console.error("카테고리 파라미터 파싱 오류:", error);
      }
    }

    if (params.morningTime && params.eveningTime) {
      const times = {
        morning: params.morningTime as string,
        evening: params.eveningTime as string,
      };
      setSelectedTimes(times);
      // AsyncStorage에 시간 정보 저장
      saveTimesToStorage(times);
    }

    // selectedTimes 파라미터도 처리 (JSON 문자열 형태)
    if (params.selectedTimes) {
      try {
        const times = JSON.parse(params.selectedTimes as string);
        if (times.morning && times.evening) {
          setSelectedTimes(times);
          // AsyncStorage에 시간 정보 저장
          saveTimesToStorage(times);
        }
      } catch (error) {
        console.error("시간 파라미터 파싱 오류:", error);
      }
    }
  }, [
    params.categories,
    params.morningTime,
    params.eveningTime,
    params.selectedTimes,
  ]);

  // 화면이 포커스될 때마다 카테고리 새로고침 및 Android 뒤로가기 처리
  useFocusEffect(
    React.useCallback(() => {
      console.log("관심뉴스 화면 포커스됨");
      loadCategories();

      // 관심뉴스 탭 방문 기록
      AsyncStorage.setItem("lastVisitedTab", "index");
      console.log("관심뉴스 탭 방문 기록됨");

      // Android에서 뒤로가기 버튼 처리
      const backAction = () => {
        if (Platform.OS === "android") {
          Alert.alert("앱 종료", "앱을 종료하시겠습니까?", [
            {
              text: "취소",
              onPress: () => null,
              style: "cancel",
            },
            {
              text: "종료",
              onPress: () => BackHandler.exitApp(),
            },
          ]);
          return true; // 이벤트를 처리했음을 알림
        }
        return false;
      };

      const backHandler = BackHandler.addEventListener(
        "hardwareBackPress",
        backAction
      );

      return () => backHandler.remove();
    }, [])
  );

  // 카테고리가 변경될 때마다 뉴스 다시 불러오기
  useEffect(() => {
    console.log("카테고리 변경 감지:", categories);
    if (categories.length > 0) {
      fetchNews();
    } else {
      setLoading(false);
    }
  }, [categories]);

  const loadCategories = async () => {
    try {
      const savedCategories = await AsyncStorage.getItem("userCategories");
      if (savedCategories) {
        const parsedCategories = JSON.parse(savedCategories);
        setCategories(parsedCategories);
      } else {
        // 기본 카테고리 설정
        const defaultCategories = ["경제", "정치", "사회"];
        setCategories(defaultCategories);
      }
    } catch (error) {
      console.error("카테고리 로드 오류:", error);
      // 기본 카테고리로 설정
      setCategories(["경제", "정치", "사회"]);
    }
  };

  const saveCategoriesToStorage = async (categories: string[]) => {
    try {
      await AsyncStorage.setItem("userCategories", JSON.stringify(categories));
      console.log("카테고리가 저장되었습니다:", categories);
    } catch (error) {
      console.error("카테고리 저장 오류:", error);
    }
  };

  const saveTimesToStorage = async (times: {
    morning: string;
    evening: string;
  }) => {
    try {
      await AsyncStorage.setItem("userTimes", JSON.stringify(times));
      console.log("시간 정보가 저장되었습니다:", times);
    } catch (error) {
      console.error("시간 정보 저장 오류:", error);
    }
  };

  const fetchNews = async () => {
    if (categories.length === 0) return;

    setLoading(true);
    setError(null);
    console.log("뉴스 가져오기 시작, 카테고리:", categories);

    try {
      const newsPromises = categories.map(async (category) => {
        try {
          const url = `https://eyehope.site/api/news/category/${encodeURIComponent(
            category
          )}?size=3`;
          console.log(`${category} 카테고리 API 호출:`, url);

          const response = await fetch(url);
          console.log(`${category} 응답 상태:`, response.status);

          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }

          const data = await response.json();
          console.log(`${category} 응답 데이터:`, data);

          if (data.success && Array.isArray(data.data)) {
            console.log(
              `${category} 카테고리: success/data 필드에서 뉴스 찾음, 개수:`,
              data.data.length
            );
            return data.data;
          } else if (data && Array.isArray(data)) {
            return data;
          } else if (data && Array.isArray(data.content)) {
            return data.content;
          } else if (data && Array.isArray(data.articles)) {
            return data.articles;
          } else {
            console.log(`${category} 카테고리: 예상치 못한 데이터 구조:`, data);
            return [];
          }
        } catch (error) {
          console.error(`${category} 카테고리 뉴스 가져오기 실패:`, error);
          // 네트워크 오류나 기타 오류 시 에러를 다시 throw하여 상위 catch에서 처리
          throw error;
        }
      });

      const allNewsResults = await Promise.allSettled(newsPromises);
      console.log("모든 뉴스 데이터 결과:", allNewsResults);

      // 성공한 결과만 필터링
      const successfulNews = allNewsResults
        .filter(
          (result): result is PromiseFulfilledResult<any[]> =>
            result.status === "fulfilled"
        )
        .map((result) => result.value);

      // 실패한 결과 확인
      const failedResults = allNewsResults.filter(
        (result) => result.status === "rejected"
      );

      if (failedResults.length > 0) {
        console.log("실패한 카테고리들:", failedResults);
        // 모든 카테고리가 실패한 경우에만 에러 상태로 설정
        if (failedResults.length === categories.length) {
          throw new Error("모든 카테고리에서 뉴스를 가져오는데 실패했습니다.");
        }
      }

      console.log("성공한 뉴스 데이터:", successfulNews);
      console.log(
        "각 카테고리별 뉴스 개수:",
        successfulNews.map(
          (news, index) => `${categories[index]}: ${news.length}개`
        )
      );

      const flattenedNews = successfulNews.flat().map((news, index) => ({
        id: news.id || news.articleId || `news-${index}`,
        title: news.title || news.headline || "제목 없음",
        content:
          news.content || news.summary || news.description || "내용 없음",
        category: news.category || news.section || "기타",
        source: news.source || news.publisher || "출처 없음",
        publishedAt:
          news.publishedAt ||
          news.createdAt ||
          news.publishDate ||
          new Date().toISOString(),
        url: news.url || "",
      }));

      console.log("변환된 뉴스 데이터:", flattenedNews);

      // 뉴스 데이터가 없는 경우 에러 상태로 설정
      if (flattenedNews.length === 0) {
        throw new Error("뉴스 데이터를 찾을 수 없습니다.");
      }

      setNewsData(flattenedNews);
    } catch (error) {
      console.error("뉴스 가져오기 오류:", error);
      setError("뉴스를 가져오는 중 문제가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleNewsPress = async (url: string) => {
    if (!url) {
      Alert.alert("알림", "기사 원문 주소가 없습니다.");
      return;
    }
    const supported = await Linking.canOpenURL(url);

    if (supported) {
      await Linking.openURL(url);
    } else {
      Alert.alert("오류", `다음 주소를 열 수 없습니다: ${url}`);
    }
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    setError(null);
    await fetchNews();
    setRefreshing(false);
  };

  const handleRetry = async () => {
    setError(null);
    await fetchNews();
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

  if (loading) {
    return (
      <SafeAreaView style={indexStyles.container}>
        <View
          style={[
            indexStyles.header,
            {
              paddingTop:
                Platform.OS === "android" ? Math.max(insets.top + 20, 30) : 20,
            },
          ]}
        >
          <Text style={[indexStyles.title, { textAlign: "center" }]}>
            관심뉴스
          </Text>
          <Text style={[indexStyles.subtitle, { textAlign: "center" }]}>
            뉴스를 불러오는 중...
          </Text>
        </View>
        <View style={indexStyles.loadingContainer}>
          <ActivityIndicator size="large" color="#007AFF" />
          <Text style={indexStyles.loadingText}>뉴스를 불러오는 중입니다</Text>
        </View>
      </SafeAreaView>
    );
  }

  if (error) {
    return (
      <SafeAreaView style={indexStyles.container}>
        <View
          style={[
            indexStyles.header,
            {
              paddingTop:
                Platform.OS === "android" ? Math.max(insets.top + 20, 30) : 20,
            },
          ]}
        >
          <Text style={[indexStyles.title, { textAlign: "center" }]}>
            관심뉴스
          </Text>
          <Text style={[indexStyles.subtitle, { textAlign: "center" }]}>
            {categories.length > 0
              ? `${categories.join(", ")} 카테고리의 최신 뉴스입니다`
              : "설정에서 관심 카테고리를 선택해주세요"}
          </Text>
        </View>
        <View style={indexStyles.errorContainer}>
          <Ionicons name="alert-circle-outline" size={64} color="#FF3B30" />
          <Text style={indexStyles.errorTitle}>정보를 불러오지 못했어요</Text>
          <Text style={indexStyles.errorMessage}>
            인터넷 연결을 확인해주세요!
          </Text>
          <TouchableOpacity
            style={indexStyles.retryButton}
            onPress={handleRetry}
          >
            <Ionicons name="refresh" size={20} color="#FFFFFF" />
            <Text style={indexStyles.retryButtonText}>정보 불러오기</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={indexStyles.container}>
      {/* 상단 제목 */}
      <View
        style={[
          indexStyles.header,
          {
            paddingTop:
              Platform.OS === "android" ? Math.max(insets.top + 20, 30) : 20,
          },
        ]}
      >
        <Text style={[indexStyles.title, { textAlign: "center" }]}>
          관심뉴스
        </Text>
        <Text style={[indexStyles.subtitle, { textAlign: "center" }]}>
          {categories.length > 0
            ? `${categories.join(", ")} 카테고리의 최신 뉴스입니다`
            : "설정에서 관심 카테고리를 선택해주세요"}
        </Text>
      </View>

      {/* 뉴스 목록 */}
      <ScrollView
        style={indexStyles.content}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={handleRefresh} />
        }
      >
        {newsData.length > 0 ? (
          <View style={indexStyles.newsList}>
            {newsData.map((news) => (
              <TouchableOpacity
                key={news.id}
                style={indexStyles.newsItem}
                onPress={() => handleNewsPress(news.url)}
                activeOpacity={0.7}
                accessible={true}
                accessibilityRole="button"
                accessibilityLabel={getNewsAccessibilityLabel(news)}
                accessibilityHint="뉴스 원문을 보려면 두 번 탭하세요"
              >
                <View style={indexStyles.newsHeader} accessible={false}>
                  <View
                    style={[
                      indexStyles.categoryBadge,
                      { backgroundColor: getCategoryColor(news.category) },
                    ]}
                    accessible={false}
                  >
                    <Text style={indexStyles.categoryText} accessible={false}>
                      {news.category}
                    </Text>
                  </View>
                  <Text style={indexStyles.timeText} accessible={false}>
                    {formatTimeAgo(news.publishedAt)}
                  </Text>
                </View>
                <Text style={indexStyles.newsTitle} accessible={false}>
                  {news.title}
                </Text>
                <Text style={indexStyles.newsContent} accessible={false}>
                  {news.content}
                </Text>
                <View style={indexStyles.newsFooter} accessible={false}>
                  <Text style={indexStyles.newsSource} accessible={false}>
                    {news.source}
                  </Text>
                  {news.url && (
                    <Ionicons name="link-outline" size={14} color="#8E8E93" />
                  )}
                </View>
              </TouchableOpacity>
            ))}
          </View>
        ) : (
          <View style={indexStyles.emptyContainer}>
            <Ionicons name="newspaper-outline" size={64} color="#C7C7CC" />
            <Text style={indexStyles.emptyTitle}>뉴스가 없습니다</Text>
            <Text style={indexStyles.emptySubtitle}>
              설정에서 관심 카테고리를 선택하거나{"\n"}인터넷 연결을
              확인해보세요.
            </Text>
          </View>
        )}

        {/* 선택된 시간 정보 표시 */}
        {selectedTimes && (
          <View style={indexStyles.timeInfoSection}>
            <Text style={indexStyles.timeInfoTitle}>설정된 알림 시간</Text>
            <View style={indexStyles.timeInfoContainer}>
              <Text style={indexStyles.timeInfoText}>
                아침: {selectedTimes.morning} | 저녁: {selectedTimes.evening}
              </Text>
            </View>
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}
