package com.duckstar.domain;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "characters",
        indexes = {
                @Index(name = "idx_characters_n",
                        columnList = "nameKor")
        }
)
@Builder
@AllArgsConstructor
public class Character extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")  // id 이름 명시
    private Long id;

    @Column(nullable = false)
    private String nameKor;

    private String nameKanji;

    private String nameEng;

    private String cv;  // 성우

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)")
    private Gender gender;

    @Column(length = 1024)
    private String mainImageUrl;

    @Column(length = 1024)
    private String mainThumbnailUrl;

    private Integer peakRank;

    private LocalDate peakDate;

    private Integer weeksOnTop10;

    public void updateImage(String mainImageUrl, String mainThumbnailUrl) {
        this.mainImageUrl = mainImageUrl;
        this.mainThumbnailUrl = mainThumbnailUrl;
    }
}
