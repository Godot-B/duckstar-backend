package com.duckstar.domain;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.SeasonType;
import com.duckstar.domain.mapping.AnimeSeason;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Season extends BaseEntity {

    // 태그 역할 엔티티. 애니 분류 용도이며
    // Week 엔티티와는 직접적인 연관 ❌

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer year;

    private SeasonType type;

    @OneToMany(cascade = CascadeType.ALL)
    private List<AnimeSeason> animeSeasons = new ArrayList<>();
}
