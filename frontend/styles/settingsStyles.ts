import { StyleSheet } from "react-native";

export const settingsStyles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F2F2F7",
  },
  header: {
    backgroundColor: "#FFFFFF",
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 16,
    borderBottomWidth: 1,
    borderBottomColor: "#E5E5EA",
  },
  title: {
    fontSize: 28,
    fontWeight: "bold",
    color: "#000000",
    textAlign: "center",
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: "#8E8E93",
    lineHeight: 22,
  },
  loadingContainer: {
    paddingVertical: 20,
    alignItems: "center",
  },
  loadingText: {
    fontSize: 16,
    color: "#8E8E93",
  },
  sectionHeaderSimple: {
    marginBottom: 12,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#000000",
  },
  interestNewsSection: {
    marginHorizontal: 20,
    marginTop: 20,
    padding: 16,
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E5E5EA",
  },
  categoriesContainer: {
    flexDirection: "row",
    flexWrap: "wrap",
    marginBottom: 16,
    marginTop: 16,
  },
  categoryItemContainer: {
    alignItems: "center",
    marginRight: 8,
    marginBottom: 8,
  },
  categoryTag: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
  },
  categoryText: {
    color: "#FFFFFF",
    fontSize: 14,
    fontWeight: "500",
  },
  questionText: {
    fontSize: 16,
    color: "#000000",
    marginBottom: 8,
  },
  instructionText: {
    fontSize: 16,
    color: "#007AFF",
    fontWeight: "500",
  },
  timeChangeSection: {
    marginHorizontal: 20,
    marginTop: 20,
    padding: 16,
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E5E5EA",
  },
  timeInfoContainer: {
    marginTop: 16,
    marginBottom: 16,
  },
  timeInfoText: {
    fontSize: 16,
    color: "#000000",
    lineHeight: 22,
  },
  timeButtonsContainer: {
    flexDirection: "row",
    alignItems: "center",
    flexWrap: "wrap",
    marginTop: 8,
  },
  timeButton: {
    backgroundColor: "#007AFF",
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
    marginHorizontal: 4,
  },
  timeButtonText: {
    color: "#FFFFFF",
    fontSize: 14,
    fontWeight: "500",
  },
  appInfoSection: {
    marginHorizontal: 20,
    marginTop: 20,
    marginBottom: 30,
    padding: 16,
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E5E5EA",
  },
  appInfoText: {
    fontSize: 14,
    color: "#8E8E93",
    textAlign: "center",
    lineHeight: 20,
    marginTop: 8,
  },
  supportText: {
    fontSize: 12,
    color: "#C7C7CC",
    textAlign: "center",
    lineHeight: 16,
    marginTop: 12,
  },
});
