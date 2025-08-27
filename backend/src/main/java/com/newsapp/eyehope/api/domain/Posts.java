package com.newsapp.eyehope.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Posts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String source;
    private String title;

    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "text")
    private String url;

    // 이 필드는 제거하고 News 엔티티와의 관계로 대체
    // private String category;

    // News 테이블의 ID를 FK로 참조
    @Column(name = "news_id", nullable = false)
    private Long newsId;

    // News 카테고리 정보 접근을 위한 메서드
    public String getCategory() {
        // News 엔티티 관계가 로드된 경우 활용 가능
        // 현재는 ID 기반으로 카테고리 정보 반환
        // 1: 경제, 2: 증권, 3: 스포츠, 4: 연예, 5: 정치, 6: IT, 7: 사회, 8: 오피니언
        if (newsId == null) return null;

        return switch (newsId.intValue()) {
            case 1 -> "경제";
            case 2 -> "증권";
            case 3 -> "스포츠";
            case 4 -> "연예";
            case 5 -> "정치";
            case 6 -> "IT";
            case 7 -> "사회";
            case 8 -> "오피니언";
            default -> "기타";
        };
    }

    // 카테고리 문자열로 newsId 설정
    public void setCategory(String category) {
        if (category == null) return;

        this.newsId = switch (category) {
            case "경제" -> 1L;
            case "증권" -> 2L;
            case "스포츠" -> 3L;
            case "연예" -> 4L;
            case "정치" -> 5L;
            case "IT" -> 6L;
            case "사회" -> 7L;
            case "오피니언" -> 8L;
            default -> null;
        };
    }

    @CreatedDate
    @Column(name = "collected_at")
    private LocalDateTime collectedAt;
}
