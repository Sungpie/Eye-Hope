import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  StyleSheet,
  SafeAreaView,
  TouchableOpacity,
  ScrollView,
  Alert,
  Platform,
  BackHandler,
} from "react-native";
import { useRouter, useLocalSearchParams } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { useFocusEffect } from "@react-navigation/native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { settingsStyles } from "../../styles/settingsStyles";

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

// 카테고리 매핑 함수들 (안전하게 수정)
const categoryToId = (category: string): string => {
  const mapping: { [key: string]: number } = {
    경제: 1,
    증권: 2,
    스포츠: 3,
    연예: 4,
    정치: 5,
    IT: 6,
    사회: 7,
    오피니언: 8,
  };
  const id = mapping[category];
  return id ? id.toString() : "0"; // 숫자를 문자열로 변환
};

const idToCategory = (id: number): string => {
  const mapping: { [key: number]: string } = {
    1: "경제",
    2: "증권",
    3: "스포츠",
    4: "연예",
    5: "정치",
    6: "IT",
    7: "사회",
    8: "오피니언",
  };
  return mapping[id] || "";
};

interface NewsItem {
  id: number;
  category: string;
  pressName: string;
}

interface UserNewsResponse {
  success: boolean;
  message: string;
  data: {
    deviceId: string;
    news: NewsItem[];
  };
}

interface UserScheduleResponse {
  success: boolean;
  message: string;
  data: {
    deviceId: string;
    notificationTime: string[];
  };
}

