import React from "react";
import {
  View,
  Text,
  StyleSheet,
  SafeAreaView,
  ScrollView,
  Pressable,
  Platform,
  BackHandler,
} from "react-native";
import { useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useFocusEffect } from "@react-navigation/native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { categoryStyles } from "../../styles/categoryStyles";

export default function CategoryScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();

  const categories = [
    { name: "경제", icon: "trending-up" as const, color: "#FF6B6B" },
    { name: "증권", icon: "bar-chart" as const, color: "#4ECDC4" },
    { name: "스포츠", icon: "football" as const, color: "#45B7D1" },
    { name: "연예", icon: "star" as const, color: "#96CEB4" },
    { name: "정치", icon: "people" as const, color: "#FECA57" },
    { name: "IT", icon: "laptop" as const, color: "#48CAE4" },
    { name: "사회", icon: "home" as const, color: "#FF9FF3" },
    { name: "오피니언", icon: "chatbubble" as const, color: "#54A0FF" },
  ];

  const handleCategoryPress = (category: string) => {
    console.log(`${category} 카테고리 선택됨`);
    router.push({
      pathname: "/categoryNews",
      params: { category: category },
    });
  };

  // Android에서 뒤로가기 버튼 처리 및 탭 방문 추적
  useFocusEffect(
    React.useCallback(() => {
      // 카테고리 탭 방문 기록
      AsyncStorage.setItem("lastVisitedTab", "category");
      console.log("카테고리 탭 방문 기록됨");

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
    }, [])
  );

  return (
    <SafeAreaView style={categoryStyles.container}>
      {/* 상단 제목 */}
      <View
        style={[
          categoryStyles.header,
          {
            paddingTop:
              Platform.OS === "android" ? Math.max(insets.top + 20, 30) : 20,
          },
        ]}
      >
        <View style={categoryStyles.titleContainer}>
          <Text style={[categoryStyles.title, { textAlign: "center" }]}>
            카테고리
          </Text>
          <Text style={categoryStyles.subtitle}>
            <Text style={{ textAlign: "center" }}>
              원하는 카테고리를 선택해서 최신 뉴스를 확인하세요
            </Text>
          </Text>
        </View>
      </View>

      {/* 카테고리 그리드 */}
      <ScrollView
        style={categoryStyles.content}
        showsVerticalScrollIndicator={false}
      >
        <View style={categoryStyles.categoriesGrid}>
          {categories.map((category) => (
            <Pressable
              key={category.name}
              style={({ pressed }) => [
                categoryStyles.categoryButton,
                { backgroundColor: category.color },
                pressed && categoryStyles.pressedCategoryButton,
              ]}
              onPress={() => handleCategoryPress(category.name)}
              accessibilityLabel={`${category.name} 카테고리`}
              accessibilityRole="button"
              accessibilityHint={`${category.name} 카테고리의 뉴스를 보려면 두 번 탭하세요`}
            >
              <View style={categoryStyles.categoryContent}>
                <Ionicons
                  name={category.icon}
                  size={32}
                  color="#FFFFFF"
                  style={categoryStyles.categoryIcon}
                />
                <Text style={categoryStyles.categoryText}>{category.name}</Text>
                <View style={categoryStyles.arrowIcon}>
                  <Ionicons name="chevron-forward" size={20} color="#FFFFFF" />
                </View>
              </View>
            </Pressable>
          ))}
        </View>

        {/* 안내 메시지 */}
        <View style={categoryStyles.infoSection}>
          <View style={categoryStyles.infoCard}>
            <Ionicons name="information-circle" size={24} color="#007AFF" />
            <View style={categoryStyles.infoTextContainer}>
              <Text style={categoryStyles.infoTitle}>카테고리별 뉴스 보기</Text>
              <Text style={categoryStyles.infoDescription}>
                각 카테고리를 터치하면 해당 분야의 최신 뉴스를 확인할 수
                있습니다. 실시간으로 업데이트되는 뉴스를 놓치지 마세요!
              </Text>
            </View>
          </View>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
