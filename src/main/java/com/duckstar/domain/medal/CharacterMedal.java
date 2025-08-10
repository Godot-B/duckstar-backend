package com.duckstar.domain.medal;

import com.duckstar.domain.Character;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("CHARACTER") // dtype 컬럼에 'CHARACTER'으로 저장됨
public class CharacterMedal extends Medal {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character character;
}