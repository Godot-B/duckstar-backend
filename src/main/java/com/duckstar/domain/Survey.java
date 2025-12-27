package com.duckstar.domain;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.SurveyStatus;
import com.duckstar.domain.enums.SurveyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Survey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    private SurveyType surveyType;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)")
    private SurveyStatus status = SurveyStatus.CLOSED;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private Integer votes = 0;

    private Integer voterCount = 0;

    private Boolean announcePrepared = false;

    public void updateStatus(LocalDateTime now) {
        if (status == SurveyStatus.NOT_YET) {
            if (startDateTime != null && !now.isBefore(startDateTime)) {
                status = SurveyStatus.OPEN;
            }
        } else if (status == SurveyStatus.OPEN) {
            if (endDateTime != null && !now.isBefore(endDateTime)) {
                status = SurveyStatus.CLOSED;
            }
        }
    }
}