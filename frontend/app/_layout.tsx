import {
  DarkTheme,
  DefaultTheme,
  ThemeProvider,
} from "@react-navigation/native";
import { useFonts } from "expo-font";
import { Stack, useRouter, useSegments } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { useEffect, useState } from "react";
import "react-native-reanimated";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { SafeAreaProvider, SafeAreaView } from "react-native-safe-area-context";

import { useColorScheme } from "@/hooks/useColorScheme";

export default function RootLayout() {
  const colorScheme = useColorScheme();
  const segments = useSegments();
  const router = useRouter();
  const [isNavigationReady, setIsNavigationReady] = useState(false);
  const [isCheckingAuth, setIsCheckingAuth] = useState(true);

  const [loaded] = useFonts({
    SpaceMono: require("../assets/fonts/SpaceMono-Regular.ttf"),
  });

  // 사용자 인증 상태 확인
  const checkAuthState = async () => {
    try {
      console.log("🔐 === _layout.tsx에서 인증 상태 확인 시작 ===");
      console.log("🔐 현재 segments:", segments);

      // 로컬 설정 상태 확인
      const setupCompleted = await AsyncStorage.getItem("setupCompleted");
      const savedCategories = await AsyncStorage.getItem("userCategories");
      const savedUserInfo = await AsyncStorage.getItem("userInfo");

      console.log("🔐 로컬 상태 확인:");
      console.log("  - setupCompleted:", setupCompleted);
      console.log("  - savedCategories:", savedCategories ? "존재" : "없음");
      console.log("  - savedUserInfo:", savedUserInfo ? "존재" : "없음");

      // 간단한 인증 상태 판단 - 관심 분야만 선택되어 있으면 인증된 것으로 간주
      const isAuthenticated = savedCategories && savedCategories !== "[]";

      console.log("🔐 최종 인증 상태:", isAuthenticated);

      // 인증되지 않은 경우 로컬 데이터 정리 (필요시에만)
      if (!isAuthenticated && setupCompleted === "true") {
        console.log("🔐 관심 분야 없음 - 로컬 데이터 정리");
        await AsyncStorage.multiRemove([
          "setupCompleted",
          "userCategories",
          "userTimes",
          "userInfo",
        ]);
      }

      // 라우팅 결정
      const inAuthGroup = segments[0] === "(tabs)";
      const isOnSelectCategory = segments[0] === "selectCategory";
      const isOnAuthFlow = [
        "selectCategory",
        "confirmation",
        "timeSelect",
      ].includes(segments[0] as string);

      console.log("🔐 라우팅 상태:");
      console.log("  - inAuthGroup:", inAuthGroup);
      console.log("  - isOnSelectCategory:", isOnSelectCategory);
      console.log("  - isOnAuthFlow:", isOnAuthFlow);
      console.log("  - segments:", segments);

      if (isAuthenticated) {
        if (!inAuthGroup) {
          console.log("🔐 인증된 사용자 → (tabs)로 이동");
          router.replace("/(tabs)");
        }
      } else {
        if (inAuthGroup) {
          console.log(
            "🔐 미인증 사용자가 (tabs)에 있음 → selectCategory로 이동"
          );
          router.replace("/selectCategory");
        } else if (!isOnAuthFlow) {
          console.log(
            "🔐 미인증 사용자가 인증 플로우 밖에 있음 → selectCategory로 이동"
          );
          router.replace("/selectCategory");
        }
      }
    } catch (error) {
      console.error("🔐 인증 상태 확인 오류:", error);
      // 오류 시 안전하게 초기 화면으로
      router.replace("/selectCategory");
    } finally {
      setIsCheckingAuth(false);
    }
  };

  useEffect(() => {
    if (!loaded) return;

    const timer = setTimeout(() => {
      setIsNavigationReady(true);
    }, 100);

    return () => clearTimeout(timer);
  }, [loaded]);

  useEffect(() => {
    if (!isNavigationReady) return;

    checkAuthState();
  }, [isNavigationReady]);

  // 폰트 로딩 중
  if (!loaded) {
    return null;
  }

  // 인증 확인 중에는 빈 화면 표시
  if (isCheckingAuth || !isNavigationReady) {
    return null;
  }

  // 기존 코드는 그대로 두고, Stack 부분만 수정
  return (
    <SafeAreaProvider>
      <ThemeProvider value={colorScheme === "dark" ? DarkTheme : DefaultTheme}>
        <SafeAreaView style={{ flex: 1 }} edges={["left", "right"]}>
          <Stack>
            <Stack.Screen
              name="(tabs)"
              options={{
                headerShown: false,
                gestureEnabled: false,
              }}
            />
            <Stack.Screen
              name="selectCategory"
              options={{
                headerShown: false,
                title: "카테고리 선택",
              }}
            />
            <Stack.Screen
              name="confirmation"
              options={{
                headerShown: false,
                title: "확인",
              }}
            />
            <Stack.Screen
              name="timeSelect"
              options={{
                headerShown: false,
                title: "시간 선택",
              }}
            />
            <Stack.Screen
              name="newsList"
              options={{
                headerShown: false,
                title: "관심 뉴스",
              }}
            />
            <Stack.Screen
              name="categoryNews"
              options={{
                headerShown: false,
                title: "카테고리별 뉴스",
              }}
            />
            <Stack.Screen
              name="index"
              options={{
                headerShown: false,
              }}
            />
            <Stack.Screen name="+not-found" />
          </Stack>
          <StatusBar style="auto" />
        </SafeAreaView>
      </ThemeProvider>
    </SafeAreaProvider>
  );
}
