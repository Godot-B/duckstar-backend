package com.duckstar.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_quarter_yq",
                        columnList = "year_value, quarter_value")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_quarter_yq",
                        columnNames = {"year_value", "quarter_value"})
        }
)
public class Quarter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer yearValue;

    @Column(nullable = false)
    private Integer quarterValue;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Week> weeks = new ArrayList<>();
}
