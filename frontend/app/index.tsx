import { View, ActivityIndicator, Text } from "react-native";

export default function Index() {
  console.log("📱 Index 컴포넌트 렌더링됨");

  // _layout.tsx에서 라우팅을 처리하므로 로딩 화면만 표시
  return (
    <View style={{ 
      flex: 1, 
      justifyContent: "center", 
      alignItems: "center", 
      backgroundColor: "#FFFFFF" 
    }}>
      <ActivityIndicator size="large" color="#007AFF" />
      <Text style={{ 
        marginTop: 16, 
        fontSize: 16, 
        color: "#8E8E93",
        textAlign: "center" 
      }}>
        앱을 시작하는 중...
      </Text>
    </View>
  );
}