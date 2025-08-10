package com.duckstar.domain.medal;

import com.duckstar.domain.Week;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.MedalType;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
public abstract class Medal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    Week week;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    MedalType type;
}