package com.duckstar.domain;

import com.duckstar.domain.enums.Gender;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "characters")
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")  // id 이름 명시
    private Long id;

    private String nameKor;

    private String nameKanji;

    private String nameEng;

    private String cv;  // 성우

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;
}
