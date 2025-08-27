package com.newsapp.eyehope.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "news")
@Getter
@Setter
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;

    private String rss;

    @Column(name = "press_id")
    private Long pressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "press_id", insertable = false, updatable = false)
    private Press press;

    // 편의 메서드 - 언론사 이름 가져오기
    public String getPressName() {
        return press != null ? press.getName() : null;
    }
}
