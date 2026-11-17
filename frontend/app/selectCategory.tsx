import React, { useState } from "react";
import {
  View,
  Text,
  Pressable,
  ScrollView,
  StyleSheet,
  TouchableOpacity,
  Platform,
} from "react-native";
import { useRouter, useLocalSearchParams } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { selectCategoryStyles } from "../styles/selectCategoryStyles";

export default function SelectCategoryScreen() {
  // 다중 선택을 위한 상태: string[] 배열
  const [selectedCategories, setSelectedCategories] = useState<string[]>([]);
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { fromSettings } = useLocalSearchParams<{ fromSettings?: string }>();

  const categories = [
    "경제",
    "증권",
    "스포츠",
    "연예",
    "정치",
    "IT",
    "사회",
    "오피니언",
  ];

  // 카테고리 선택/해제 및 최대 5개 제한
  const handleCategorySelect = (category: string) => {
    if (selectedCategories.includes(category)) {
      // 이미 선택된 경우 해제
      setSelectedCategories(selectedCategories.filter((c) => c !== category));
    } else {
      if (selectedCategories.length < 5) {
        setSelectedCategories([...selectedCategories, category]);
      } else {
        // 5개 초과 선택 방지
        alert("최대 5개까지 선택할 수 있어요!");
      }
    }
  };

  // 뒤로가기 처리
  const handleGoBack = () => {
    router.back();
  };

  // 완료 버튼 클릭 시, 선택된 카테고리 배열을 쿼리 파라미터로 전달
  const handleComplete = () => {
    if (selectedCategories.length > 0) {
      console.log("선택된 카테고리:", selectedCategories);
      console.log("fromSettings 파라미터:", fromSettings);
      console.log("fromSettings 타입:", typeof fromSettings);

      router.push({
        pathname: "/confirmation",
        params: {
          categories: JSON.stringify(selectedCategories),
          fromSettings: fromSettings || undefined,
        },
      });
    }
  };

  return (
    <View style={selectCategoryStyles.container}>
      {/* 상단 헤더 - 설정 페이지에서 온 경우에만 표시 */}
      {fromSettings === "true" && (
        <View
          style={[
            selectCategoryStyles.header,
            { paddingTop: Math.max(insets.top + 10, 20) },
          ]}
        >
          <TouchableOpacity
            style={selectCategoryStyles.backButton}
            onPress={handleGoBack}
          >
            <Ionicons name="chevron-back" size={24} color="#007AFF" />
          </TouchableOpacity>
          <Text style={selectCategoryStyles.headerTitle}>관심 분야 선택</Text>
          <View style={selectCategoryStyles.placeholder} />
        </View>
      )}

      {/* 상단 안내 문구 */}
      <View
        style={[
          selectCategoryStyles.instructionContainer,
          {
            marginTop:
              fromSettings === "true"
                ? Platform.OS === "android"
                  ? Math.max(insets.top + 10, 30)
                  : 35
                : Platform.OS === "android"
                ? Math.max(insets.top + 10, 70)
                : 75,
          },
        ]}
      >
        <Text
          style={[
            selectCategoryStyles.instructionText,
            {
              color: "#000000",
              fontSize: 18,
              fontWeight: "bold",
            },
          ]}
        >
          관심 분야 선택을 진행하겠습니다.
        </Text>
        <Text
          style={[
            selectCategoryStyles.instructionText,
            {
              color: "#333333",
              fontSize: 16,
            },
          ]}
        >
          총 8개의 분야 중, 원하는 뉴스 기사 분야를{"\n"}
          최대 5개까지 선택해주세요.
        </Text>
      </View>

      {/* 중간 버튼 그리드 */}
      <ScrollView
        style={selectCategoryStyles.gridContainer}
        showsVerticalScrollIndicator={false}
        contentContainerStyle={selectCategoryStyles.gridContent}
      >
        <View style={selectCategoryStyles.gridWrapper}>
          {categories.map((item) => {
            const isSelected = selectedCategories.includes(item);
            return (
              <Pressable
                key={item}
                style={[
                  selectCategoryStyles.categoryButton,
                  isSelected && selectCategoryStyles.selectedCategoryButton,
                ]}
                onPress={() => handleCategorySelect(item)}
                onPressIn={() => console.log("터치 시작:", item)}
                accessibilityLabel={`${item} 카테고리`}
                accessibilityRole="button"
                accessibilityState={{ selected: isSelected }}
                accessibilityHint="이 카테고리를 선택하려면 두 번 탭하세요"
              >
                <Text
                  style={[
                    selectCategoryStyles.categoryButtonText,
                    isSelected &&
                      selectCategoryStyles.selectedCategoryButtonText,
                  ]}
                >
                  {item}
                </Text>
              </Pressable>
            );
          })}
        </View>

        {/* 완료 버튼 - 카테고리 그리드 바로 밑에 위치 */}
        <View style={selectCategoryStyles.completeButtonContainer}>
          <Pressable
            style={({ pressed }) => [
              selectCategoryStyles.completeButton,
              selectedCategories.length === 0 &&
                selectCategoryStyles.disabledCompleteButton,
              pressed && selectCategoryStyles.pressedButton,
            ]}
            onPress={handleComplete}
            disabled={selectedCategories.length === 0}
            onPressIn={() => console.log("완료 버튼 터치됨")}
            accessibilityLabel="선택 완료 버튼"
            accessibilityRole="button"
            accessibilityState={{ disabled: selectedCategories.length === 0 }}
            accessibilityHint={
              selectedCategories.length > 0
                ? "선택한 카테고리로 진행하려면 두 번 탭하세요"
                : "카테고리를 먼저 선택해주세요"
            }
          >
            <Text
              style={[
                selectCategoryStyles.completeButtonText,
                selectedCategories.length === 0 &&
                  selectCategoryStyles.disabledCompleteButtonText,
              ]}
            >
              완료
            </Text>
          </Pressable>
        </View>
      </ScrollView>
    </View>
  );
}
