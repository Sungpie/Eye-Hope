import { StyleSheet } from "react-native";

export const timeSelectStyles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#FFFFFF",
  },
  instructionContainer: {
    marginHorizontal: 20,
    marginTop: 20,
    marginBottom: 20,
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: "#E0E0E0",
  },
  instructionText: {
    fontSize: 16,
    textAlign: "center",
    color: "#000000",
    lineHeight: 24,
    marginBottom: 8,
  },
  instructionTextBlue: {
    fontSize: 16,
    textAlign: "center",
    color: "#007AFF",
    lineHeight: 24,
    fontWeight: "500",
    marginBottom: 8,
  },
  instructionSubText: {
    fontSize: 14,
    textAlign: "center",
    color: "#8E8E93",
    lineHeight: 20,
  },
  timeSelectionContainer: {
    flex: 1,
    marginHorizontal: 20,
  },
  timeGridContainer: {
    flexDirection: "row",
    justifyContent: "space-between",
  },
  timeSection: {
    flex: 1,
    marginHorizontal: 8,
  },
  sectionHeader: {
    backgroundColor: "#87CEEB",
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderRadius: 8,
    marginBottom: 16,
    alignItems: "center",
  },
  sectionHeaderText: {
    fontSize: 16,
    fontWeight: "600",
    color: "#FFFFFF",
  },
  timeButtonsContainer: {
    alignItems: "center",
  },
  timeButtonWrapper: {
    marginBottom: 12,
    width: "100%",
  },
  timeButton: {
    backgroundColor: "#87CEEB",
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderRadius: 8,
    alignItems: "center",
    elevation: 2,
    shadowColor: "#000",
    shadowOffset: {
      width: 0,
      height: 1,
    },
    shadowOpacity: 0.2,
    shadowRadius: 1.41,
  },
  selectedTimeButton: {
    backgroundColor: "#007AFF",
  },
  timeButtonText: {
    fontSize: 16,
    fontWeight: "500",
    color: "#FFFFFF",
  },
  selectedTimeButtonText: {
    color: "#FFFFFF",
  },
  buttonContainer: {
    paddingHorizontal: 20,
    paddingBottom: 30,
    alignItems: "center",
  },
  completeButton: {
    paddingVertical: 16,
    paddingHorizontal: 32,
    borderRadius: 8,
    backgroundColor: "#007AFF",
    minWidth: 120,
    alignItems: "center",
    elevation: 3,
    shadowColor: "#000",
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    marginBottom: 12,
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
  completeButtonText: {
    fontSize: 18,
    fontWeight: "600",
    color: "#FFFFFF",
  },
  disabledButtonText: {
    color: "#8E8E93",
  },
  skipText: {
    fontSize: 12,
    color: "#8E8E93",
    textAlign: "center",
    lineHeight: 16,
  },
  selectedTimeDebug: {
    marginTop: 12,
    padding: 8,
    backgroundColor: "#FFF3CD",
    borderRadius: 6,
    borderWidth: 1,
    borderColor: "#FFE69C",
  },
  debugText: {
    fontSize: 12,
    color: "#856404",
    textAlign: "center",
    fontWeight: "500",
  },
});
