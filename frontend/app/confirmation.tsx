import React from "react";
import {
  View,
  Text,
  Pressable,
  StyleSheet,
  Alert,
  ActivityIndicator,
  Platform,
} from "react-native";
import { useRouter, useLocalSearchParams } from "expo-router";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { useState } from "react";
import { useSafeAreaInsets } from "react-native-safe-area-context";

// 카테고리 매핑 함수들
const categoryToId = (category: string): number => {
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
  return mapping[category] || 0;
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

interface UserNewsData {
  newsIds: number[];
}

interface UserRegistrationData {
  name?: string;
  email?: string;
  nickname: string;
  password?: string;
}

export default function ConfirmationScreen() {
  const { categories, fromSettings } = useLocalSearchParams<{
    categories: string;
    fromSettings?: string;
  }>();
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const [loading, setLoading] = useState(false);

  // JSON 문자열을 파싱하여 카테고리 배열로 변환
  const selectedCategories = categories ? JSON.parse(categories) : [];

  // 사용자 등록 API 호출 - POST 요청 제거됨
  const registerUser = async (userData: UserRegistrationData) => {
    try {
      console.log("👤 === 사용자 등록 (로컬 저장만) ===");
      console.log("📤 저장할 데이터:", JSON.stringify(userData, null, 2));

      // POST 요청 대신 로컬 저장만 수행
      console.log("✅ 사용자 정보가 로컬에 저장되었습니다");

      return { success: true, message: "사용자 정보가 로컬에 저장되었습니다" };
    } catch (error) {
      console.error("👤 사용자 등록 오류:", error);
      throw error;
    }
  };

  // 백엔드에 사용자 관심 뉴스 저장 - POST 요청 제거됨
  const saveUserNews = async (newsData: UserNewsData) => {
    try {
      console.log("📰 === 사용자 관심 뉴스 저장 (로컬 저장만) ===");
      console.log("📤 저장할 데이터:", JSON.stringify(newsData, null, 2));

      // POST 요청 대신 로컬 저장만 수행
      console.log("✅ 관심 뉴스가 로컬에 저장되었습니다");

      return { success: true, message: "관심 뉴스가 로컬에 저장되었습니다" };
    } catch (error) {
      console.error("🚨 관심 뉴스 저장 오류:", error);
      throw error;
    }
  };

  const handleGoBack = () => {
    router.back();
  };

  const handleConfirm = async () => {
    console.log("=== 카테고리 확인 완료 ===");
    console.log("선택된 카테고리:", selectedCategories);
    console.log("fromSettings 파라미터:", fromSettings);

    setLoading(true);

    try {
      console.log("🔍 로컬 데이터만 사용하여 설정 완료");

      // 로컬스토리지에 저장
      await AsyncStorage.setItem(
        "userCategories",
        JSON.stringify(selectedCategories)
      );
      console.log("✅ 관심 뉴스가 로컬에 저장되었습니다");

      // fromSettings 파라미터 확인
      if (fromSettings === "true") {
        console.log("설정 페이지로 돌아가기");
        router.push({
          pathname: "/(tabs)/settings",
          params: {
            selectedCategories: JSON.stringify(selectedCategories),
            fromNewsUpdate: "true",
          },
        });
      } else {
        console.log("설정 완료 - 메인 탭으로 이동");
        // 일반 플로우라면 바로 메인 탭으로 이동

        // 설정 완료 플래그 저장
        await AsyncStorage.setItem("setupCompleted", "true");

        // 기본 사용자 정보를 AsyncStorage에 저장

        router.push({
          pathname: "/(tabs)",
          params: {
            categories: JSON.stringify(selectedCategories),
            selectedTimes: JSON.stringify({ morning: "", evening: "" }),
          },
        });
      }
    } catch (error) {
      console.error("❌ 관심 뉴스 저장 오류:", error);

      const errorMessage =
        error instanceof Error
          ? error.message
          : "관심 뉴스 저장 중 오류가 발생했습니다.";

      Alert.alert("오류", errorMessage, [
        {
          text: "그래도 진행",
          onPress: () => {
            // 오류가 발생해도 로컬에만 저장하고 진행
            AsyncStorage.setItem(
              "userCategories",
              JSON.stringify(selectedCategories)
            );

            if (fromSettings === "true") {
              router.push({
                pathname: "/(tabs)/settings",
                params: {
                  selectedCategories: JSON.stringify(selectedCategories),
                },
              });
            } else {
              // 설정 완료 플래그 저장
              AsyncStorage.setItem("setupCompleted", "true");

              router.push({
                pathname: "/(tabs)",
                params: {
                  categories: JSON.stringify(selectedCategories),
                  selectedTimes: JSON.stringify({ morning: "", evening: "" }),
                },
              });
            }
          },
        },
        {
          text: "처음부터 다시",
          onPress: () => {
            // 로컬 데이터 모두 삭제 후 처음부터
            AsyncStorage.multiRemove([
              "setupCompleted",
              "userCategories",
              "userTimes",
              "userInfo",
              "deviceId",
            ]).then(() => {
              router.replace("/selectCategory");
            });
          },
        },
        {
          text: "재시도",
          style: "cancel",
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      {/* 상단 안내 문구 박스 */}
      <View
        style={[
          styles.instructionContainer,
          { marginTop: Math.max(insets.top + 20, 30) },
        ]}
      >
        <Text style={styles.instructionText}>
          선택하신 뉴스 기사를 확인할게요.
        </Text>
      </View>

      {/* 중간 카테고리 목록 */}
      <View style={styles.categoriesContainer}>
        {selectedCategories.map((category: string, index: number) => (
          <View key={index} style={styles.categoryItem}>
            <View style={styles.diamondIcon}>
              <Text
                style={[
                  styles.diamondText,
                  Platform.OS === "android" && { fontSize: 20 },
                ]}
              >
                {category}
              </Text>
            </View>
          </View>
        ))}
      </View>

      {/* 하단 요약 박스 */}
      <View style={styles.summaryContainer}>
        <Text style={styles.summaryText}>
          총 {selectedCategories.length}가지 관심 뉴스를 선택하셨어요.
        </Text>
        <Text style={styles.summaryText}>
          관심 분야는 언제든지 바꿀 수 있어요.
        </Text>
      </View>

      {/* 하단 버튼 영역 */}
      <View style={styles.buttonContainer}>
        <Pressable
          style={({ pressed }) => [
            styles.button,
            styles.confirmButton,
            pressed && styles.pressedButton,
            loading && styles.disabledButton,
          ]}
          onPress={handleConfirm}
          disabled={loading}
          accessibilityLabel="맞아요 버튼"
          accessibilityRole="button"
          accessibilityHint="선택한 카테고리가 맞다면 두 번 탭하세요"
        >
          {loading ? (
            <ActivityIndicator color="#000000" />
          ) : (
            <Text
              style={[
                styles.confirmButtonText,
                loading && styles.disabledButtonText,
              ]}
            >
              맞아요
            </Text>
          )}
        </Pressable>

        <Pressable
          style={({ pressed }) => [
            styles.button,
            styles.modifyButton,
            pressed && styles.pressedButton,
          ]}
          onPress={handleGoBack}
          disabled={loading}
          accessibilityLabel="아니에요 수정할래요 버튼"
          accessibilityRole="button"
          accessibilityHint="카테고리를 수정하려면 두 번 탭하세요"
        >
          <Text style={styles.modifyButtonText}>아니에요 수정할래요</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#FFFFFF",
  },
  instructionContainer: {
    marginHorizontal: 20,
    marginTop: 20,
    marginBottom: 30,
    padding: 16,
    borderWidth: 1,
    borderColor: "#E0E0E0",
    borderRadius: 8,
    backgroundColor: "#FFFFFF",
  },
  instructionText: {
    fontSize: 16,
    textAlign: "center",
    color: "#000000",
    lineHeight: 24,
  },
  categoriesContainer: {
    marginHorizontal: 20,
    marginBottom: 30,
    alignItems: "center",
  },
  categoryItem: {
    marginBottom: 16,
  },
  diamondIcon: {
    width: 120,
    height: 60,
    backgroundColor: "#276ADC",
    justifyContent: "center",
    alignItems: "center",
    borderRadius: 16,
    elevation: 3,
    shadowColor: "#000",
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
  },
  diamondText: {
    fontSize: 24,
    fontWeight: "500",
    color: "white",
    textAlign: "center",
    width: 80,
  },
  summaryContainer: {
    marginHorizontal: 20,
    marginBottom: 40,
    padding: 16,
    borderWidth: 1,
    borderColor: "#E0E0E0",
    borderRadius: 8,
    backgroundColor: "#FFFFFF",
  },
  summaryText: {
    fontSize: 16,
    textAlign: "center",
    color: "#000000",
    lineHeight: 24,
    marginBottom: 8,
  },
  buttonContainer: {
    flexDirection: "row",
    justifyContent: "space-between",
    paddingHorizontal: 20,
    paddingBottom: 30,
    gap: 12,
  },
  button: {
    flex: 1,
    paddingVertical: 13,
    paddingHorizontal: 16,
    borderRadius: 8,
    alignItems: "center",
    justifyContent: "center",
    elevation: 3,
    shadowColor: "#000",
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
  },
  confirmButton: {
    backgroundColor: "#87CEEB",
  },
  modifyButton: {
    backgroundColor: "#87CEEB",
  },
  disabledButton: {
    backgroundColor: "#C7C7CC",
    elevation: 0,
    shadowOpacity: 0,
  },
  pressedButton: {
    opacity: 0.8,
    transform: [{ scale: 0.98 }],
  },
  confirmButtonText: {
    fontSize: 18,
    fontWeight: "600",
    color: "#000000",
    textAlign: "center",
  },
  disabledButtonText: {
    color: "#8E8E93",
  },
  modifyButtonText: {
    fontSize: 16,
    fontWeight: "600",
    color: "#000000",
    textAlign: "center",
    lineHeight: 24,
  },
});
