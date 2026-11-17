// components/SafeAreaLayout.tsx
import React from 'react';
import { View, StyleSheet, StatusBar, Platform } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

interface SafeAreaLayoutProps {
  children: React.ReactNode;
  backgroundColor?: string;
  statusBarStyle?: 'default' | 'light-content' | 'dark-content';
  hideStatusBar?: boolean;
}

export function SafeAreaLayout({
  children,
  backgroundColor = '#F2F2F7',
  statusBarStyle = 'dark-content',
  hideStatusBar = false,
}: SafeAreaLayoutProps) {
  const insets = useSafeAreaInsets();

  return (
    <>
      <StatusBar
        barStyle={statusBarStyle}
        backgroundColor={backgroundColor}
        translucent={Platform.OS === 'android'}
        hidden={hideStatusBar}
      />
      <View
        style={[
          styles.container,
          {
            backgroundColor,
            paddingTop: Platform.OS === 'android' ? insets.top : 0,
            paddingBottom: Platform.OS === 'android' ? insets.bottom : 0,
          },
        ]}
      >
        {children}
      </View>
    </>
  );
}

// 헤더용 컴포넌트
interface SafeHeaderProps {
  children: React.ReactNode;
  backgroundColor?: string;
}

export function SafeHeader({ 
  children, 
  backgroundColor = '#FFFFFF' 
}: SafeHeaderProps) {
  const insets = useSafeAreaInsets();

  return (
    <View
      style={[
        styles.header,
        {
          backgroundColor,
          paddingTop: Platform.OS === 'ios' ? insets.top : 0,
        },
      ]}
    >
      {children}
    </View>
  );
}

// 하단 탭바용 컴포넌트
interface SafeTabBarProps {
  children: React.ReactNode;
  backgroundColor?: string;
}

export function SafeTabBar({ 
  children, 
  backgroundColor = '#FFFFFF' 
}: SafeTabBarProps) {
  const insets = useSafeAreaInsets();

  return (
    <View
      style={[
        styles.tabBar,
        {
          backgroundColor,
          paddingBottom: Math.max(insets.bottom, 5), // 최소 5px 확보
        },
      ]}
    >
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    paddingHorizontal: 20,
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#E5E5EA',
  },
  tabBar: {
    borderTopWidth: 1,
    borderTopColor: '#E5E5EA',
    paddingTop: 5,
  },
});