export default function SettingsScreen() {
  const router = useRouter();
  const params = useLocalSearchParams<{
    fromCategory?: string;
    selectedCategories?: string;
    selectedTimes?: string;
    fromNewsUpdate?: string;
  }>();
  const insets = useSafeAreaInsets();

  // 상태 관리
  const [currentCategories, setCurrentCategories] = useState<string[]>([
    "경제",
    "정치",
    "사회",
    "IT",
    "스포츠",
  ]);
  const [currentTimes, setCurrentTimes] = useState<{
    morning: string;
    evening: string;
  }>({
    morning: "오전 9시",
    evening: "오후 8시",
  });
  const [loading, setLoading] = useState(false);

  // 앱 시작 시 저장된 데이터 로드
  useEffect(() => {
    loadSavedData();
  }, []);

  // 화면이 포커스될 때마다 저장된 데이터 새로고침 및 Android 뒤로가기 처리
  useFocusEffect(
    React.useCallback(() => {
      console.log("설정 화면 포커스됨 - 데이터 새로고침");
      console.log("현재 params:", params);

      // 파라미터가 있으면 우선 처리
      if (params.selectedCategories || params.selectedTimes) {
        console.log("파라미터가 있어서 파라미터 우선 처리");
        handleParamsUpdate();
      } else {
        // 파라미터가 없을 때만 저장된 데이터 로드
        console.log("파라미터가 없어서 저장된 데이터 로드");
        loadSavedData();
      }

      // Android에서 뒤로가기 버튼 처리 - 항상 관심뉴스 페이지로 이동
      const backAction = () => {
        if (Platform.OS === "android") {
          router.push("/(tabs)"); // 관심뉴스 페이지로 이동
          return true; // 이벤트를 처리했음을 알림
        }
        return false;
      };

      const backHandler = BackHandler.addEventListener(
        "hardwareBackPress",
        backAction
      );

      return () => backHandler.remove();
    }, [params.selectedCategories, params.selectedTimes])
  );

  // 백엔드에서 사용자 관심 뉴스 가져오기 - 비활성화 (로컬 데이터만 사용)
  const fetchUserNews = async (): Promise<string[] | null> => {
    console.log("📰 백엔드 API 호출 비활성화 - 로컬 데이터만 사용");
    return null;
  };

  // 백엔드에서 사용자 알림 시간 가져오기 - 비활성화
  // const fetchUserSchedule = async (): Promise<{ morning: string; evening: string } | null> => {
  //   try {
  //     const deviceId = await AsyncStorage.getItem("deviceId");
  //     if (!deviceId) {
  //       console.log("DeviceId가 없습니다");
  //       return null;
  //     }

  //     console.log("⏰ === 백엔드에서 사용자 알림 시간 가져오기 시작 ===");
  //     console.log("📤 DeviceId:", deviceId);
  //
  //     const response = await fetch(`http://13.124.111.205:8080/api/users/schedules/${encodeURIComponent(deviceId)}`, {
  //       method: "GET",
  //       headers: {
  //         "Content-Type": "application/json",
  //       },
  //     });

  //     console.log("📥 알림 시간 응답 상태:", response.status);

  //     if (response.ok) {
  //       const result: UserScheduleResponse = await response.json();
  //       console.log("📥 알림 시간 응답 데이터:", JSON.stringify(result, null, 2));
  //
  //       if (result.success && result.data && Array.isArray(result.data.notificationTime)) {
  //         const times = result.data.notificationTime;
  //         if (times.length >= 2) {
  //           return {
  //             morning: times[0],
  //             evening: times[1],
  //           };
  //         }
  //       }
  //
  //       console.log("📥 알림 시간 응답 데이터 형식이 올바르지 않음:", result);
  //       return null;
  //     } else {
  //       const errorText = await response.text();
  //       console.log("📥 알림 시간 HTTP 오류 응답:", response.status, errorText);
  //       return null;
  //     }
  //
  //   } catch (error) {
  //     console.error("🚨 사용자 알림 시간 가져오기 오류:", error);
  //     return null;
  //   }
  // };

  // 저장된 데이터 불러오기
  const loadSavedData = async () => {
    setLoading(true);

    try {
      // 로컬 데이터만 사용 (백엔드 API 호출 비활성화)
      console.log("📱 로컬 데이터만 사용하여 설정 로드");

      const savedCategories = await AsyncStorage.getItem("userCategories");
      if (savedCategories) {
        const parsedCategories = JSON.parse(savedCategories);
        setCurrentCategories(parsedCategories);
        console.log("📱 로컬에서 관심 뉴스 로드됨:", parsedCategories);
      }

      // 3. 알림 시간 가져오기 - 비활성화
      // const backendSchedule = await fetchUserSchedule();
      //
      // if (backendSchedule) {
      //   console.log("✅ 백엔드에서 알림 시간 로드됨:", backendSchedule);
      //   setCurrentTimes(backendSchedule);
      //   // 백엔드 데이터를 로컬에도 동기화
      //   await AsyncStorage.setItem("userTimes", JSON.stringify(backendSchedule));
      // } else {
      //   // 백엔드에서 가져오기 실패 시 로컬 데이터 사용
      //   console.log("⚠️ 백엔드에서 알림 시간 가져오기 실패 - 로컬 데이터 사용");
      //   const savedTimes = await AsyncStorage.getItem("userTimes");
      //   if (savedTimes) {
      //     const parsedTimes = JSON.parse(savedTimes);
      //     setCurrentTimes(parsedTimes);
      //     console.log("📱 로컬에서 알림 시간 로드됨:", parsedTimes);
      //   }
      // }
    } catch (error) {
      console.error("❌ 저장된 데이터 로드 오류:", error);

      // 에러 발생 시 사용자에게 알림 (선택사항)
      Alert.alert(
        "데이터 로드 오류",
        "일부 데이터를 불러오는 중 문제가 발생했습니다. 로컬 데이터로 표시됩니다.",
        [{ text: "확인" }]
      );

      // 에러 발생 시에도 로컬 데이터는 로드
      try {
        const savedCategories = await AsyncStorage.getItem("userCategories");
        const savedTimes = await AsyncStorage.getItem("userTimes");

        if (savedCategories) {
          setCurrentCategories(JSON.parse(savedCategories));
        }
        // if (savedTimes) {
        //   setCurrentTimes(JSON.parse(savedTimes));
        // }
      } catch (localError) {
        console.error("❌ 로컬 데이터 로드도 실패:", localError);
      }
    } finally {
      setLoading(false);
    }
  };

  // AsyncStorage에 카테고리 저장
  const saveCategoriesToStorage = async (categories: string[]) => {
    try {
      await AsyncStorage.setItem("userCategories", JSON.stringify(categories));
      console.log("카테고리가 저장되었습니다:", categories);
    } catch (error) {
      console.error("카테고리 저장 오류:", error);
    }
  };

  // AsyncStorage에 시간 정보 저장 - 비활성화
  // const saveTimesToStorage = async (times: {
  //   morning: string;
  //   evening: string;
  // }) => {
  //   try {
  //     await AsyncStorage.setItem("userTimes", JSON.stringify(times));
  //     console.log("시간 정보가 저장되었습니다:", times);
  //   } catch (error) {
  //     console.error("시간 정보 저장 오류:", error);
  //   }
  // };

  // 파라미터 업데이트 처리 함수
  const handleParamsUpdate = () => {
    console.log("파라미터 업데이트 처리 시작");

    // selectedCategories 파라미터가 있으면 업데이트
    if (params.selectedCategories) {
      try {
        const categories = JSON.parse(params.selectedCategories as string);
        console.log("파라미터에서 카테고리 파싱:", categories);
        if (Array.isArray(categories)) {
          console.log("카테고리 상태 업데이트:", categories);
          setCurrentCategories(categories);
          // AsyncStorage에 카테고리 저장
          saveCategoriesToStorage(categories);
        }
      } catch (error) {
        console.error("카테고리 파싱 오류:", error);
      }
    }

    // selectedTimes 파라미터 처리 - 비활성화
    // if (params.selectedTimes) {
    //   try {
    //     const times = JSON.parse(params.selectedTimes as string);
    //     if (times.morning && times.evening) {
    //       const newTimes = {
    //         morning: times.morning,
    //         evening: times.evening,
    //       };
    //       console.log("시간 상태 업데이트:", newTimes);
    //       setCurrentTimes(newTimes);
    //       // AsyncStorage에 시간 정보 저장
    //       saveTimesToStorage(newTimes);
    //     }
    //   } catch (error) {
    //     console.error("시간 파라미터 파싱 오류:", error);
    //   }
    // }

    // fromNewsUpdate 파라미터가 있으면 백엔드에서 최신 뉴스 정보 다시 로드
    if (params.fromNewsUpdate === "true") {
      console.log("뉴스 업데이트 완료 - 백엔드에서 최신 정보 로드");
      loadSavedData();
    }

    console.log("파라미터 업데이트 처리 완료");
  };

  const handleCategoryChange = () => {
    router.push({
      pathname: "/selectCategory",
      params: { fromSettings: "true" },
    });
  };

  // 알림 시간 변경 기능 비활성화
  // const handleTimeChange = () => {
  //   router.push({
  //     pathname: "/timeSelect" as any,
  //     params: { fromSettings: "true" },
  //   });
  // };

  // 접근성을 위한 관심뉴스 텍스트 생성
  const getCategoriesAccessibilityLabel = () => {
    let label = "현재 관심뉴스. ";
    if (currentCategories.length > 0) {
      label += `선택된 카테고리: ${currentCategories.join(", ")}. `;
    } else {
      label += "선택된 카테고리가 없습니다. ";
    }
    label +=
      "관심뉴스를 수정 변경하시겠어요? 변경을 원하신다면 두 번 눌러주세요.";

    return label;
  };

  // 알림 시간 관련 접근성 함수 비활성화
  // const getTimeAccessibilityLabel = () => {
  //   let label = "알림 시간대 변경. ";
  //   label += `현재 알림 시간대는 ${currentTimes.morning || "미설정"}와 ${currentTimes.evening || "미설정"}에요. `;
  //   label += "시간대 변경을 원하신다면 두 번 눌러주세요.";
  //
  //   return label;
  // };

  return (
    <SafeAreaView style={settingsStyles.container}>
      <ScrollView showsVerticalScrollIndicator={false}>
        {/* 상단 제목 */}
        <View
          style={[
            settingsStyles.header,
            {
              paddingTop:
                Platform.OS === "android" ? Math.max(insets.top + 20, 30) : 20,
            },
          ]}
        >
          <Text style={[settingsStyles.title, { textAlign: "center" }]}>
            설정
          </Text>
          <Text style={[settingsStyles.subtitle, { textAlign: "center" }]}>
            사용자 정보와 현재 관심뉴스를 수정할 수 있습니다.
          </Text>
        </View>

        {/* 로딩 표시 */}
        {loading && (
          <View style={settingsStyles.loadingContainer}>
            <Text style={settingsStyles.loadingText}>
              데이터를 불러오는 중...
            </Text>
          </View>
        )}

        {/* 현재 관심뉴스 섹션 - 접근성 개선 */}
        <TouchableOpacity
          style={[settingsStyles.interestNewsSection, { alignItems: "center" }]}
          onPress={handleCategoryChange}
          activeOpacity={0.7}
          accessible={true}
          accessibilityRole="button"
          accessibilityLabel={getCategoriesAccessibilityLabel()}
          accessibilityHint="관심 뉴스 카테고리를 수정할 수 있는 페이지로 이동합니다"
        >
          <View style={settingsStyles.sectionHeaderSimple} accessible={false}>
            <Text
              style={[settingsStyles.sectionTitle, { textAlign: "center" }]}
              accessible={false}
            >
              현재 관심뉴스
            </Text>
          </View>

          <View
            style={[
              settingsStyles.categoriesContainer,
              { justifyContent: "center" },
            ]}
            accessible={false}
          >
            {currentCategories.map((category, index) => (
              <View
                key={index}
                style={settingsStyles.categoryItemContainer}
                accessible={false}
              >
                <View
                  style={[
                    settingsStyles.categoryTag,
                    { backgroundColor: getCategoryColor(category) },
                  ]}
                  accessible={false}
                >
                  <Text
                    style={[
                      settingsStyles.categoryText,
                      { textAlign: "center" },
                    ]}
                    accessible={false}
                  >
                    {category || "카테고리"}
                  </Text>
                </View>
              </View>
            ))}
          </View>
          <Text
            style={[settingsStyles.questionText, { textAlign: "center" }]}
            accessible={false}
          >
            관심뉴스를 수정 / 변경하시겠어요?
          </Text>
          <Text
            style={[settingsStyles.instructionText, { textAlign: "center" }]}
            accessible={false}
          >
            변경을 원하신다면 두 번 눌러주세요.
          </Text>
        </TouchableOpacity>

        {/* 시간대 변경 섹션 - 숨김 처리 */}
        {/* 
        <TouchableOpacity
          style={[settingsStyles.timeChangeSection, { alignItems: "center" }]}
          onPress={handleTimeChange}
          activeOpacity={0.7}
          accessible={true}
          accessibilityRole="button"
          accessibilityLabel={getTimeAccessibilityLabel()}
          accessibilityHint="뉴스 알림을 받을 시간대를 수정할 수 있는 페이지로 이동합니다"
        >
          <Text style={[settingsStyles.sectionTitle, { textAlign: "center" }]} accessible={false}>
            알림 시간대 변경
          </Text>
          <View style={[settingsStyles.timeInfoContainer, { alignItems: "center" }]} accessible={false}>
            <Text style={[settingsStyles.timeInfoText, { textAlign: "center" }]} accessible={false}>
              현재 알림 시간대는
            </Text>
            <View
              style={[
                settingsStyles.timeButtonsContainer,
                {
                  alignItems: "center",
                  flexDirection: "row",
                  justifyContent: "center",
                },
              ]}
              accessible={false}
            >
              <View style={settingsStyles.timeButton} accessible={false}>
                <Text style={[settingsStyles.timeButtonText, { textAlign: "center" }]} accessible={false}>
                  {currentTimes.morning || "미설정"}
                </Text>
              </View>
              <Text style={[settingsStyles.timeInfoText, { textAlign: "center" }]} accessible={false}>
                와
              </Text>
              <View style={settingsStyles.timeButton} accessible={false}>
                <Text style={[settingsStyles.timeButtonText, { textAlign: "center" }]} accessible={false}>
                  {currentTimes.evening || "미설정"}
                </Text>
              </View>
              <Text style={[settingsStyles.timeInfoText, { textAlign: "center" }]} accessible={false}>
                에요.
              </Text>
            </View>
          </View>
          <Text style={[settingsStyles.instructionText, { textAlign: "center" }]} accessible={false}>
            시간대 변경을 원하신다면 두 번 눌러주세요.
          </Text>
        </TouchableOpacity>
        */}

        {/* 앱 정보 섹션 */}
        <View style={settingsStyles.appInfoSection}>
          <Text style={settingsStyles.sectionTitle}>앱 정보</Text>
          <Text style={settingsStyles.appInfoText}>
            Eye-Hope v1.0.0{"\n"}
            개인 맞춤형 뉴스 알림 서비스
          </Text>
          <Text style={settingsStyles.supportText}>
            경기청년 갭이어 프로그램의 지원을 받아 제작되었습니다.
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
