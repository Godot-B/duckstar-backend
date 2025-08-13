package com.duckstar.repository.AnimeCharacter;

import com.duckstar.domain.mapping.AnimeCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimeCharacterRepository extends JpaRepository<AnimeCharacter, Long>, AnimeCharacterRepositoryCustom {
}
