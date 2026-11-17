import React, { useState } from "react";
import {
  View,
  Text,
  Pressable,
  StyleSheet,
  ScrollView,
  Alert,
  ActivityIndicator,
} from "react-native";
import { useRouter, useLocalSearchParams } from "expo-router";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { timeSelectStyles } from "../styles/timeSelectStyles";

interface NotificationScheduleData {
  notificationTime: string[];
}

export default function TimeSelectScreen() {
  const { categories, fromSettings } = useLocalSearchParams<{
    categories: string;
    fromSettings?: string;
  }>();
  const router = useRouter();

  // JSON 문자열을 파싱하여 카테고리 배열로 변환
  const selectedCategories = categories ? JSON.parse(categories) : [];
  const [selectedMorningTime, setSelectedMorningTime] = useState<string | null>(
    null
  );
  const [selectedEveningTime, setSelectedEveningTime] = useState<string | null>(
    null
  );
  const [loading, setLoading] = useState(false);

  const morningTimes = [
    "05:00",
    "06:00",
    "07:00",
    "08:00",
    "09:00",
    "10:00",
    "11:00",
    "12:00",
    "13:00",
  ];
  const eveningTimes = [
    "13:00",
    "14:00",
    "15:00",
    "16:00",
    "17:00",
    "18:00",
    "19:00",
    "20:00",
    "21:00",
  ];

  const handleMorningTimeSelect = (time: string) => {
    setSelectedMorningTime(time);
  };

  const handleEveningTimeSelect = (time: string) => {
    setSelectedEveningTime(time);
  };

  // 백엔드에 알림 시간 업데이트 요청 - POST 요청 제거됨
  const updateNotificationSchedule = async (
    scheduleData: NotificationScheduleData
  ) => {
    try {
      console.log("🔄 === 알림 시간 업데이트 (로컬 저장만) ===");
      console.log("📤 저장할 데이터:", JSON.stringify(scheduleData, null, 2));

      // POST 요청 대신 로컬 저장만 수행
      console.log("✅ 알림 시간이 로컬에 저장되었습니다");

      return { success: true, message: "알림 시간이 로컬에 저장되었습니다" };
    } catch (error) {
      console.error("🚨 알림 시간 업데이트 오류:", error);
      throw error;
    }
  };

  // 설정 완료 플래그 저장 함수
  const saveSetupCompleted = async () => {
    try {
      await AsyncStorage.setItem("setupCompleted", "true");
      console.log("설정 완료 플래그가 저장되었습니다");
    } catch (error) {
      console.error("설정 완료 플래그 저장 오류:", error);
    }
  };

  const handleComplete = async () => {
    // fromSettings가 아닌 경우 시간 선택을 필수로 만들기
    if (
      fromSettings !== "true" &&
      (!selectedMorningTime || !selectedEveningTime)
    ) {
      Alert.alert(
        "시간 선택 필요",
        "알림을 받을 시간을 선택해주세요.\n아침과 저녁 시간을 모두 선택해야 합니다.",
        [{ text: "확인" }]
      );
      return;
    }

    const selectedTimes = {
      morning: selectedMorningTime || "09:00", // 기본값 설정
      evening: selectedEveningTime || "12:45", // 기본값 설정
    };

    console.log("=== 시간 선택 완료 ===");
    console.log("선택된 시간:", selectedTimes);
    console.log("fromSettings:", fromSettings);

    // fromSettings 파라미터 확인
    if (fromSettings === "true") {
      // 설정 페이지에서 온 경우 - 백엔드에 시간 업데이트 요청
      setLoading(true);

      // 설정 페이지로 돌아가면서 시간 정보 전달
      router.push({
        pathname: "/(tabs)/settings",
        params: {
          selectedTimes: JSON.stringify(selectedTimes),
          fromSettings: "true",
        },
      });
    } else {
      // 일반 플로우(초기 설정)라면 바로 메인 탭으로 이동
      console.log("설정 완료 - 메인 탭으로 이동");
      console.log("categories:", categories);
      console.log("selectedTimes:", JSON.stringify(selectedTimes));

      // 설정 완료 플래그 저장
      await saveSetupCompleted();

      router.push({
        pathname: "/(tabs)",
        params: {
          categories: categories,
          selectedTimes: JSON.stringify(selectedTimes),
        },
      });
    }
  };

  const renderTimeButton = (
    time: string,
    isSelected: boolean,
    onPress: () => void,
    accessibilityLabel: string
  ) => (
    <Pressable
      style={[
        timeSelectStyles.timeButton,
        isSelected && timeSelectStyles.selectedTimeButton,
      ]}
      onPress={onPress}
      accessibilityLabel={accessibilityLabel}
      accessibilityRole="button"
      accessibilityState={{ selected: isSelected }}
      accessibilityHint="이 시간을 선택하려면 두 번 탭하세요"
    >
      <Text
        style={[
          timeSelectStyles.timeButtonText,
          isSelected && timeSelectStyles.selectedTimeButtonText,
        ]}
      >
        {time}
      </Text>
    </Pressable>
  );

  return (
    <View style={timeSelectStyles.container}>
      {/* 상단 안내 문구 */}
      <View style={timeSelectStyles.instructionContainer}>
        <Text style={timeSelectStyles.instructionText}>
          {fromSettings === "true"
            ? "새로운 알림 시간을 선택해주세요."
            : "매일 아침 / 저녁으로 핫한 뉴스를 알림으로 보내드려요."}
        </Text>
        {fromSettings !== "true" && (
          <Text style={timeSelectStyles.instructionTextBlue}>
            어느 시간 대를 원하는지 골라주세요.
          </Text>
        )}
        <Text style={timeSelectStyles.instructionSubText}>
          {fromSettings === "true"
            ? "(시간을 선택하지 않으면 기존 설정이 유지됩니다)"
            : "(알림 시간은 선택사항입니다)"}
        </Text>
      </View>

      {/* 시간 선택 영역 */}
      <ScrollView
        style={timeSelectStyles.timeSelectionContainer}
        showsVerticalScrollIndicator={false}
      >
        <View style={timeSelectStyles.timeGridContainer}>
          {/* 아침 섹션 */}
          <View style={timeSelectStyles.timeSection}>
            <View style={timeSelectStyles.sectionHeader}>
              <Text style={timeSelectStyles.sectionHeaderText}>아침</Text>
            </View>
            <View style={timeSelectStyles.timeButtonsContainer}>
              {morningTimes.map((time) => (
                <View key={time} style={timeSelectStyles.timeButtonWrapper}>
                  {renderTimeButton(
                    time,
                    selectedMorningTime === time,
                    () => handleMorningTimeSelect(time),
                    `${time} 아침 시간 선택`
                  )}
                </View>
              ))}
            </View>
          </View>

          {/* 저녁 섹션 */}
          <View style={timeSelectStyles.timeSection}>
            <View style={timeSelectStyles.sectionHeader}>
              <Text style={timeSelectStyles.sectionHeaderText}>저녁</Text>
            </View>
            <View style={timeSelectStyles.timeButtonsContainer}>
              {eveningTimes.map((time) => (
                <View key={time} style={timeSelectStyles.timeButtonWrapper}>
                  {renderTimeButton(
                    time,
                    selectedEveningTime === time,
                    () => handleEveningTimeSelect(time),
                    `${time} 저녁 시간 선택`
                  )}
                </View>
              ))}
            </View>
          </View>
        </View>
      </ScrollView>

      {/* 하단 완료 버튼 */}
      <View style={timeSelectStyles.buttonContainer}>
        <Pressable
          style={({ pressed }) => [
            timeSelectStyles.completeButton,
            pressed && timeSelectStyles.pressedButton,
            loading && timeSelectStyles.disabledButton,
          ]}
          onPress={handleComplete}
          disabled={loading}
          accessibilityLabel="다음 단계로 이동"
          accessibilityRole="button"
          accessibilityHint={
            fromSettings === "true"
              ? "설정을 저장하고 설정 페이지로 돌아갑니다"
              : "사용자 정보 입력 화면으로 이동합니다"
          }
        >
          {loading ? (
            <ActivityIndicator color="#FFFFFF" />
          ) : (
            <Text
              style={[
                timeSelectStyles.completeButtonText,
                loading && timeSelectStyles.disabledButtonText,
              ]}
            >
              {fromSettings === "true" ? "저장" : "다음"}
            </Text>
          )}
        </Pressable>

        {/* 시간 선택 안내 텍스트 */}
        <Text style={timeSelectStyles.skipText}>
          {fromSettings === "true"
            ? "변경사항이 즉시 적용됩니다"
            : "알림 시간은 나중에 설정에서 변경할 수 있습니다"}
        </Text>

        {/* 선택된 시간 표시 (디버그용) */}
        {(selectedMorningTime || selectedEveningTime) && (
          <View style={timeSelectStyles.selectedTimeDebug}>
            <Text style={timeSelectStyles.debugText}>
              선택된 시간: {selectedMorningTime || "미선택"} /{" "}
              {selectedEveningTime || "미선택"}
            </Text>
          </View>
        )}
      </View>
    </View>
  );
}
